/*
 * @test
 * @summary java.awt.datatransfer.FlavorEvent conformance: white-box structure + black-box behavior.
 * @run main FlavorEventTest
 */

import java.lang.reflect.*;

public class FlavorEventTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.FlavorEvent", false, FlavorEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.FlavorEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends EventObject");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.Clipboard cb = new java.awt.datatransfer.Clipboard("evt");
        java.awt.datatransfer.FlavorEvent e = new java.awt.datatransfer.FlavorEvent(cb);
        check(e.getSource() == cb, "getSource is clipboard");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
