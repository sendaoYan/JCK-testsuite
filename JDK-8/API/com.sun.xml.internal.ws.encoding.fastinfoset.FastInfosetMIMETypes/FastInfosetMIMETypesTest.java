/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetMIMETypes conformance: reflection (kind, superclass, interfaces, methods).
 * @run main FastInfosetMIMETypesTest
 */

import java.lang.reflect.Modifier;


public class FastInfosetMIMETypesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetMIMETypes", false, FastInfosetMIMETypesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetMIMETypes"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("INFOSET"), "field INFOSET");
        check(fn.contains("SOAP_11"), "field SOAP_11");
        check(fn.contains("SOAP_12"), "field SOAP_12");
        check(fn.contains("STATEFUL_INFOSET"), "field STATEFUL_INFOSET");
        check(fn.contains("STATEFUL_SOAP_11"), "field STATEFUL_SOAP_11");
        check(fn.contains("STATEFUL_SOAP_12"), "field STATEFUL_SOAP_12");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
