/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSliderUI.ComponentHandler conformance: white-box structure + black-box behavior.
 * @run main ComponentHandlerTest
 */
import java.lang.reflect.Modifier;

public class ComponentHandlerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSliderUI$ComponentHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.ComponentAdapter"), "superclass java.awt.event.ComponentAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "componentResized"), "declares method componentResized");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicSliderUI o = new javax.swing.plaf.basic.BasicSliderUI(new javax.swing.JSlider());
        javax.swing.plaf.basic.BasicSliderUI.ComponentHandler h = o.new ComponentHandler();
        check(h instanceof java.awt.event.ComponentAdapter, "extends ComponentAdapter");
    }
}
