/*
 * @test
 * @summary com.sun.xml.internal.ws.client.sei.SEIStub conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SEIStubTest
 */

import java.lang.reflect.Modifier;


public class SEIStubTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.sei.SEIStub", false, SEIStubTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.sei.SEIStub"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.client.Stub"), "superclass com.sun.xml.internal.ws.client.Stub");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.reflect.InvocationHandler"), "implements java.lang.reflect.InvocationHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("doProcess"), "method doProcess");
        check(mn.contains("doProcessAsync"), "method doProcessAsync");
        check(mn.contains("getOperationDispatcher"), "method getOperationDispatcher");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("setOutboundHeaders"), "method setOutboundHeaders");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("seiModel"), "field seiModel");
        check(fn.contains("soapVersion"), "field soapVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
