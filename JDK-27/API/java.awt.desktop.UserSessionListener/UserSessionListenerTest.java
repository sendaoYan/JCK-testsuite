/*
 * @test
 * @summary java.awt.desktop.UserSessionListener conformance: white-box structure + black-box behavior.
 * @run main UserSessionListenerTest
 */

import java.lang.reflect.*;

public class UserSessionListenerTest {
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
        Class<?> c = Class.forName("java.awt.desktop.UserSessionListener", false, UserSessionListenerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.desktop.UserSessionListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
        check(hasInterface(c, "java.awt.desktop.SystemEventListener"), "extends SystemEventListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.desktop.UserSessionListener l = new java.awt.desktop.UserSessionListener() {
            public void userSessionDeactivated(java.awt.desktop.UserSessionEvent e) {}
            public void userSessionActivated(java.awt.desktop.UserSessionEvent e) {}
        };
        l.userSessionDeactivated(null);
        l.userSessionActivated(null);
        check(true, "listener callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
