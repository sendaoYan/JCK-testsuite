/*
 * @test
 * @summary javax.swing.LookAndFeel conformance: white-box structure + black-box behavior.
 * @run main LookAndFeelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.InputMap;
import javax.swing.LookAndFeel;

public class LookAndFeelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.LookAndFeel");
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
        LookAndFeel laf = new LookAndFeel() {
            public String getName() { return "Test"; }
            public String getID() { return "Test"; }
            public String getDescription() { return "Test LAF"; }
            public boolean isNativeLookAndFeel() { return false; }
            public boolean isSupportedLookAndFeel() { return true; }
        };
        check("Test".equals(laf.getName()), "getName");
        check("Test".equals(laf.getID()), "getID");
        check("Test LAF".equals(laf.getDescription()), "getDescription");
        check(!laf.isNativeLookAndFeel(), "isNativeLookAndFeel");
        check(laf.isSupportedLookAndFeel(), "isSupportedLookAndFeel");

        InputMap map = LookAndFeel.makeInputMap(new Object[] { "SPACE", "action" });
        check(map != null && map.size() >= 1, "makeInputMap");
        Object[] bindings = LookAndFeel.makeKeyBindings(new Object[] { "SPACE", "action" });
        check(bindings != null && bindings.length >= 1, "makeKeyBindings");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
