/*
 * @test
 * @summary com.sun.corba.se.spi.ior.IORFactories conformance: type shape via reflection (headless-safe).
 * @run main IORFactoriesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IORFactoriesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.IORFactories", false, IORFactoriesTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("getIOR"), "method getIOR");
        check(mn.contains("getIORFactory"), "method getIORFactory");
        check(mn.contains("getIORTemplateList"), "method getIORTemplateList");
        check(mn.contains("makeIOR"), "method makeIOR");
        check(mn.contains("makeIORTemplate"), "method makeIORTemplate");
        check(mn.contains("makeIORTemplateList"), "method makeIORTemplateList");
        check(mn.contains("makeObjectId"), "method makeObjectId");
        check(mn.contains("makeObjectKey"), "method makeObjectKey");
        check(mn.contains("makeObjectKeyFactory"), "method makeObjectKeyFactory");
        check(mn.contains("makeObjectReference"), "method makeObjectReference");
        check(mn.contains("makeObjectReferenceFactory"), "method makeObjectReferenceFactory");
        check(mn.contains("makeObjectReferenceTemplate"), "method makeObjectReferenceTemplate");
        check(mn.contains("registerValueFactories"), "method registerValueFactories");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
