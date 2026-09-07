# Resource and ecology balance record

This is a reproducible DEV balance baseline, not a completed multiplayer economy playtest.
The measurements are in [ecology-balance-results.json](../pack/ecology-balance-results.json).
Run `./gradlew --no-daemon ecologyBalanceReport` on the designated build server to reproduce
the chemistry experiment. Its settings are explicitly recorded; server configuration
overrides need a matching experiment before comparing results.

## Mineral regions

Vanilla ore generation remains available. The additional iron, copper and gold deposits
now select mineral regions from the world seed and a separate salt for each ore. A region
is 16×16 chunks (256×256 blocks); approximately 25% qualify for each mineral. This makes
bonus deposits geographically clustered instead of equally likely in every chunk.

Within eligible regions, iron/copper get a placement attempt on average once per six
chunks, and gold once per twelve. The world-average attempt rates remain approximately
1/24 and 1/48 respectively, matching the previous bonus supply. These are attempts, not
guaranteed veins: stone/deepslate replacement, air exposure, biome and height rules still
apply. Existing chunks are unchanged; the change applies to newly generated terrain.
No retrogen, tick scan, cached world reference or additional network packet is introduced.

The domain test samples 10,000 regions, checking coverage, independent seeds/minerals,
negative-coordinate boundaries and extreme inputs. The server GameTest loads all three
placed features from the data registry, round-trips the registered modifier codec and
checks placement decisions and random-source preservation across 1,024 regions per ore.
Player surveying, mining time and trade value still need terrain/economy playtests.

## Consolidated iron production

For sustained raw-iron processing with adequate supplies:

| Quantity | One Civitas factory | Sixteen vanilla furnaces |
| --- | ---: | ---: |
| Ingots per 200 ticks | 16 | 16 |
| Coal per 16 ingots | 2 | 2 |
| Processing block entities | 1 | 16 |
| Raw industrial load | 40 | 160 |
| PM per 200 ticks | 2.0 | 6.4 |
| SOX per 200 ticks | 0.4 | 1.6 |

The factory reduces machine count, load and emissions at equal theoretical throughput
and fuel efficiency. Calibration, foundation and construction costs are additional.
This table does not measure CPU savings, transport throughput or the cost of an IE
power network. Powered IE fixtures separately verify real native processing and ports:
the arc furnace turns one iron ore into two ingots and one slag using 102,400 energy,
while the diesel generator consumes biodiesel and supplies a native HV capacitor.

## Natural recovery experiment

Each case emits for one hour, then stops emissions for twelve hours. Rain is continuous,
surface water is level and connected, and 17×17 cells remain active. Pollutants transported
outside that window remain stored and are counted in total mass. No pollutants or health
values are reset after shutdown. This controlled scenario represents sustained wet
industrial exposure, not a prediction for every biome or weather pattern.

| Central-cell measure | Factory after production | Factory after 12h recovery | 16 furnaces after production | 16 furnaces after 12h recovery |
| --- | ---: | ---: | ---: | ---: |
| AQI | 10.67 | 0.00 | 35.57 | 0.00 |
| Water quality /100 | 75.74 | 100.00 | 48.36 | 100.00 |
| Vegetation health | 10.64% | 97.51% | 0.04% | 91.83% |
| Biodiversity | 19.84% | 89.26% | 0.31% | 73.32% |

Peak aggregate degradation is 67.87% for the factory and 83.57% for the furnace group.
Neither case returns every health measure to 95% within twelve hours. The report uses
`-1` for that unreached threshold. Total stored pollutant mass decreases after shutdown;
transport outside the active area is retained rather than silently discarded.

The defaults therefore give persistent ecological consequences without ordinary factory
air exceeding the default severe-exposure AQI threshold of 500 in this scenario. Paid
remediation and stopping pollution remain useful; clean air alone does not immediately
restore biodiversity. Whether these recovery times are enjoyable still needs player
feedback. No balance rates were changed to manufacture a passing result.
