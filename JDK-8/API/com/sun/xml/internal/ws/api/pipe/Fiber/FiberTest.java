/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.Fiber conformance: reflection (kind, superclass, interfaces, methods).
 * @run main FiberTest
 */

import java.lang.reflect.Modifier;


public class FiberTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.Fiber", false, FiberTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.Fiber"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.Cancelable"), "implements com.sun.xml.internal.ws.api.Cancelable");
        check(has(ifs, "com.sun.xml.internal.ws.api.ComponentRegistry"), "implements com.sun.xml.internal.ws.api.ComponentRegistry");
        check(has(ifs, "java.lang.Runnable"), "implements java.lang.Runnable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addInterceptor"), "method addInterceptor");
        check(mn.contains("addListener"), "method addListener");
        check(mn.contains("cancel"), "method cancel");
        check(mn.contains("current"), "method current");
        check(mn.contains("getCompletionCallback"), "method getCompletionCallback");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getContextClassLoader"), "method getContextClassLoader");
        check(mn.contains("getCurrentIfSet"), "method getCurrentIfSet");
        check(mn.contains("getPacket"), "method getPacket");
        check(mn.contains("getSPI"), "method getSPI");
        check(mn.contains("isStartedSync"), "method isStartedSync");
        check(mn.contains("isSynchronous"), "method isSynchronous");
        check(mn.contains("removeInterceptor"), "method removeInterceptor");
        check(mn.contains("removeListener"), "method removeListener");
        check(mn.contains("resetCont"), "method resetCont");
        check(mn.contains("resume"), "method resume");
        check(mn.contains("resumeAndReturn"), "method resumeAndReturn");
        check(mn.contains("run"), "method run");
        check(mn.contains("runSync"), "method runSync");
        check(mn.contains("setCompletionCallback"), "method setCompletionCallback");
        check(mn.contains("setContextClassLoader"), "method setContextClassLoader");
        check(mn.contains("setDeliverThrowableInPacket"), "method setDeliverThrowableInPacket");
        check(mn.contains("start"), "method start");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("owner"), "field owner");
        check(fn.contains("serializeExecution"), "field serializeExecution");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
