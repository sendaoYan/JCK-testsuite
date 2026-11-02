/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.policy.EncodingConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EncodingConstantsTest
 */

import java.lang.reflect.Modifier;


public class EncodingConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.policy.EncodingConstants", false, EncodingConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.policy.EncodingConstants"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ENCODING_NS"), "field ENCODING_NS");
        check(fn.contains("OPTIMIZED_FI_SERIALIZATION_ASSERTION"), "field OPTIMIZED_FI_SERIALIZATION_ASSERTION");
        check(fn.contains("OPTIMIZED_MIME_NS"), "field OPTIMIZED_MIME_NS");
        check(fn.contains("OPTIMIZED_MIME_SERIALIZATION_ASSERTION"), "field OPTIMIZED_MIME_SERIALIZATION_ASSERTION");
        check(fn.contains("SELECT_OPTIMAL_ENCODING_ASSERTION"), "field SELECT_OPTIMAL_ENCODING_ASSERTION");
        check(fn.contains("SUN_ENCODING_CLIENT_NS"), "field SUN_ENCODING_CLIENT_NS");
        check(fn.contains("SUN_FI_SERVICE_NS"), "field SUN_FI_SERVICE_NS");
        check(fn.contains("UTF816FFFE_CHARACTER_ENCODING_ASSERTION"), "field UTF816FFFE_CHARACTER_ENCODING_ASSERTION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
