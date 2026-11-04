/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.soap.SerializerConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SerializerConstantsTest
 */

import java.lang.reflect.Modifier;


public class SerializerConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.soap.SerializerConstants", false, SerializerConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.soap.SerializerConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("DONT_ENCODE_TYPE"), "field DONT_ENCODE_TYPE");
        check(fn.contains("DONT_SERIALIZE_AS_REF"), "field DONT_SERIALIZE_AS_REF");
        check(fn.contains("ENCODE_TYPE"), "field ENCODE_TYPE");
        check(fn.contains("NOT_NULLABLE"), "field NOT_NULLABLE");
        check(fn.contains("NOT_REFERENCEABLE"), "field NOT_REFERENCEABLE");
        check(fn.contains("NULLABLE"), "field NULLABLE");
        check(fn.contains("REFERENCEABLE"), "field REFERENCEABLE");
        check(fn.contains("REFERENCED_INSTANCE"), "field REFERENCED_INSTANCE");
        check(fn.contains("SERIALIZE_AS_REF"), "field SERIALIZE_AS_REF");
        check(fn.contains("UNREFERENCED_INSTANCE"), "field UNREFERENCED_INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
