/*
 * @test
 * @summary java.awt.AWTException conformance: white-box structure + black-box behavior.
 * @run main AWTExceptionTest
 */

import java.lang.reflect.*;

public class AWTExceptionTest {
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
        Class<?> c = Class.forName("java.awt.AWTException", false, AWTExceptionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.AWTException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
    }

    static void testBlackBox() throws Exception {
        java.awt.AWTException e = new java.awt.AWTException("msg");
        check(e.getMessage() != null, "getMessage non-null");
        java.awt.AWTException e2 = new java.awt.AWTException("msg");
        check(e.equals(e2) || !e.equals(e2), "equals callable");
        check(e.toString() != null, "toString non-null");
        check(java.lang.Exception.class.isInstance(e), "instanceof java.lang.Exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        java.awt.AWTException ex = new java.awt.AWTException("why");
        check(ex.getMessage().equals("why"), "AWTException message preserved");
        check(ex instanceof java.lang.Exception, "AWTException checked Exception");
        check(ex instanceof java.io.Serializable, "Exception is Serializable");
        check(ex.getCause() == null, "no cause");
        java.awt.AWTException empty = new java.awt.AWTException("");
        check(empty.getMessage().equals(""), "empty message");
        check(ex.toString().startsWith("java.awt.AWTException"), "toString class-prefixed: " + ex);
        // declaring code must handle it: verify via a throwing method
        try {
            throwIfTrue(true);
            check(false, "unreachable");
        } catch (java.awt.AWTException e) {
            check(e.getMessage().equals("expected"), "caught declared AWTException");
        }
    }
    static void throwIfTrue(boolean b) throws java.awt.AWTException {
        if (b) throw new java.awt.AWTException("expected");
    }

}
