/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.MatteBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main MatteBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class MatteBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$MatteBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.MatteBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.MatteBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.MatteBorderUIResource(1, 2, 3, 4, java.awt.Color.RED);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins.top == 1 && ins.left == 2 && ins.bottom == 3 && ins.right == 4, "insets");
        check(java.awt.Color.RED.equals(b.getMatteColor()), "matte color");
        check(b.getTileIcon() == null, "no tile icon for color");
        javax.swing.plaf.BorderUIResource.MatteBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.MatteBorderUIResource(new javax.swing.ImageIcon());
        check(b2.getTileIcon() != null, "icon ctor");
    }
}
