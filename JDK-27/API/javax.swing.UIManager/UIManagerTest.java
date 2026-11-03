/*
 * @test
 * @summary javax.swing.UIManager conformance: white-box structure + black-box behavior.
 * @run main UIManagerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.UIManager;

public class UIManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.UIManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(UIManager.getDefaults() != null, "getDefaults");
        check(UIManager.getLookAndFeelDefaults() != null, "getLookAndFeelDefaults");
        check(UIManager.getLookAndFeel() != null, "getLookAndFeel");
        check(UIManager.getInstalledLookAndFeels().length > 0, "installed look and feels");
        check(UIManager.getSystemLookAndFeelClassName().length() > 0, "system LAF class name");
        check(UIManager.getCrossPlatformLookAndFeelClassName().length() > 0, "cross platform LAF class name");

        UIManager.put("jck.t8.str", "value");
        check("value".equals(UIManager.get("jck.t8.str")), "put/get");
        UIManager.put("jck.t8.int", Integer.valueOf(42));
        check(UIManager.getInt("jck.t8.int") == 42, "getInt");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
