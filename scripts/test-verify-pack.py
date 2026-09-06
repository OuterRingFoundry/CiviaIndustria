#!/usr/bin/env python3
import hashlib,importlib.util,io,tempfile,unittest,zipfile
from pathlib import Path
spec=importlib.util.spec_from_file_location('verify_pack',Path(__file__).with_name('verify-pack.py'));module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
class VerifyPackTests(unittest.TestCase):
    def test_ranges(self):
        self.assertTrue(module.accepts('[21.1,)', '21.1.249'))
        self.assertTrue(module.accepts('[1.21.1,1.21.2)', '1.21.1'))
        self.assertFalse(module.accepts('[1.21.2,)', '1.21.1'))
        self.assertFalse(module.accepts('[1.21.1,1.21.2)', '1.21.2'))
    def test_valid_hashes_and_rejections(self):
        with tempfile.TemporaryDirectory() as path:
            root=Path(path);buffer=io.BytesIO()
            with zipfile.ZipFile(buffer,'w') as jar:jar.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\n[[mods]]\nmodId="example"\nversion="1.0"\n[[dependencies.example]]\nmodId="minecraft"\ntype="required"\nversionRange="[1.21.1,1.21.2)"\n')
            data=buffer.getvalue();(root/'example.jar').write_bytes(data)
            a={'filename':'example.jar','side':'both','minecraft':'1.21.1','loader':'neoforge','sha256':hashlib.sha256(data).hexdigest()};lock={'schema':1,'artifacts':[a]}
            self.assertIn('example',module.verify(lock,root,'server'))
            (root/'extra.jar').write_bytes(data)
            with self.assertRaises(ValueError):module.verify(lock,root,'server')
            (root/'extra.jar').unlink();(root/'example.jar').write_bytes(b'bad')
            with self.assertRaises(ValueError):module.verify(lock,root,'server')
            with self.assertRaises(ValueError):module.verify({'schema':1,'artifacts':[]},root,'server')
    def test_metadata_adversaries(self):
        def jar(text,nested=None):
            out=io.BytesIO()
            with zipfile.ZipFile(out,'w') as z:
                z.writestr('META-INF/neoforge.mods.toml',text)
                if nested is not None:
                    z.writestr('META-INF/jarjar/metadata.json','{"jars":[{"path":"META-INF/jarjar/child.jar"}]}')
                    z.writestr('META-INF/jarjar/child.jar',nested)
            return out.getvalue()
        base='modLoader="javafml"\n[[mods]]\nmodId="example"\nversion="1"\n'
        with tempfile.TemporaryDirectory() as path:
            root=Path(path)
            for suffix in ['[[dependencies.example]]\nmodId="missing"\ntype="required"\n', '[[dependencies.example]]\nmodId="minecraft"\ntype="required"\nversionRange="[1.22,)"\n']:
                data=jar(base+suffix);(root/'a.jar').write_bytes(data)
                lock={'schema':1,'artifacts':[{'filename':'a.jar','side':'both','minecraft':'1.21.1','loader':'neoforge','sha256':hashlib.sha256(data).hexdigest()}]}
                with self.assertRaises(ValueError):module.verify(lock,root,'server')
            data=jar(base,jar(base));(root/'a.jar').write_bytes(data);lock['artifacts'][0]['sha256']=hashlib.sha256(data).hexdigest()
            with self.assertRaises(ValueError):module.verify(lock,root,'server')
            lock['artifacts'][0]['loader']='fabric'
            with self.assertRaises(ValueError):module.verify(lock,root,'server')
if __name__=='__main__':unittest.main()
