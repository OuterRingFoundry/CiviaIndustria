#!/usr/bin/env python3
"""Regression: launcher exit zero and a success fragment cannot certify a suite."""
import unittest
from pathlib import Path
from gametest_results import evaluate, required_test_count


class GameTestResultsTests(unittest.TestCase):
    good = 'All 39 required tests passed :)\nAll dimensions are saved\nBUILD SUCCESSFUL\n'

    def test_complete_suite(self):
        self.assertTrue(evaluate(self.good, 0, 39)['passed'])
        self.assertEqual(required_test_count(Path(__file__).resolve().parents[1] /
                         'src/main/java/com/civitasindustria/test'), 44)

    def test_empty_partial_and_duplicate_summaries(self):
        for text in (self.good.replace('39', '0'), self.good.replace('39', '34'),
                     self.good + 'All 39 required tests passed', 'required tests passed\nBUILD SUCCESSFUL'):
            with self.subTest(text=text):
                self.assertFalse(evaluate(text, 0, 39)['passed'])
        self.assertFalse(evaluate(self.good.replace('39', '0'), 0, 0)['passed'])

    def test_crash_exit_and_unsaved_world(self):
        for text, code in ((self.good, 1), (self.good.replace('All dimensions are saved', ''), 0),
                           (self.good + 'Encountered an unexpected exception', 0),
                           (self.good + 'Parsing error loading recipe', 0),
                           (self.good + "Couldn't parse element ResourceKey[minecraft:root / minecraft:loot_table]", 0),
                           (self.good.replace('BUILD SUCCESSFUL', 'BUILD FAILED'), 0)):
            with self.subTest(text=text, code=code):
                self.assertFalse(evaluate(text, code, 39)['passed'])


class StagingResultsTests(unittest.TestCase):
    def test_recorded_combined_workload_and_adversaries(self):
        import json
        from staging_results import validate
        report = json.loads((Path(__file__).resolve().parents[1] /
                             'pack/combined-staging-results.json').read_text())
        # Extend the historical timing fixture with synthetic animal counters for parser tests.
        report.update(animals_with_ai=200, minimum_animal_ticks=1400, pollution_affected_animals=200)
        self.assertTrue(validate(report, True)['passed'])
        for key, value in [('p95_ms', 45), ('mean_ms', 51), ('observed_tps', 15),
                           ('p95_ms', float('nan')), ('max_ms', float('inf')),
                           ('fake_players', 0), ('cargo_conserved', False),
                           ('minimum_train_travel', 0), ('warehouses_receiving', 19),
                           ('raid_active_ticks', 0), ('peak_raiders', 81),
                           ('animals_with_ai', 0), ('minimum_animal_ticks', 0),
                           ('pollution_affected_animals', 0), ('pollution_affected_animals', 201)]:
            with self.subTest(key=key, value=value):
                with self.assertRaises(ValueError):
                    validate({**report, key: value}, True)


if __name__ == '__main__':
    unittest.main()
