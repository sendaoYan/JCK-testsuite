/*
 * @test
 * @summary com.sun.xml.internal.ws.message.stream.StreamHeader11 conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StreamHeader11Test
 */

import java.lang.reflect.Modifier;


public class StreamHeader11Test {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.stream.StreamHeader11", false, StreamHeader11Test.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.stream.StreamHeader11"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.message.stream.StreamHeader"), "superclass com.sun.xml.internal.ws.message.stream.StreamHeader");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("processHeaderAttributes"), "method processHeaderAttributes");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("SOAP_1_1_MUST_UNDERSTAND"), "field SOAP_1_1_MUST_UNDERSTAND");
        check(fn.contains("SOAP_1_1_ROLE"), "field SOAP_1_1_ROLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
