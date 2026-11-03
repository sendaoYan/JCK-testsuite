/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSliderUI.ActionScroller conformance: white-box structure + black-box behavior.
 * @run main ActionScrollerTest
 */
import java.lang.reflect.Modifier;

public class ActionScrollerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSliderUI$ActionScroller");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractAction"), "superclass javax.swing.AbstractAction");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "actionPerformed"), "declares method actionPerformed");
        check(hasDeclaredMethod(c, "isEnabled"), "declares method isEnabled");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicSliderUI o = new javax.swing.plaf.basic.BasicSliderUI(new javax.swing.JSlider());
        javax.swing.plaf.basic.BasicSliderUI.ActionScroller h = o.new ActionScroller(new javax.swing.JSlider(), 1, true);
        check(h instanceof javax.swing.AbstractAction, "extends AbstractAction");
    }
}
