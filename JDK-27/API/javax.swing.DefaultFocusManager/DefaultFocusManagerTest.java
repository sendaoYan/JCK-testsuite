/*
 * @test
 * @summary javax.swing.DefaultFocusManager conformance: white-box structure + black-box behavior.
 * @run main DefaultFocusManagerTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;

import javax.swing.DefaultFocusManager;
import javax.swing.JButton;
import javax.swing.JPanel;

public class DefaultFocusManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultFocusManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.FocusManager"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultFocusManager fm = new DefaultFocusManager();

        JPanel cont = new JPanel();
        Component b1 = new JButton("a");
        Component b2 = new JButton("b");
        cont.add(b1);
        cont.add(b2);

        // z-order comparison is observable without a display
        check(fm.compareTabOrder(b1, b2), "compareTabOrder a before b");
        check(!fm.compareTabOrder(b2, b1), "compareTabOrder b not before a");

        // traversal helpers are callable; non-displayable containers yield null
        check(fm.getFirstComponent(cont) == null, "first component null when not displayable");
        check(fm.getLastComponent(cont) == null, "last component null when not displayable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
