/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.EmptyBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main EmptyBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class EmptyBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$EmptyBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.EmptyBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.EmptyBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.EmptyBorderUIResource(1, 2, 3, 4);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins.top == 1 && ins.left == 2 && ins.bottom == 3 && ins.right == 4, "insets");
        check(b.isBorderOpaque() == false, "not opaque");
        javax.swing.plaf.BorderUIResource.EmptyBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(5, 6, 7, 8));
        java.awt.Insets ins2 = b2.getBorderInsets(new javax.swing.JLabel());
        check(ins2.top == 5 && ins2.left == 6 && ins2.bottom == 7 && ins2.right == 8, "Insets ctor");
    }
}
