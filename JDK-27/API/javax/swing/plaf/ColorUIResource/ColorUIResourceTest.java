/*
 * @test
 * @summary javax.swing.plaf.ColorUIResource conformance: white-box structure + black-box behavior.
 * @run main ColorUIResourceTest
 */

import java.lang.reflect.Modifier;

public class ColorUIResourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.ColorUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Color"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.ColorUIResource c = new javax.swing.plaf.ColorUIResource(10, 20, 30);
        check(c instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(c.getRed() == 10 && c.getGreen() == 20 && c.getBlue() == 30, "rgb ctor");
        javax.swing.plaf.ColorUIResource c2 = new javax.swing.plaf.ColorUIResource(0x01020304);
        check(c2.getAlpha() == 255 && c2.getRed() == 2 && c2.getGreen() == 3 && c2.getBlue() == 4, "int rgb ctor");
        javax.swing.plaf.ColorUIResource c3 = new javax.swing.plaf.ColorUIResource(0.5f, 0.25f, 0.125f);
        check(c3.getRed() == 128 && c3.getGreen() == 64 && c3.getBlue() == 32, "float ctor");
        javax.swing.plaf.ColorUIResource c4 = new javax.swing.plaf.ColorUIResource(java.awt.Color.BLACK);
        check(c4.getRed() == 0 && c4.getGreen() == 0 && c4.getBlue() == 0, "Color copy ctor");
    }
}
