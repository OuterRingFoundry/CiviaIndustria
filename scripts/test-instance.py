#!/usr/bin/env python3
"""Regression checks for launcher-rewritten properties and exact pack verification."""
import importlib.util,tempfile,unittest
from pathlib import Path
from pack_manifest import fingerprint
spec=importlib.util.spec_from_file_location('verify',Path(__file__).with_name('verify-instance.py'));verify=importlib.util.module_from_spec(spec);spec.loader.exec_module(verify)
class InstanceTests(unittest.TestCase):
 def test_properties_and_managed_files(self):
  with tempfile.TemporaryDirectory() as tmp:
   root=Path(tmp);(root/'mods').mkdir();(root/'mods/test.jar').write_bytes(b'fixture');(root/'config').mkdir();(root/'config/mod.toml').write_text('enabled=true\n');(root/'server.properties').write_text('# generated\nonline-mode=true\nserver-port=25565\n');fingerprint(root)
   (root/'server.properties').write_text('# rewritten timestamp\nserver-port=25565\nonline-mode=true\n');verify.verify(root)
   (root/'server.properties').write_text('online-mode=false\nserver-port=25565\n')
   with self.assertRaisesRegex(ValueError,'Properties changed'):verify.verify(root)
   (root/'server.properties').write_text('online-mode=true\nserver-port=25565\n');(root/'config/extra.toml').write_text('enabled=false')
   with self.assertRaisesRegex(ValueError,'Unexpected pack file'):verify.verify(root)
 def test_unsafe_property_path(self):
  import json
  with tempfile.TemporaryDirectory() as tmp:
   root=Path(tmp);(root/'mods').mkdir();(root/'mods/test.jar').write_bytes(b'fixture');state=fingerprint(root);state['properties']['../outside.properties']={};(root/'pack-state.json').write_text(json.dumps(state))
   with self.assertRaisesRegex(ValueError,'Unsafe manifest path'):verify.verify(root)
if __name__=='__main__':unittest.main()
