/*
 * @test
 * @summary javax.swing.InternalFrameFocusTraversalPolicy conformance: white-box structure + black-box behavior.
 * @run main InternalFrameFocusTraversalPolicyTest
 */

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Modifier;

import javax.swing.InternalFrameFocusTraversalPolicy;
import javax.swing.JInternalFrame;

public class InternalFrameFocusTraversalPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.InternalFrameFocusTraversalPolicy");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.FocusTraversalPolicy"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        InternalFrameFocusTraversalPolicy p = new InternalFrameFocusTraversalPolicy() {
            public Component getComponentAfter(Container c, Component a) { return null; }
            public Component getComponentBefore(Container c, Component a) { return null; }
            public Component getFirstComponent(Container c) { return null; }
            public Component getLastComponent(Container c) { return null; }
            public Component getDefaultComponent(Container c) { return null; }
        };

        JInternalFrame frame = new JInternalFrame("title");
        // empty frame has no initial focus owner
        check(p.getInitialComponent(frame) == null, "initial component null for empty frame");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
