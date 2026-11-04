/*
 * @test
 * @summary com.sun.xml.internal.ws.message.stream.StreamHeader conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StreamHeaderTest
 */

import java.lang.reflect.Modifier;


public class StreamHeaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.stream.StreamHeader", false, StreamHeaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.stream.StreamHeader"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.message.AbstractHeaderImpl"), "superclass com.sun.xml.internal.ws.message.AbstractHeaderImpl");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getLocalPart"), "method getLocalPart");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getRole"), "method getRole");
        check(mn.contains("isIgnorable"), "method isIgnorable");
        check(mn.contains("isRelay"), "method isRelay");
        check(mn.contains("processHeaderAttributes"), "method processHeaderAttributes");
        check(mn.contains("readAsEPR"), "method readAsEPR");
        check(mn.contains("readHeader"), "method readHeader");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("_isMustUnderstand"), "field _isMustUnderstand");
        check(fn.contains("_isRelay"), "field _isRelay");
        check(fn.contains("_localName"), "field _localName");
        check(fn.contains("_mark"), "field _mark");
        check(fn.contains("_namespaceURI"), "field _namespaceURI");
        check(fn.contains("_role"), "field _role");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
