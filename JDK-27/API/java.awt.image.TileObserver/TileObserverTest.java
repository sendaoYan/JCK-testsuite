/*
 * @test
 * @summary java.awt.image.TileObserver conformance: white-box structure plus black-box behavior.
 * @run main TileObserverTest
 */

import java.awt.image.*;
import java.lang.reflect.*;

public class TileObserverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.TileObserver");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "tileUpdate", WritableRenderedImage.class, int.class, int.class, boolean.class),
              "tileUpdate(WritableRenderedImage,int,int,boolean)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] args = new int[]{-1, -1, -1};
        final boolean[] flag = {false};
        TileObserver to = new TileObserver() {
            public void tileUpdate(WritableRenderedImage w, int tx, int ty, boolean willBeWritable) {
                args[0] = tx; args[1] = ty; args[2] = willBeWritable ? 1 : 0; flag[0] = true;
            }
        };
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        to.tileUpdate(img, 2, 3, true);
        check(flag[0] && args[0] == 2 && args[1] == 3 && args[2] == 1, "tileUpdate captures args");
        to.tileUpdate(img, 0, 0, false);
        check(args[0] == 0 && args[1] == 0 && args[2] == 0, "tileUpdate captures false flag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
