/*
 * @test
 * @summary com.sun.xml.internal.ws.client.AsyncResponseImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AsyncResponseImplTest
 */

import java.lang.reflect.Modifier;


public class AsyncResponseImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.AsyncResponseImpl", false, AsyncResponseImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.AsyncResponseImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.util.concurrent.FutureTask"), "superclass java.util.concurrent.FutureTask");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.client.ResponseContextReceiver"), "implements com.sun.xml.internal.ws.client.ResponseContextReceiver");
        check(has(ifs, "javax.xml.ws.Response"), "implements javax.xml.ws.Response");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cancel"), "method cancel");
        check(mn.contains("getContext"), "method getContext");
        check(mn.contains("run"), "method run");
        check(mn.contains("set"), "method set");
        check(mn.contains("setCancelable"), "method setCancelable");
        check(mn.contains("setResponseContext"), "method setResponseContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
