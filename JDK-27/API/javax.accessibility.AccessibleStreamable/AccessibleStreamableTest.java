/*
 * @test
 * @summary javax.accessibility.AccessibleStreamable conformance: white-box structure + black-box behavior.
 * @run main AccessibleStreamableTest
 */

import java.lang.reflect.*;
import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AccessibleStreamableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleStreamable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getStream"), "declares getStream");
    }

    static void testBlackBox() throws Exception {
        final DataFlavor[] flavors = new DataFlavor[]{
            new DataFlavor("text/plain; charset=UTF-8", "plain text"),
            new DataFlavor("image/png", "png image")
        };
        final InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        javax.accessibility.AccessibleStreamable s = new javax.accessibility.AccessibleStreamable() {
            public DataFlavor[] getMimeTypes() { return flavors; }
            public InputStream getStream(DataFlavor f) { return stream; }
        };

        DataFlavor[] got = s.getMimeTypes();
        check(got.length == 2, "mimeTypes length 2");
        check(got[0] == flavors[0], "mimeTypes[0] identity");
        check(s.getStream(flavors[0]) == stream, "getStream returns same stream");

        javax.accessibility.AccessibleStreamable empty = new javax.accessibility.AccessibleStreamable() {
            public DataFlavor[] getMimeTypes() { return new DataFlavor[0]; }
            public InputStream getStream(DataFlavor f) { return null; }
        };
        check(empty.getMimeTypes().length == 0, "empty mimeTypes");
        check(empty.getStream(null) == null, "null stream allowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
