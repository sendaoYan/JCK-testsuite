/*
 * @test
 * @summary java.awt.Transparency conformance: interface reflection.
 * @run main TransparencyTest
 */

public class TransparencyTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Transparency");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Transparency.class.isInterface(), "Transparency is an interface");
        check(java.awt.Transparency.OPAQUE == 1, "OPAQUE == 1");
        check(java.awt.Transparency.BITMASK == 2, "BITMASK == 2");
        check(java.awt.Transparency.TRANSLUCENT == 3, "TRANSLUCENT == 3");
        // implementors return the right values
        check(java.awt.Color.red.getTransparency() == java.awt.Transparency.OPAQUE, "Color implements Transparency: red OPAQUE");
        check(new java.awt.Color(255, 0, 0, 128).getTransparency() == java.awt.Transparency.TRANSLUCENT,
              "translucent red");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).getTransparency()
              == java.awt.Transparency.TRANSLUCENT, "ARGB image TRANSLUCENT");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB).getTransparency()
              == java.awt.Transparency.OPAQUE, "RGB image OPAQUE");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).getColorModel()
              .getTransparency() == java.awt.Transparency.TRANSLUCENT, "ARGB color model TRANSLUCENT");
    }

}
