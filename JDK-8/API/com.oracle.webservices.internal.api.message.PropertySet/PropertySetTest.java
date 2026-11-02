/*
 * @test
 * @summary com.oracle.webservices.internal.api.message.PropertySet conformance: type shape via reflection (headless-safe).
 * @run main PropertySetTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PropertySetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.webservices.internal.api.message.PropertySet", false, PropertySetTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "asMap"), "declares asMap");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "createMapView"), "declares createMapView");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "supports"), "declares supports");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
