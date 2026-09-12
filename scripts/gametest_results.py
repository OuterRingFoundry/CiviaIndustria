"""Fail closed on empty, incomplete or crashed dedicated GameTest runs."""
import re


def required_test_count(source):
    # The current suite uses required @GameTest methods (no generated/optional tests).
    return sum(len(re.findall(r'@GameTest\s*\(', p.read_text()))
               for p in source.rglob('*.java'))


def evaluate(text, exit_code, expected):
    counts = [int(n) for n in re.findall(r'All (\d+) required tests passed', text)]
    errors = [marker for marker in ('Encountered an unexpected exception',
                                    'CIVITAS SHARED RAILWAY FAILED', 'BUILD FAILED',
                                    'Parsing error loading recipe', 'Failed to parse recipe',
                                    "Couldn't parse element ResourceKey[minecraft:root / minecraft:loot_table]",
                                    'Error loading KubeJS script')
              if marker in text]
    passed = (exit_code == 0 and expected > 0 and counts == [expected]
              and 'BUILD SUCCESSFUL' in text and 'All dimensions are saved' in text
              and not errors)
    return {'passed': passed, 'required_tests': counts[-1] if counts else 0,
            'expected_required_tests': expected, 'errors': errors}
