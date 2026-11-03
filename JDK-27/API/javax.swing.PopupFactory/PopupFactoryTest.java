/*
 * @test
 * @summary javax.swing.PopupFactory conformance: white-box structure + black-box behavior.
 * @run main PopupFactoryTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public class PopupFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.PopupFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PopupFactory f = PopupFactory.getSharedInstance();
        check(f != null, "shared instance");
        check(PopupFactory.getSharedInstance() == f, "shared singleton");

        PopupFactory custom = new PopupFactory();
        PopupFactory.setSharedInstance(custom);
        check(PopupFactory.getSharedInstance() == custom, "setSharedInstance");

        Popup p = custom.getPopup(new JPanel(), new JPanel(), 0, 0);
        check(p != null, "getPopup non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
