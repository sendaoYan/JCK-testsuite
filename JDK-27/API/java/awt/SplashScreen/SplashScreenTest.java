/*
 * @test
 * @summary java.awt.SplashScreen conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SplashScreenTest
 */

import java.lang.reflect.*;

public class SplashScreenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.SplashScreen", false, SplashScreenTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.SplashScreen");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasMethod(c, "getSplashScreen"), "declares getSplashScreen");
        check(hasMethod(c, "isVisible"), "declares isVisible");
    }

    static void testBlackBox() throws Exception {
        // Without -splash:... on the command line, no splash screen is present, so
        // getSplashScreen() deterministically returns null (headful only: in headless
        // mode this method throws HeadlessException).
        java.awt.SplashScreen s = java.awt.SplashScreen.getSplashScreen();
        check(s == null, "getSplashScreen null without -splash");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
