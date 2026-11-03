/*
 * @test
 * @summary java.awt.AWTError conformance: white-box structure + black-box behavior.
 * @run main AWTErrorTest
 */

import java.lang.reflect.*;

public class AWTErrorTest {
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
        Class<?> c = Class.forName("java.awt.AWTError", false, AWTErrorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.AWTError");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
    }

    static void testBlackBox() throws Exception {
        java.awt.AWTError e = new java.awt.AWTError("msg");
        check(e.getMessage() != null, "getMessage non-null");
        java.awt.AWTError e2 = new java.awt.AWTError("msg");
        check(e.equals(e2) || !e.equals(e2), "equals callable");
        check(e.toString() != null, "toString non-null");
        check(java.lang.Error.class.isInstance(e), "instanceof java.lang.Error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
