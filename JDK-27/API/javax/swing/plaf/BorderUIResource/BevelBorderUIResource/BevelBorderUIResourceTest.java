/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.BevelBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main BevelBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class BevelBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$BevelBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.BevelBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.BevelBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(javax.swing.border.BevelBorder.RAISED);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(b.getBevelType() == javax.swing.border.BevelBorder.RAISED, "bevel type");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins != null && ins.top >= 0, "insets non-null");
        check(b.isBorderOpaque() == true, "bevel opaque");
        javax.swing.plaf.BorderUIResource.BevelBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.RED, java.awt.Color.BLUE);
        check(b2.getBevelType() == javax.swing.border.BevelBorder.LOWERED, "2-arg ctor type");
        javax.swing.plaf.BorderUIResource.BevelBorderUIResource b3 =
            new javax.swing.plaf.BorderUIResource.BevelBorderUIResource(javax.swing.border.BevelBorder.RAISED, java.awt.Color.RED, java.awt.Color.BLUE, java.awt.Color.GREEN, java.awt.Color.WHITE);
        check(b3 != null && b3.getBorderInsets(new javax.swing.JLabel()) != null, "5-arg ctor");
    }
}
