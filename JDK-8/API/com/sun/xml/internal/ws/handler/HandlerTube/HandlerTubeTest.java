/*
 * @test
 * @summary com.sun.xml.internal.ws.handler.HandlerTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HandlerTubeTest
 */

import java.lang.reflect.Modifier;


public class HandlerTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.handler.HandlerTube", false, HandlerTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.handler.HandlerTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl"), "superclass com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("closeClientsideHandlers"), "method closeClientsideHandlers");
        check(mn.contains("closeServersideHandlers"), "method closeServersideHandlers");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("initiateClosing"), "method initiateClosing");
        check(mn.contains("isHandlerChainEmpty"), "method isHandlerChainEmpty");
        check(mn.contains("processException"), "method processException");
        check(mn.contains("processRequest"), "method processRequest");
        check(mn.contains("processResponse"), "method processResponse");
        check(mn.contains("resetProcessor"), "method resetProcessor");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("handlers"), "field handlers");
        check(fn.contains("port"), "field port");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
