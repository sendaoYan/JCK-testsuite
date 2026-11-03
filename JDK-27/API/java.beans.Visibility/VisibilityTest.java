/*
 * @test
 * @summary java.beans.Visibility conformance: white-box structure + black-box behavior.
 * @run main VisibilityTest
 */

import java.beans.Visibility;

public class VisibilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Visibility");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final boolean[] state = new boolean[]{true};
        Visibility v = new Visibility() {
            public boolean needsGui() { return state[0]; }
            public void dontUseGui() { state[0] = false; }
            public void okToUseGui() { state[0] = true; }
            public boolean avoidingGui() { return !state[0]; }
        };
        check(v.needsGui(), "needsGui initial");
        v.dontUseGui();
        check(v.avoidingGui(), "avoidingGui after dontUseGui");
        v.okToUseGui();
        check(v.needsGui(), "needsGui after okToUseGui");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
