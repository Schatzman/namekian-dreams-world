# Human Validation Checklist

Use a local Fabric 1.20.1 test client with this mod jar installed.

1. Start Minecraft, create a new world, and select the `Namekian Dreams Overworld` preset (`namekian_dreams_world:namekian_dreams_overworld`).
2. Confirm the dimension loads with expanded height by opening F3 and checking Y can go below `-64` and above `320` when teleporting.
3. Atmosphere check: in clear daylight, observe Namekian green sky/fog styling. Expected config colors are `sky_color=#3F9678`, `fog_color=#4FAE8A`, `water_fog_color=#24584F`, and `cloud_color=#A7D9BF`; the compile-safe client path explicitly applies the custom fog color and visual daylight dimming through registered dimension effects.
4. Water/cloud observation: find water and high cloud visibility areas; compare against vanilla Overworld. Record whether water fog and clouds visually match the expected Namekian palette or remain limited by vanilla/Fabric rendering hooks.
5. Daylight dimming check: at noon (`/time set noon`), compare perceived outdoor brightness against a vanilla Overworld. Expected visual result is darker daylight. Actual gameplay skylight mixin is intentionally not installed; the tested rule is `15 -> 10`, intermediate sky light subtracts five, and clamp is `0..10`.
6. Deep ore check: teleport to a deep solid region, for example `/tp @p 0 -220 0`, then use spectator/creative mining to inspect stone/deepslate. Expect frequent deep diamond, redstone, lapis, and gold below vanilla bedrock.
7. High ore check: teleport to high terrain or probe columns above vanilla build height, for example `/tp @p 512 420 512` and nearby high mountains. Expect high copper/iron/coal and mountain-biased emeralds where stone exists.
8. Megavein/lode check: fly through deep and mid/high stone regions in spectator mode. Rare large lodes should appear as dramatic clusters rather than single vanilla-sized veins.
9. Cave interaction check: inspect cave-adjacent regions below sea level and in high terrain; ore placement should survive cave/fractal terrain and remain bounded without obvious chunk-generation stalls.
10. Record client observations with screenshots and coordinates for any mismatch, especially sky/cloud/water color limitations or missing megavein visuals.
