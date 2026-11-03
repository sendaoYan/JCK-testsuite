/*
 * @test
 * @summary javax.swing.Popup conformance: white-box structure + black-box behavior.
 * @run main PopupTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public class PopupTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Popup");
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
        // Popup's constructors are protected; obtain one via PopupFactory.
        Popup p = PopupFactory.getSharedInstance().getPopup(new JPanel(), new JPanel(), 0, 0);
        check(p != null, "factory creates popup");
        p.hide(); // headless-safe no-op
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
