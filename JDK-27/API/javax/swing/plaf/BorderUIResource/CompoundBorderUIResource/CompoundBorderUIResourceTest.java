/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.CompoundBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main CompoundBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class CompoundBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$CompoundBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.CompoundBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.border.Border outer = new javax.swing.border.LineBorder(java.awt.Color.RED, 2);
        javax.swing.border.Border inner = new javax.swing.border.EmptyBorder(1, 2, 3, 4);
        javax.swing.plaf.BorderUIResource.CompoundBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.CompoundBorderUIResource(outer, inner);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(b.getOutsideBorder() == outer, "outside border");
        check(b.getInsideBorder() == inner, "inside border");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins.top == 3 && ins.left == 4 && ins.bottom == 5 && ins.right == 6, "compound insets");
    }
}
