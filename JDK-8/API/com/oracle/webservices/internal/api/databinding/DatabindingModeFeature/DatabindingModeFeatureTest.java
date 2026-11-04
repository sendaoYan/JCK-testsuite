/*
 * @test
 * @summary com.oracle.webservices.internal.api.databinding.DatabindingModeFeature conformance: type shape via reflection (headless-safe).
 * @run main DatabindingModeFeatureTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DatabindingModeFeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.webservices.internal.api.databinding.DatabindingModeFeature", false, DatabindingModeFeatureTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.ws.WebServiceFeature"), "superclass javax.xml.ws.WebServiceFeature");
        check(hasInterface(c, "com.sun.xml.internal.ws.api.ServiceSharedFeatureMarker"), "implements com.sun.xml.internal.ws.api.ServiceSharedFeatureMarker");
        check(hasMethod(c, "builder"), "declares builder");
        check(hasMethod(c, "getID"), "declares getID");
        check(hasMethod(c, "getMode"), "declares getMode");
        check(hasMethod(c, "getProperties"), "declares getProperties");
        check(hasField(c, "GLASSFISH_JAXB"), "declares field GLASSFISH_JAXB");
        check(hasField(c, "ID"), "declares field ID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
