package io.github.namekiandreams.portal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NamekianFrameValidatorTest {
    @Test
    void acceptsNetherLikeMinAndMaxRectangles() {
        assertTrue(validFrame(2, 3));
        assertTrue(validFrame(NamekianFrameValidator.MAX_WIDTH, NamekianFrameValidator.MAX_HEIGHT));
    }

    @Test
    void rejectsTooSmallOrTooLargeRectangles() {
        assertFalse(validFrame(1, 3));
        assertFalse(validFrame(2, 2));
        assertFalse(validFrame(NamekianFrameValidator.MAX_WIDTH + 1, 3));
        assertFalse(validFrame(2, NamekianFrameValidator.MAX_HEIGHT + 1));
    }

    @Test
    void rejectsBrokenFrameAndBlockedInterior() {
        assertFalse(NamekianFrameValidator.frameCellsValid(2, 3, (u, v) -> {
            if (u == -1 && v == 1) {
                return NamekianFrameValidator.CellType.INVALID;
            }
            return cellForValidFrame(2, 3, u, v);
        }));
        assertFalse(NamekianFrameValidator.frameCellsValid(2, 3, (u, v) -> {
            if (u == 0 && v == 1) {
                return NamekianFrameValidator.CellType.FRAME;
            }
            return cellForValidFrame(2, 3, u, v);
        }));
    }

    private static boolean validFrame(int width, int height) {
        return NamekianFrameValidator.frameCellsValid(width, height, (u, v) -> cellForValidFrame(width, height, u, v));
    }

    private static NamekianFrameValidator.CellType cellForValidFrame(int width, int height, int u, int v) {
        boolean inner = u >= 0 && u < width && v >= 0 && v < height;
        return inner ? NamekianFrameValidator.CellType.INNER : NamekianFrameValidator.CellType.FRAME;
    }
}
