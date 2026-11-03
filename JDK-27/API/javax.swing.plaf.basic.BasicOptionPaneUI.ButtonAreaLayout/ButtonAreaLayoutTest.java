/*
 * @test
 * @summary javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout conformance: white-box structure + black-box behavior.
 * @run main ButtonAreaLayoutTest
 */
import java.lang.reflect.Modifier;

public class ButtonAreaLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicOptionPaneUI$ButtonAreaLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");

        check(hasDeclaredMethod(c, "setSyncAllWidths"), "declares method setSyncAllWidths");
        check(hasDeclaredMethod(c, "getSyncAllWidths"), "declares method getSyncAllWidths");
        check(hasDeclaredMethod(c, "setPadding"), "declares method setPadding");
        check(hasDeclaredMethod(c, "getPadding"), "declares method getPadding");
        check(hasDeclaredMethod(c, "setCentersChildren"), "declares method setCentersChildren");
        check(hasDeclaredMethod(c, "getCentersChildren"), "declares method getCentersChildren");
        check(hasDeclaredMethod(c, "preferredLayoutSize"), "declares method preferredLayoutSize");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout h = new javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout(true, 6);
        check(h instanceof java.awt.LayoutManager, "implements LayoutManager");
        check(h.getSyncAllWidths() == true, "syncAllWidths");
        check(h.getPadding() == 6, "padding");
        check(h.getCentersChildren() == true, "centersChildren default true");
        h.setPadding(10); check(h.getPadding() == 10, "setPadding");
        h.setCentersChildren(true); check(h.getCentersChildren() == true, "setCentersChildren");
        h.setSyncAllWidths(false); check(h.getSyncAllWidths() == false, "setSyncAllWidths");
        check(h.preferredLayoutSize(new java.awt.Container()) != null, "preferredLayoutSize");
    }
}
