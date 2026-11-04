/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.JavaMethod conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JavaMethodTest
 */

import java.lang.reflect.Modifier;


public class JavaMethodTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.JavaMethod", false, JavaMethodTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.JavaMethod"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getMEP"), "method getMEP");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getOperationName"), "method getOperationName");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getRequestMessageName"), "method getRequestMessageName");
        check(mn.contains("getRequestPayloadName"), "method getRequestPayloadName");
        check(mn.contains("getResponseMessageName"), "method getResponseMessageName");
        check(mn.contains("getResponsePayloadName"), "method getResponsePayloadName");
        check(mn.contains("getSEIMethod"), "method getSEIMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
