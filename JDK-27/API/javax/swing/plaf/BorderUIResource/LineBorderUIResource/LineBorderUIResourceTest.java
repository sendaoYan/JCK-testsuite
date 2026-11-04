/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.LineBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main LineBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class LineBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$LineBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.LineBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.LineBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.LineBorderUIResource(java.awt.Color.RED);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(java.awt.Color.RED.equals(b.getLineColor()), "line color");
        check(b.getThickness() == 1, "default thickness 1");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins != null && ins.top == 1 && ins.left == 1 && ins.bottom == 1 && ins.right == 1, "insets 1");
        javax.swing.plaf.BorderUIResource.LineBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.LineBorderUIResource(java.awt.Color.BLUE, 3);
        check(b2.getThickness() == 3, "thickness ctor");
        check(b2.getBorderInsets(new javax.swing.JLabel()).top == 3, "insets 3");
    }
}
