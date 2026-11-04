/*
 * @test
 * @summary javax.swing.plaf.UIResource conformance: white-box structure + black-box behavior.
 * @run main UIResourceTest
 */

import java.lang.reflect.Modifier;

public class UIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.UIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        check(javax.swing.plaf.UIResource.class.isAssignableFrom(javax.swing.plaf.ColorUIResource.class), "ColorUIResource implements UIResource");
        javax.swing.plaf.UIResource r = new javax.swing.plaf.UIResource() {};
        check(r != null, "marker interface implementable");
    }
}
