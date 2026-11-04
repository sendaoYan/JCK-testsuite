/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource.TitledBorderUIResource conformance: white-box structure + black-box behavior.
 * @run main TitledBorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class TitledBorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource$TitledBorderUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.TitledBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.BorderUIResource.TitledBorderUIResource b =
            new javax.swing.plaf.BorderUIResource.TitledBorderUIResource("Title");
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        check("Title".equals(b.getTitle()), "title");
        check(b.getBorder() != null, "default border");
        check(b.getBorderInsets(new javax.swing.JLabel()) != null, "insets non-null");
        check(b.isBorderOpaque() == false, "titled not opaque");
        javax.swing.border.Border base = new javax.swing.border.LineBorder(java.awt.Color.BLACK);
        javax.swing.plaf.BorderUIResource.TitledBorderUIResource b2 =
            new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(base, "T", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12), java.awt.Color.RED);
        check(b2.getBorder() == base, "base border");
        check(b2.getTitlePosition() == javax.swing.border.TitledBorder.TOP, "title position");
        check(b2.getTitleJustification() == javax.swing.border.TitledBorder.CENTER, "justification");
        check(java.awt.Color.RED.equals(b2.getTitleColor()), "title color");
    }
}
