/*
 * @test
 * @summary java.awt.image.ImageObserver conformance: white-box structure + constant values.
 * @run main ImageObserverTest
 */

import java.awt.image.ImageObserver;
import java.lang.reflect.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

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
        passed += ImageObserverDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ImageObserverDeepContract {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImageObserver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "imageUpdate"), "method imageUpdate");
    }

    static void testConstants() throws Exception {
        // The eight observer flags are distinct powers of two (bitwise combinable).
        Class<?> c = Class.forName("java.awt.image.ImageObserver");
        check(ImageObserver.WIDTH == 1, "WIDTH == 1");
        check(ImageObserver.HEIGHT == 2, "HEIGHT == 2");
        check(ImageObserver.PROPERTIES == 4, "PROPERTIES == 4");
        check(ImageObserver.SOMEBITS == 8, "SOMEBITS == 8");
        check(ImageObserver.FRAMEBITS == 16, "FRAMEBITS == 16");
        check(ImageObserver.ALLBITS == 32, "ALLBITS == 32");
        check(ImageObserver.ERROR == 64, "ERROR == 64");
        check(ImageObserver.ABORT == 128, "ABORT == 128");
        int all = ImageObserver.WIDTH | ImageObserver.HEIGHT | ImageObserver.PROPERTIES
                | ImageObserver.SOMEBITS | ImageObserver.FRAMEBITS | ImageObserver.ALLBITS
                | ImageObserver.ERROR | ImageObserver.ABORT;
        check(all == 255, "flags occupy the low 8 bits");
        check(c.getField("WIDTH").getModifiers()
                == (java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC
                    | java.lang.reflect.Modifier.FINAL), "WIDTH is public static final");
        // Each constant is declared on the interface itself.
        check(c.getField("ABORT").getDeclaringClass() == c, "ABORT declared on ImageObserver");
    }

    static class RecordingObserver implements ImageObserver {
        int calls;
        public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
            calls++;
            return false;
        }
    }

    static void testObserverUsage() {
        // A fully loaded BufferedImage answers width/height/property queries without
        // ever invoking the observer (all data is already available).
        BufferedImage bi = new BufferedImage(7, 5, BufferedImage.TYPE_INT_RGB);
        RecordingObserver obs = new RecordingObserver();
        check(bi.getWidth(obs) == 7, "getWidth with observer");
        check(bi.getHeight(null) == 5, "getHeight with null observer");
        check(obs.calls == 0, "no imageUpdate callback for loaded image");
        check(bi.getProperty("missing", obs) == Image.UndefinedProperty,
              "unknown property -> Image.UndefinedProperty");
        check(obs.calls == 0, "property query still needs no callback");
        // imageUpdate returning false asks to stop further notifications.
        check(!new RecordingObserver().imageUpdate(null, ImageObserver.WIDTH, 0, 0, 7, 5),
              "imageUpdate return value is the continuation vote");
    }



    static int run() throws Exception {
        testWhiteBox();
        testConstants();
        testObserverUsage();
        return passed;
    }
}
