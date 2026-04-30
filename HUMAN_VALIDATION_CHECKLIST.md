# Human Validation Checklist

Use a local Fabric 1.20.1 test client with this mod jar installed. The coordinates below come from `./gradlew diagnoseFields` and `./gradlew diagnoseOres` on the deterministic diagnostic seed path and prove generated block outcomes, not scalar-only fields.

1. Start Minecraft, create a new world, and select the `Namekian Dreams Overworld` preset (`namekian_dreams_world:namekian_dreams_overworld`). The preset uses the vanilla Overworld multi-noise biome source while the custom generator adds deterministic biome-region block masks for visible surfaces and underground regions.
2. Confirm the dimension loads with expanded height by opening F3 and checking Y can go below `-64` and above `320` when teleporting. Default `sea_level` is now `0`.
3. Atmosphere screenshot: in clear daylight, observe the Namekian green sky. Expected config colors are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`. Client mixins force sky and cloud colors for dimensions whose effects are `namekian_dreams_world:namekian_overworld`, and water fog is forced while the camera is in water.
4. Daylight/F3 light check: at noon (`/time set noon`), outdoor F3 sky light in the Namekian dimension should be capped by the scoped `BlockAndTintGetter#getBrightness` interface mixin: `LightLayer.SKY` value `15` maps to `10`, intermediate values subtract five, and values clamp to `0..10`. This claim is scoped to levels whose dimension effects id is `namekian_dreams_world:namekian_overworld`.
5. Water/ocean check: teleport to `/tp @p -8192 0 0`. Expected: block `minecraft:water`, a broad ocean basin around sea level `Y=0`, diagnostic proof `water_columns=49`, and generated floor around `surface_y=-144`, not a single waterfall source.
6. Deep ocean floor / abyss check: teleport to `/tp @p -8192 -295 -7680`. Expected: generated deep ocean floor near bottom, block `minecraft:packed_ice`, diagnostic `surface_y=-296`, proving deep floors can descend below `Y=-200` and toward bedrock.
7. Mountain/extreme/frozen peak check: teleport to `/tp @p -8192 897 -8192`. Expected: high frozen peak with `minecraft:snow_block`, diagnostic `surface_y=895`, and obvious terrain far above vanilla build-height expectations.
8. Cave/cavern air check: teleport in spectator mode to `/tp @p -8192 -288 -8192`. Expected: actual `minecraft:air` underground cave/cavern below generated surface, not a scalar-only cave signal.
9. Jungle/lush check: teleport to `/tp @p -8192 188 -1024`. Expected: lush/jungle-style surface with `minecraft:moss_block`, diagnostic `region=JUNGLE_LUSH`.
10. Dripstone region check: teleport to `/tp @p -7168 -96 -4096`. Expected: `minecraft:dripstone_block`, diagnostic `region=DRIPSTONE_CAVES`.
11. Deep-dark style region check: teleport to `/tp @p -7680 -96 5632`. Expected: `minecraft:tuff` or nearby sculk/tuff palette, diagnostic `region=DEEP_DARK`.
12. Gravel biome/material check: teleport to `/tp @p -7680 -60 5632`. Expected: `minecraft:gravel`, diagnostic `region=GRAVELLY`.
13. Deep ore check: run `./gradlew diagnoseOres`, then teleport to `/tp @p -4096 -296 -3648`. Expected: actual generated `minecraft:deepslate_diamond_ore` in solid deepslate/stone below vanilla bedrock.
14. High ore check: teleport to `/tp @p -4096 370 1024`. Expected: actual generated `minecraft:emerald_ore` in high terrain above vanilla build height.
15. Megavein/lode check: teleport to `/tp @p -3904 333 -3904`. Expected: visible ore-rich lode, not a void candidate. Diagnostic metadata: `cluster_ore_blocks=729`, lode `center=(-3907,308,-3895)`, `radii=(63,107,84)`, `density=1.165`, `signal=emerald_lode`.
16. Record client observations with screenshots and coordinates for any mismatch, especially sky color, F3 skylight cap, water fog, missing ocean basin, missing deep floor, absent biome-region blocks, or absent ore-rich lode.
