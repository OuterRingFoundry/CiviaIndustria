"""Validate measured workload and performance, independently of a completion marker."""
import math


def validate(report, combined=False, max_p95_ms=45.0, min_tps=19.5):
    expected = {'decorations': 10000, 'machines': 1000, 'active_furnaces': 200,
                'warehouse_controllers': 20, 'seeded_rain_cells': 500, 'measured_ticks': 1200}
    if combined:
        expected.update(fake_players=30, networks_observed=3, moving_trains=20,
                        warehouses_receiving=20, observed_active_furnaces_at_end=200, animals_with_ai=200)
    for key, value in expected.items():
        if report.get(key) != value:
            raise ValueError(f'Incomplete staging workload: {key} must equal {value}')
    for key in ('mean_ms', 'p95_ms', 'max_ms', 'observed_tps'):
        value = report.get(key)
        if isinstance(value, bool) or not isinstance(value, (float, int)) or not math.isfinite(value) or value <= 0:
            raise ValueError('Invalid staging measurement: ' + key)
    if not math.isfinite(max_p95_ms) or not math.isfinite(min_tps) or max_p95_ms <= 0 or min_tps <= 0:
        raise ValueError('Invalid staging thresholds')
    if report['p95_ms'] >= max_p95_ms or report['mean_ms'] > 50 or report['observed_tps'] < min_tps:
        raise ValueError('Staging performance target failed')
    if report['max_ms'] < max(report['p95_ms'], report['mean_ms']):
        raise ValueError('Inconsistent staging timing measurements')
    if combined:
        if report.get('cargo_conserved') is not True:
            raise ValueError('Staging cargo conservation missing')
        for key in ('minimum_train_travel', 'warehouse_items_received', 'raid_active_ticks', 'peak_raiders', 'minimum_animal_ticks', 'pollution_affected_animals'):
            value = report.get(key, 0)
            if not isinstance(value, (float, int)) or not math.isfinite(value) or value <= 0:
                raise ValueError('Missing combined staging activity: ' + key)
        if report['minimum_animal_ticks'] < 100 or report['pollution_affected_animals'] > 200:
            raise ValueError('Incomplete or inconsistent animal workload')
        if report['peak_raiders'] > 80:
            raise ValueError('Global raid budget exceeded')
    return {'passed': True, 'max_p95_ms': max_p95_ms, 'min_tps': min_tps,
            'scope': 'Synthetic server workload; real-client and GPU gates remain open'}
