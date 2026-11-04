/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.CheckedException conformance: reflection (kind, superclass, interfaces, methods).
 * @run main CheckedExceptionTest
 */

import java.lang.reflect.Modifier;


public class CheckedExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.CheckedException", false, CheckedExceptionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.CheckedException"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBridge"), "method getBridge");
        check(mn.contains("getDetailBean"), "method getDetailBean");
        check(mn.contains("getExceptionClass"), "method getExceptionClass");
        check(mn.contains("getExceptionType"), "method getExceptionType");
        check(mn.contains("getMessageName"), "method getMessageName");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getParent"), "method getParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
