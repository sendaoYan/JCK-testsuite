/*
 * @test
 * @summary java.awt.image.ImageObserver conformance: white-box structure + constant values.
 * @run main ImageObserverTest
 */

import java.awt.image.ImageObserver;
import java.lang.reflect.*;

public class ImageObserverTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }
    static int fieldInt(Class<?> c, String name) {
        try { return c.getField(name).getInt(null); } catch (Exception e) { throw new AssertionError(name + ": " + e); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImageObserver", false, ImageObserverTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "imageUpdate", java.awt.Image.class, int.class, int.class, int.class, int.class, int.class), "imageUpdate(Image,int,int,int,int,int)");
    }

    static void testBlackBox() {
        check(fieldInt(ImageObserver.class, "WIDTH") == 1, "WIDTH 1");
        check(fieldInt(ImageObserver.class, "HEIGHT") == 2, "HEIGHT 2");
        check(fieldInt(ImageObserver.class, "PROPERTIES") == 4, "PROPERTIES 4");
        check(fieldInt(ImageObserver.class, "SOMEBITS") == 8, "SOMEBITS 8");
        check(fieldInt(ImageObserver.class, "FRAMEBITS") == 16, "FRAMEBITS 16");
        check(fieldInt(ImageObserver.class, "ALLBITS") == 32, "ALLBITS 32");
        check(fieldInt(ImageObserver.class, "ERROR") == 64, "ERROR 64");
        check(fieldInt(ImageObserver.class, "ABORT") == 128, "ABORT 128");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
