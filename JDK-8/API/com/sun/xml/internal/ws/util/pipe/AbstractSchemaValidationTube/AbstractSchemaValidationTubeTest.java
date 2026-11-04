/*
 * @test
 * @summary com.sun.xml.internal.ws.util.pipe.AbstractSchemaValidationTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AbstractSchemaValidationTubeTest
 */

import java.lang.reflect.Modifier;


public class AbstractSchemaValidationTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.pipe.AbstractSchemaValidationTube", false, AbstractSchemaValidationTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.pipe.AbstractSchemaValidationTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl"), "superclass com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("doProcess"), "method doProcess");
        check(mn.contains("getSchemaSources"), "method getSchemaSources");
        check(mn.contains("getValidator"), "method getValidator");
        check(mn.contains("isNoValidation"), "method isNoValidation");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("binding"), "field binding");
        check(fn.contains("feature"), "field feature");
        check(fn.contains("resolver"), "field resolver");
        check(fn.contains("sf"), "field sf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
