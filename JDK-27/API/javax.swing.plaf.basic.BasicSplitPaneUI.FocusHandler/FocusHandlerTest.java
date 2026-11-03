/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneUI.FocusHandler conformance: white-box structure + black-box behavior.
 * @run main FocusHandlerTest
 */
import java.lang.reflect.Modifier;

public class FocusHandlerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$FocusHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.FocusAdapter"), "superclass java.awt.event.FocusAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "focusGained"), "declares method focusGained");
        check(hasDeclaredMethod(c, "focusLost"), "declares method focusLost");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicSplitPaneUI o = new javax.swing.plaf.basic.BasicSplitPaneUI();
        javax.swing.plaf.basic.BasicSplitPaneUI.FocusHandler h = o.new FocusHandler();
        check(h instanceof java.awt.event.FocusAdapter, "extends FocusAdapter");
    }
}
