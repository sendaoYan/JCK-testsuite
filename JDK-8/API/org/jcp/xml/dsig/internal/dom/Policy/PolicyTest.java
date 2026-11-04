/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Policy conformance: type shape via reflection (headless-safe).
 * @run main PolicyTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Policy", false, PolicyTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "disabledAlgs"), "declares disabledAlgs");
        check(hasMethod(c, "disabledReferenceUriSchemes"), "declares disabledReferenceUriSchemes");
        check(hasMethod(c, "maxReferences"), "declares maxReferences");
        check(hasMethod(c, "maxTransforms"), "declares maxTransforms");
        check(hasMethod(c, "minKeySize"), "declares minKeySize");
        check(hasMethod(c, "restrictAlg"), "declares restrictAlg");
        check(hasMethod(c, "restrictDuplicateIds"), "declares restrictDuplicateIds");
        check(hasMethod(c, "restrictKey"), "declares restrictKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
