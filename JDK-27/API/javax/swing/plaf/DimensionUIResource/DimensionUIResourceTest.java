/*
 * @test
 * @summary javax.swing.plaf.DimensionUIResource conformance: white-box structure + black-box behavior.
 * @run main DimensionUIResourceTest
 */

import java.lang.reflect.Modifier;

public class DimensionUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.DimensionUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Dimension"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.DimensionUIResource d = new javax.swing.plaf.DimensionUIResource(30, 40);
        check(d instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(d.width == 30 && d.height == 40, "width/height");
        d.width = 55;
        check(d.width == 55, "mutable");
    }
}
