/*
 * @test
 * @summary javax.swing.plaf.basic.BasicBorders conformance: white-box structure + black-box behavior.
 * @run main BasicBordersTest
 */

import java.lang.reflect.Modifier;

public class BasicBordersTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicBorders");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        check(javax.swing.plaf.basic.BasicBorders.getButtonBorder() != null, "button border");
        check(javax.swing.plaf.basic.BasicBorders.getRadioButtonBorder() != null, "radio border");
        check(javax.swing.plaf.basic.BasicBorders.getToggleButtonBorder() != null, "toggle border");
        check(javax.swing.plaf.basic.BasicBorders.getMenuBarBorder() != null, "menubar border");
        check(javax.swing.plaf.basic.BasicBorders.getSplitPaneBorder() != null, "splitpane border");
        check(javax.swing.plaf.basic.BasicBorders.getSplitPaneDividerBorder() != null, "divider border");
        check(javax.swing.plaf.basic.BasicBorders.getTextFieldBorder() != null, "textfield border");
        check(javax.swing.plaf.basic.BasicBorders.getProgressBarBorder() != null, "progress border");
        check(javax.swing.plaf.basic.BasicBorders.getInternalFrameBorder() != null, "internalframe border");
        check(javax.swing.plaf.basic.BasicBorders.getButtonBorder() instanceof javax.swing.border.Border, "button border is Border");
        check(javax.swing.plaf.basic.BasicBorders.getButtonBorder().getBorderInsets(new javax.swing.JLabel()) != null, "button border insets");
    }
}
