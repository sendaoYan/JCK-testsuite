/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.utils.SerializerMessages_es conformance: class hierarchy and members.
 * @run main SerializerMessages_esTest
 */
public class SerializerMessages_esTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.utils.SerializerMessages_es", false, SerializerMessages_esTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContents"), "method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
