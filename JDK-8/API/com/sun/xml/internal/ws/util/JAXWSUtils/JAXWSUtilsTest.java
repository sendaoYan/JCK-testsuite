/*
 * @test
 * @summary com.sun.xml.internal.ws.util.JAXWSUtils conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXWSUtilsTest
 */

import java.lang.reflect.Modifier;


public class JAXWSUtilsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.JAXWSUtils", false, JAXWSUtilsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.JAXWSUtils"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("absolutize"), "method absolutize");
        check(mn.contains("checkAbsoluteness"), "method checkAbsoluteness");
        check(mn.contains("getEncodedURL"), "method getEncodedURL");
        check(mn.contains("getFileOrURL"), "method getFileOrURL");
        check(mn.contains("getFileOrURLName"), "method getFileOrURLName");
        check(mn.contains("getUUID"), "method getUUID");
        check(mn.contains("matchQNames"), "method matchQNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
