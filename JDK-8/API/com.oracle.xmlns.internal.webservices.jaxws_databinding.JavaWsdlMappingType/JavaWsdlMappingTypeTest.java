/*
 * @test
 * @summary com.oracle.xmlns.internal.webservices.jaxws_databinding.JavaWsdlMappingType conformance: type shape via reflection (headless-safe).
 * @run main JavaWsdlMappingTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaWsdlMappingTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.xmlns.internal.webservices.jaxws_databinding.JavaWsdlMappingType", false, JavaWsdlMappingTypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getClassAnnotation"), "declares getClassAnnotation");
        check(hasMethod(c, "getDatabinding"), "declares getDatabinding");
        check(hasMethod(c, "getExistingAnnotations"), "declares getExistingAnnotations");
        check(hasMethod(c, "getJavaMethods"), "declares getJavaMethods");
        check(hasMethod(c, "getJavaTypeName"), "declares getJavaTypeName");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getOtherAttributes"), "declares getOtherAttributes");
        check(hasMethod(c, "getXmlSchemaMapping"), "declares getXmlSchemaMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
