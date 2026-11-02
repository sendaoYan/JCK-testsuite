/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.SerializerFactory conformance: abstract class hierarchy and members.
 * @run main SerializerFactoryTest
 */
public class SerializerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.SerializerFactory", false, SerializerFactoryTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("registerSerializerFactory"), "method registerSerializerFactory");
        check(mn.contains("getSerializerFactory"), "method getSerializerFactory");
        check(mn.contains("getSupportedMethod"), "method getSupportedMethod");
        check(mn.contains("makeSerializer"), "method makeSerializer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
