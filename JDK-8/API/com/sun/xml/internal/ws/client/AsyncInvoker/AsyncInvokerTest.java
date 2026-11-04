/*
 * @test
 * @summary com.sun.xml.internal.ws.client.AsyncInvoker conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AsyncInvokerTest
 */

import java.lang.reflect.Modifier;


public class AsyncInvokerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.AsyncInvoker", false, AsyncInvokerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.AsyncInvoker"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Runnable"), "implements java.lang.Runnable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("do_run"), "method do_run");
        check(mn.contains("getResponseImpl"), "method getResponseImpl");
        check(mn.contains("isNonNullAsyncHandlerGiven"), "method isNonNullAsyncHandlerGiven");
        check(mn.contains("run"), "method run");
        check(mn.contains("setNonNullAsyncHandlerGiven"), "method setNonNullAsyncHandlerGiven");
        check(mn.contains("setReceiver"), "method setReceiver");
        check(mn.contains("setResponseImpl"), "method setResponseImpl");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("nonNullAsyncHandlerGiven"), "field nonNullAsyncHandlerGiven");
        check(fn.contains("responseImpl"), "field responseImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
