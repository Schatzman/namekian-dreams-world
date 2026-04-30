package io.github.namekiandreams.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class FieldMathTest {
    @Test
    void smoothstepClampsAndEasesRange() {
        assertEquals(0.0, FieldMath.smoothstep(10.0, 20.0, 4.0));
        assertEquals(1.0, FieldMath.smoothstep(10.0, 20.0, 24.0));
        assertEquals(0.5, FieldMath.smoothstep(10.0, 20.0, 15.0), 0.000001);
    }
    @Test
    void smoothstepRejectsDegenerateRange() {
        assertThrows(IllegalArgumentException.class, () -> FieldMath.smoothstep(4.0, 4.0, 4.0));
    }
    @Test
    void hashNoiseIsDeterministicAndCoordinateSensitive() {
        double first = FieldMath.signedHashNoise(7L, 1, 2, 3);
        assertEquals(first, FieldMath.signedHashNoise(7L, 1, 2, 3));
        assertNotEquals(first, FieldMath.signedHashNoise(7L, 1, 2, 4));
    }
}
