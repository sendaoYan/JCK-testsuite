/*
 * @test
 * @summary javax.swing.FocusManager conformance: white-box structure + black-box behavior.
 * @run main FocusManagerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.FocusManager;

public class FocusManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.FocusManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.DefaultKeyboardFocusManager"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check("FocusManagerClassName".equals(FocusManager.FOCUS_MANAGER_CLASS_PROPERTY), "property constant");
        check(FocusManager.isFocusManagerEnabled(), "focus manager enabled by default");
        FocusManager current = FocusManager.getCurrentManager();
        check(current != null, "getCurrentManager non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
