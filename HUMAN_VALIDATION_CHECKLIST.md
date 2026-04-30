# Human Validation Checklist

Use a local Fabric 1.20.1 test client with this mod jar installed. The diagnostics below use the deterministic diagnostic seed path and are intended as concrete places to inspect if your playtest seed has similar symptoms.

1. Start Minecraft, create a new world, and select the `Namekian Dreams Overworld` preset (`namekian_dreams_world:namekian_dreams_overworld`). The preset now uses the vanilla Overworld multi-noise biome source instead of fixed plains.
2. Confirm the dimension loads with expanded height by opening F3 and checking Y can go below `-64` and above `320` when teleporting.
3. Atmosphere screenshot: in clear daylight, observe the Namekian green sky. Expected config colors are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`. Client mixins force sky and cloud colors for dimensions whose effects are `namekian_dreams_world:namekian_overworld`, and water fog is forced while the camera is in water.
4. Daylight/F3 light caveat: at noon (`/time set noon`), outdoor F3 sky light may still report vanilla `15`. The unsafe gameplay skylight mixin was removed after client mixin-prep failure; this build only claims visual sky/fog/cloud/water atmosphere changes plus the tested skylight offset math helper.
5. Water/ocean check: run `./gradlew diagnoseFields` and use the printed `water=/tp @p ...` coordinate. Teleport there and verify an open water basin/ocean surface near sea level.
6. Mountain/extreme check: use the printed `mountain=/tp @p ...` and `extreme=/tp @p ...` coordinates. Teleport there and verify terrain rises above vanilla build-height expectations, with obvious non-plains relief nearby.
7. Cave/cavern check: use the printed `cave=/tp @p ...` coordinate. Teleport in spectator mode and verify actual underground air exists below the generated surface, not just scalar diagnostic output.
8. Deep ore check: teleport to a deep solid region near the printed cave or megavein diagnostics, then inspect stone/deepslate. Expect frequent deep diamond, redstone, lapis, and gold below vanilla bedrock.
9. High ore check: teleport to high terrain near the printed mountain coordinate. Expect high copper/iron/coal and mountain-biased emeralds where stone exists.
10. Megavein/lode check: run `./gradlew diagnoseOres`; use the reported megavein presence as the bounded proof, then fly through nearby deep and mid/high stone regions in spectator mode. Rare large lodes should appear as dramatic clusters rather than single vanilla-sized veins.
11. Record client observations with screenshots and coordinates for any mismatch, especially sky color, F3 skylight, water fog, missing water basins, missing mountain terrain, or absent cave air.
