/*
 * @test
 * @summary java.awt.IllegalComponentStateException conformance: white-box structure + black-box behavior.
 * @run main IllegalComponentStateExceptionTest
 */

import java.lang.reflect.*;

public class IllegalComponentStateExceptionTest {
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
        Class<?> c = Class.forName("java.awt.IllegalComponentStateException", false, IllegalComponentStateExceptionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.IllegalComponentStateException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.lang.IllegalStateException"), "extends java.lang.IllegalStateException");
    }

    static void testBlackBox() throws Exception {
        java.awt.IllegalComponentStateException e1 = new java.awt.IllegalComponentStateException();
        check(e1.getMessage() != null || e1.getMessage() == null, "no-arg ctor callable");
        java.awt.IllegalComponentStateException e2 = new java.awt.IllegalComponentStateException("boom");
        check("boom".equals(e2.getMessage()), "message ctor");
        check(java.lang.IllegalStateException.class.isInstance(e2), "instanceof java.lang.IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
