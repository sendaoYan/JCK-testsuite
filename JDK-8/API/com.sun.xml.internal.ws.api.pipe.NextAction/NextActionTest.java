/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.NextAction conformance: reflection (kind, superclass, interfaces, methods).
 * @run main NextActionTest
 */

import java.lang.reflect.Modifier;


public class NextActionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.NextAction", false, NextActionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.NextAction"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("abortResponse"), "method abortResponse");
        check(mn.contains("getKindString"), "method getKindString");
        check(mn.contains("getNext"), "method getNext");
        check(mn.contains("getPacket"), "method getPacket");
        check(mn.contains("getThrowable"), "method getThrowable");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("invokeAndForget"), "method invokeAndForget");
        check(mn.contains("invokeAsync"), "method invokeAsync");
        check(mn.contains("returnWith"), "method returnWith");
        check(mn.contains("setNext"), "method setNext");
        check(mn.contains("suspend"), "method suspend");
        check(mn.contains("throwException"), "method throwException");
        check(mn.contains("throwExceptionAbortResponse"), "method throwExceptionAbortResponse");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
