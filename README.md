# Namekian Dreams World

Fabric 1.20.1 world-generation mod that adds an explicit selectable Overworld preset, `namekian_dreams_world:namekian_dreams_overworld`. The preset uses a custom chunk generator with an expanded build range (`min_y=-304`, `height=1328`, `max_y=1023`), default-on surreal fractal density regions, Namekian atmosphere effects, scoped skylight/client-light dimming, large lava lakes, weighted biome selection, probabilistic structure tuning, and intentionally rich ore generation.

## Build

```bash
./gradlew build
```

## Diagnostics

```bash
./gradlew diagnoseFields
./gradlew diagnoseOres
```

The field diagnostic samples deterministic generated block outcomes and reports concrete teleport coordinates for broad Y=0 oceans, deep ocean floors, high mountains, actual cave air, large lava lakes, frozen peaks, jungle/lush, dark forest, mushroom fields, birch forest, rare Nether biome regions, dripstone, deep-dark style, and gravel regions. The ore diagnostic samples generated solid terrain and reports concrete deep ore, high ore, and visible ore-rich megavein/lode coordinates.

## Atmosphere And Daylight

The Namekian dimension type uses `effects: namekian_dreams_world:namekian_overworld`, registered on the client through Fabric's `DimensionRenderingRegistry`. Defaults are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`; client-only mixins force sky/cloud colors for Namekian-dimension clients and force configured water fog while the camera is in water.

Actual gameplay skylight offset is scoped through an interface mixin on `BlockAndTintGetter#getBrightness`: when the receiver is a `Level` whose dimension effects id is `namekian_dreams_world:namekian_overworld`, only `LightLayer.SKY` is shifted by `outdoor_sky_light_offset=-5` and clamped to `max_sky_light_level=10`, so sky light `15 -> 10`. Render/client combined light is additionally adjusted by a client-only `LevelRenderer#getLightColor` mixin using the same packed-light helper; if F3 `Client Light` is sourced from another path, this still darkens rendered daylight while the F3 line may not exactly mirror the helper.

## Lava Lakes

Large lava lakes are configurable in `namekian_dreams_world.properties`: `enable_large_lava_lakes`, `lava_lake_frequency`, `lava_lake_threshold`, `deep_lava_start_y`, `lava_lake_max_radius`, and `surface_lava_lake_chance`. Defaults create broad deep lava bodies below the deep threshold, rare cave/cavern basins, and very rare exposed lava in extreme terrain. `diagnoseFields` prints a representative broad body and proves it contains multiple lava blocks.

## Biomes And Structures

The Overworld preset uses `namekian_dreams_world:namekian_biome_source` rather than vanilla `minecraft:multi_noise`. Its JSON includes all vanilla Overworld biomes plus rare `minecraft:crimson_forest`, `minecraft:warped_forest`, and `minecraft:soul_sand_valley`. Dark forest and mushroom fields are boosted, birch forest is reduced, and selected biome keys drive visible surface palettes used by diagnostics.

Vanilla 1.20.1 structure-set shapes were inspected before overriding. `data/minecraft/worldgen/structure_set/ruined_portals.json` keeps vanilla structures and salt but changes random-spread spacing from `40` to `38`, roughly 10.8% more common by area. `data/minecraft/worldgen/structure_set/villages.json` keeps vanilla village entries and salt but changes spacing from `34` to `37`, roughly 15.6% less common by area. Structure placement remains probabilistic and should be evaluated over many chunks.

## Ore Generation

`NamekianChunkGenerator` replaces eligible generated stone/deepslate with deterministic ores from `NamekianOreSampler` across `min_y=-304` to `max_y=1023`. Defaults are deliberately rich: broad coal/iron, mid/high copper, deep-rich gold/redstone/lapis/diamond, high-mountain emerald, and default-on rare megavein/lode fields bounded by deterministic warped ellipsoid sampling. `diagnoseOres` proves actual ore blocks in generated solid terrain and reports lode metadata for visible inspection.
