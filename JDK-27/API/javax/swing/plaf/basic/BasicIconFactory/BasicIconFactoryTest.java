/*
 * @test
 * @summary javax.swing.plaf.basic.BasicIconFactory conformance: white-box structure + black-box behavior.
 * @run main BasicIconFactoryTest
 */

import java.lang.reflect.Modifier;

public class BasicIconFactoryTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicIconFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        check(javax.swing.plaf.basic.BasicIconFactory.getMenuItemCheckIcon() != null, "menu check icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getMenuItemArrowIcon() != null, "menu item arrow icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getMenuArrowIcon() != null, "menu arrow icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getCheckBoxIcon() != null, "checkbox icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getRadioButtonIcon() != null, "radio icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getCheckBoxMenuItemIcon() != null, "checkbox menu icon");
        check(javax.swing.plaf.basic.BasicIconFactory.getRadioButtonMenuItemIcon() != null, "radio menu icon");
        javax.swing.Icon empty = javax.swing.plaf.basic.BasicIconFactory.createEmptyFrameIcon();
        check(empty != null, "empty frame icon");
        javax.swing.Icon cb = javax.swing.plaf.basic.BasicIconFactory.getCheckBoxIcon();
        check(cb.getIconWidth() > 0 && cb.getIconHeight() > 0, "checkbox icon has size");
    }
}
