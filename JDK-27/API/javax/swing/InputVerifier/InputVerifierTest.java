/*
 * @test
 * @summary javax.swing.InputVerifier conformance: white-box structure + black-box behavior.
 * @run main InputVerifierTest
 */

import java.lang.reflect.Modifier;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class InputVerifierTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.InputVerifier");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JComponent comp = new JPanel();
        InputVerifier v = new InputVerifier() {
            public boolean verify(JComponent c) { return c == comp; }
        };

        check(v.verify(comp), "verify true");
        check(!v.verify(new JPanel()), "verify false");

        // shouldYieldFocus defaults to the verify() result
        check(v.shouldYieldFocus(comp), "shouldYieldFocus true");
        check(!v.shouldYieldFocus(new JPanel()), "shouldYieldFocus false");

        // verifyTarget delegates to verify, tolerate null target
        check(v.verifyTarget(comp), "verifyTarget");
        check(v.shouldYieldFocus(comp, null), "shouldYieldFocus with target");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
