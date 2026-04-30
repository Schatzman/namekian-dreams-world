package io.github.namekiandreams.portal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PortalDiagnostics {
    private static final Path DIMENSION_JSON = Path.of(
            "src/main/resources/data/namekian_dreams_world/dimension/namekian_dreams.json");
    private static final Path PRESET_JSON = Path.of(
            "src/main/resources/data/namekian_dreams_world/worldgen/world_preset/namekian_dreams_overworld.json");
    private static final String GENERATOR = "namekian_dreams_world:namekian_chunk_generator";
    private static final String BIOME_SOURCE = "namekian_dreams_world:namekian_biome_source";
    private static final String DIMENSION_TYPE = "namekian_dreams_world:namekian_overworld";

    private PortalDiagnostics() {
    }

    public static void main(String[] args) throws IOException {
        DiagnosticResult result = run();
        System.out.println(result.toReport());
        if (!result.acceptancePassed()) {
            throw new IllegalStateException("Portal diagnostic did not observe every required portal signal");
        }
    }

    public static DiagnosticResult run() throws IOException {
        String dimension = Files.readString(DIMENSION_JSON);
        String preset = Files.readString(PRESET_JSON);
        boolean dimensionValid = dimension.contains(DIMENSION_TYPE)
                && dimension.contains(GENERATOR)
                && dimension.contains(BIOME_SOURCE)
                && dimension.contains("minecraft:crimson_forest")
                && !dimension.contains("minecraft:fixed")
                && !dimension.contains("\"seed\"");
        boolean presetPreserved = preset.contains("\"minecraft:overworld\"")
                && preset.contains(GENERATOR)
                && preset.contains(BIOME_SOURCE);
        boolean minFrame = validFrame(2, 3);
        boolean maxFrame = validFrame(NamekianFrameValidator.MAX_WIDTH, NamekianFrameValidator.MAX_HEIGHT);
        boolean brokenFrameRejected = !NamekianFrameValidator.frameCellsValid(2, 3, (u, v) -> {
            if (u == -1 && v == 1) {
                return NamekianFrameValidator.CellType.INVALID;
            }
            return (u >= 0 && u < 2 && v >= 0 && v < 3)
                    ? NamekianFrameValidator.CellType.INNER
                    : NamekianFrameValidator.CellType.FRAME;
        });
        return new DiagnosticResult(dimensionValid, presetPreserved, minFrame, maxFrame, brokenFrameRejected);
    }

    private static boolean validFrame(int width, int height) {
        return NamekianFrameValidator.frameCellsValid(width, height, (u, v) -> {
            boolean inner = u >= 0 && u < width && v >= 0 && v < height;
            return inner ? NamekianFrameValidator.CellType.INNER : NamekianFrameValidator.CellType.FRAME;
        });
    }

    public record DiagnosticResult(boolean dimensionResourceValid, boolean worldPresetPreserved,
                                   boolean minFrameValid, boolean maxFrameValid, boolean brokenFrameRejected) {
        public boolean acceptancePassed() {
            return dimensionResourceValid && worldPresetPreserved && minFrameValid && maxFrameValid && brokenFrameRejected;
        }

        public String toReport() {
            return "Namekian Dreams portal diagnostic: dimension=namekian_dreams_world:namekian_dreams"
                    + ", dimension_resource_valid=" + dimensionResourceValid
                    + ", overworld_preset_preserved=" + worldPresetPreserved
                    + ", frame_examples=min_2x3:" + minFrameValid + " max_21x21:" + maxFrameValid
                    + ", broken_frame_rejected=" + brokenFrameRejected
                    + ", activation_item=minecraft:fire_charge"
                    + ", coordinate_scale=1.0"
                    + ", portal_color=#077563"
                    + ", future_1_21_activation=minecraft:wind_charge_candidate";
        }
    }
}
