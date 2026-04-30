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

The field diagnostic samples deterministic generated block outcomes and reports concrete teleport coordinates for broad Y=0 oceans, deep ocean floors, high mountains, actual cave air, frozen peaks, jungle/lush, dripstone, deep-dark style, and gravel regions. The ore diagnostic samples generated solid terrain and reports concrete deep ore, high ore, and visible ore-rich megavein/lode coordinates.

## Atmosphere And Daylight

The Namekian dimension type uses `effects: namekian_dreams_world:namekian_overworld`, registered on the client through Fabric's `DimensionRenderingRegistry`. Defaults are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`; client-only mixins force sky/cloud colors for Namekian-dimension clients and force configured water fog while the camera is in water.

Actual gameplay skylight offset is scoped through an interface mixin on `BlockAndTintGetter#getBrightness`: when the receiver is a `Level` whose dimension effects id is `namekian_dreams_world:namekian_overworld`, only `LightLayer.SKY` is shifted by `outdoor_sky_light_offset=-5` and clamped to `max_sky_light_level=10`, so sky light `15 -> 10`. Client startup must remain the proof for this hook because the previous unsafe interface-target attempt was backed out after mixin preparation failure.

## Ore Generation

`NamekianChunkGenerator` replaces eligible generated stone/deepslate with deterministic ores from `NamekianOreSampler` across `min_y=-304` to `max_y=1023`. Defaults are deliberately rich: broad coal/iron, mid/high copper, deep-rich gold/redstone/lapis/diamond, high-mountain emerald, and default-on rare megavein/lode fields bounded by deterministic warped ellipsoid sampling. `diagnoseOres` proves actual ore blocks in generated solid terrain and reports lode metadata for visible inspection.
