/*
 * @test
 * @summary com.oracle.webservices.internal.api.databinding.ExternalMetadataFeature conformance: type shape via reflection (headless-safe).
 * @run main ExternalMetadataFeatureTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ExternalMetadataFeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.webservices.internal.api.databinding.ExternalMetadataFeature", false, ExternalMetadataFeatureTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.ws.WebServiceFeature"), "superclass javax.xml.ws.WebServiceFeature");
        check(hasMethod(c, "addFiles"), "declares addFiles");
        check(hasMethod(c, "addResources"), "declares addResources");
        check(hasMethod(c, "builder"), "declares builder");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getFiles"), "declares getFiles");
        check(hasMethod(c, "getID"), "declares getID");
        check(hasMethod(c, "getMetadataReader"), "declares getMetadataReader");
        check(hasMethod(c, "getResourceNames"), "declares getResourceNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
