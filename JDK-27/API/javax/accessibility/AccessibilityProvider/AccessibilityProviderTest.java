/*
 * @test
 * @summary javax.accessibility.AccessibilityProvider conformance: white-box structure + black-box behavior.
 * @run main AccessibilityProviderTest
 */

import java.lang.reflect.*;

public class AccessibilityProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibilityProvider");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "activate"), "declares activate");
    }

    static void testBlackBox() throws Exception {
        final boolean[] activated = new boolean[1];
        javax.accessibility.AccessibilityProvider p = new javax.accessibility.AccessibilityProvider() {
            public String getName() { return "testProvider"; }
            public void activate() { activated[0] = true; }
        };
        check("testProvider".equals(p.getName()), "getName");
        check(!activated[0], "not activated initially");
        p.activate();
        check(activated[0], "activate sets flag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
