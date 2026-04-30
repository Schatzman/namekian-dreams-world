# Namekian Dreams World

Fabric 1.20.1 world-generation mod that adds an explicit selectable Overworld preset, `namekian_dreams_world:namekian_dreams_overworld`. The preset uses a custom chunk generator with an expanded build range (`min_y=-304`, `height=1328`, `max_y=1023`), default-on surreal fractal density regions, Namekian atmosphere effects, visual daylight dimming, and intentionally rich ore generation.

## Build

```bash
./gradlew build
```

## Diagnostics

```bash
./gradlew diagnoseFields
./gradlew diagnoseOres
```

The field diagnostic samples deterministic terrain fields and reports whether normal, amplified, extreme, cave, and fractal contributions are present with the default config. The ore diagnostic samples the expanded vertical range and reports ore coverage below vanilla bedrock, above vanilla build height, boosted ore presence, and megavein candidate presence.

## Atmosphere And Daylight

The Namekian dimension type uses `effects: namekian_dreams_world:namekian_overworld`, registered on the client through Fabric's `DimensionRenderingRegistry`. Defaults are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`; client-only mixins force sky/cloud colors for Namekian-dimension clients and force configured water fog while the camera is in water.

Actual gameplay skylight offset is represented by tested config/math (`enable_actual_sky_light_offset=true`, `outdoor_sky_light_offset=-5`, `max_sky_light_level=10`), where outdoor sky light `15 -> 10` and intermediate values subtract five with clamp. No runtime gameplay skylight mixin is installed: the attempted `BlockAndTintGetter` mixin was removed because the target is an interface and failed client mixin preparation. A future gameplay-light implementation needs a compile/runtime-safe target with reliable `Level`/dimension identity before changing sky light.

## Ore Generation

`NamekianChunkGenerator` replaces eligible generated stone/deepslate with deterministic ores from `NamekianOreSampler` across `min_y=-304` to `max_y=1023`. Defaults are deliberately rich: broad coal/iron, mid/high copper, deep-rich gold/redstone/lapis/diamond, high-mountain emerald, and default-on rare megavein/lode fields bounded by deterministic warped ellipsoid sampling.
