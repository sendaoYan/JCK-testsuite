/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.EtchedBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main EtchedBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class EtchedBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$EtchedBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.EtchedBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.EtchedBorderUIResource b = new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource();
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(b.getEtchType() == javax.swing.border.EtchedBorder.LOWERED, "default etch type");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins != null && ins.top == 2 && ins.bottom == 2, "insets 2");
        javax.swing.plaf.BorderUIResource.EtchedBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource(javax.swing.border.EtchedBorder.RAISED);
        check(b2.getEtchType() == javax.swing.border.EtchedBorder.RAISED, "raised type");
        javax.swing.plaf.BorderUIResource.EtchedBorderUIResource b3 =
            new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource(java.awt.Color.RED, java.awt.Color.BLUE);
        check(b3.getHighlightColor() != null && b3.getShadowColor() != null, "color ctor");
        check(b.isBorderOpaque() == true, "etched opaque");
    }
}
