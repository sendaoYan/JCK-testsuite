/*
 * @test
 * @summary com.sun.xml.internal.ws.developer.EPRRecipe conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EPRRecipeTest
 */

import java.lang.reflect.Modifier;


public class EPRRecipeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.developer.EPRRecipe", false, EPRRecipeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.developer.EPRRecipe"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addMetadata"), "method addMetadata");
        check(mn.contains("addReferenceParameter"), "method addReferenceParameter");
        check(mn.contains("addReferenceParameters"), "method addReferenceParameters");
        check(mn.contains("getMetadata"), "method getMetadata");
        check(mn.contains("getReferenceParameters"), "method getReferenceParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
