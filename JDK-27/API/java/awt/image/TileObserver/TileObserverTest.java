/*
 * @test
 * @summary java.awt.image.TileObserver conformance: white-box structure plus black-box behavior.
 * @run main TileObserverTest
 */

import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.image.BufferedImage;
import java.awt.image.TileObserver;
import java.awt.image.WritableRenderedImage;

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
        passed += TileObserverDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class TileObserverDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    /** Records every tileUpdate callback verbatim. */
    static class RecordingObserver implements TileObserver {
        WritableRenderedImage lastSource;
        int lastTx = -99, lastTy = -99;
        Boolean lastWritable;
        int calls;
        public void tileUpdate(WritableRenderedImage src, int tx, int ty, boolean writable) {
            calls++;
            lastSource = src;
            lastTx = tx;
            lastTy = ty;
            lastWritable = writable;
        }
    }



    static int run() throws Exception {

                Class<?> c = Class.forName("java.awt.image.TileObserver");
                check(c.isInterface(), "is interface");
                check(c.getSuperclass() == null, "no superclass");
                check(c.getInterfaces().length == 0, "no direct interfaces");
                check(hasMethod(c, "tileUpdate"), "method tileUpdate");
                // Exactly one abstract method with signature (WritableRenderedImage, int, int, boolean) -> void.
                java.lang.reflect.Method m = c.getMethod("tileUpdate",
                        WritableRenderedImage.class, int.class, int.class, boolean.class);
                check(m.getReturnType() == void.class && java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                      "tileUpdate(WritableRenderedImage,int,int,boolean) is abstract and returns void");
                // The observer is a plain callback: invoking it directly records arguments.
                RecordingObserver obs = new RecordingObserver();
                WritableRenderedImage src = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
                obs.tileUpdate(src, 3, 2, true);
                check(obs.calls == 1 && obs.lastSource == src, "callback receives source image");
                check(obs.lastTx == 3 && obs.lastTy == 2, "callback receives tile indices");
                check(obs.lastWritable == Boolean.TRUE, "callback receives writable=true");
                obs.tileUpdate(src, 0, 0, false);
                check(obs.calls == 2 && obs.lastWritable == Boolean.FALSE, "callback receives writable=false");
                // Registration on a WritableRenderedImage is accepted and repeatable.
                src.addTileObserver(obs);
                src.addTileObserver(obs);
                check(true, "addTileObserver accepted twice");
                src.removeTileObserver(obs);
                src.removeTileObserver(obs);
                check(true, "removeTileObserver accepted twice");

        return passed;
    }
}
