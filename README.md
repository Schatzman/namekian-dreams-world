# Namekian Dreams World

Fabric 1.20.1 world-generation mod that adds an explicit selectable Overworld preset, `namekian_dreams_world:namekian_dreams_overworld`. The preset uses a custom chunk generator with an expanded build range (`min_y=-304`, `height=1328`, `max_y=1023`) and default-on surreal fractal density regions.

## Build

```bash
./gradlew build
```

## Diagnostics

```bash
./gradlew diagnoseFields
```

The diagnostic samples deterministic terrain fields and reports whether normal, amplified, extreme, cave, and fractal contributions are present with the default config.
