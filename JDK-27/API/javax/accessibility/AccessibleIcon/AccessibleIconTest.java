/*
 * @test
 * @summary javax.accessibility.AccessibleIcon conformance: white-box structure + black-box behavior.
 * @run main AccessibleIconTest
 */

import java.lang.reflect.*;

public class AccessibleIconTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleIcon");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleIconDescription"), "declares getAccessibleIconDescription");
        check(hasMethod(c, "setAccessibleIconDescription"), "declares setAccessibleIconDescription");
        check(hasMethod(c, "getAccessibleIconWidth"), "declares getAccessibleIconWidth");
        check(hasMethod(c, "getAccessibleIconHeight"), "declares getAccessibleIconHeight");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleIcon icon = new javax.accessibility.AccessibleIcon() {
            String desc = "none"; int w = 0; int h = 0;
            public String getAccessibleIconDescription() { return desc; }
            public void setAccessibleIconDescription(String d) { this.desc = d; }
            public int getAccessibleIconWidth() { return w; }
            public int getAccessibleIconHeight() { return h; }
        };
        check("none".equals(icon.getAccessibleIconDescription()), "initial description");
        check(icon.getAccessibleIconWidth() == 0 && icon.getAccessibleIconHeight() == 0, "initial dimensions 0");

        icon.setAccessibleIconDescription("Save icon");
        check("Save icon".equals(icon.getAccessibleIconDescription()), "set/get description round trip");

        // Width/height are read-only in the interface; verify they stay fixed.
        check(icon.getAccessibleIconWidth() == 0, "width stays 0");
        check(icon.getAccessibleIconHeight() == 0, "height stays 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
