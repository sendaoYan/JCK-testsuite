/*
 * @test
 * @summary com.sun.corba.se.impl.ior.ObjectReferenceProducerBase conformance: type shape via reflection (headless-safe).
 * @run main ObjectReferenceProducerBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectReferenceProducerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.ObjectReferenceProducerBase", false, ObjectReferenceProducerBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("getIORFactory"), "method getIORFactory");
        check(mn.contains("getIORTemplateList"), "method getIORTemplateList");
        check(mn.contains("make_object"), "method make_object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
