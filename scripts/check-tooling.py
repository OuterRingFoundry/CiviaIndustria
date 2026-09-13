#!/usr/bin/env python3
"""Run the hyphen-named tooling test modules, which unittest discovery skips."""
import importlib.util
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MODULES = ('test-verify-pack', 'test-backup', 'test-instance',
           'test-fixture-process', 'test-gametest-results')
suite = unittest.TestSuite()
for name in MODULES:
    spec = importlib.util.spec_from_file_location(name.replace('-', '_'), ROOT / (name + '.py'))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    tests = unittest.defaultTestLoader.loadTestsFromModule(module)
    if not tests.countTestCases():
        raise RuntimeError('No tooling tests discovered in ' + name)
    suite.addTests(tests)
result = unittest.TextTestRunner(verbosity=2).run(suite)
sys.exit(0 if result.wasSuccessful() else 1)
