/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneUI.KeyboardUpLeftHandler conformance: white-box structure + black-box behavior.
 * @run main KeyboardUpLeftHandlerTest
 */
import java.lang.reflect.Modifier;

public class KeyboardUpLeftHandlerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardUpLeftHandler");
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
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ActionListener"), "interface 0");

        check(hasDeclaredMethod(c, "actionPerformed"), "declares method actionPerformed");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicSplitPaneUI o = new javax.swing.plaf.basic.BasicSplitPaneUI();
        javax.swing.plaf.basic.BasicSplitPaneUI.KeyboardUpLeftHandler h = o.new KeyboardUpLeftHandler();
        check(h instanceof java.awt.event.ActionListener, "implements ActionListener");
    }
}
