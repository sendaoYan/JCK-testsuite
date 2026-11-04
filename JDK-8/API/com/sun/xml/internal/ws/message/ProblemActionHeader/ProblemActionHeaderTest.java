/*
 * @test
 * @summary com.sun.xml.internal.ws.message.ProblemActionHeader conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ProblemActionHeaderTest
 */

import java.lang.reflect.Modifier;


public class ProblemActionHeaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.ProblemActionHeader", false, ProblemActionHeaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.ProblemActionHeader"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.message.AbstractHeaderImpl"), "superclass com.sun.xml.internal.ws.message.AbstractHeaderImpl");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getLocalPart"), "method getLocalPart");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("readHeader"), "method readHeader");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("action"), "field action");
        check(fn.contains("av"), "field av");
        check(fn.contains("soapAction"), "field soapAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
