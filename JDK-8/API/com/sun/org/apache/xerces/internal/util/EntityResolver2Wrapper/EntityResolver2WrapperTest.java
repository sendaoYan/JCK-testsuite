/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.EntityResolver2Wrapper conformance: class hierarchy and members.
 * @run main EntityResolver2WrapperTest
 */
public class EntityResolver2WrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.EntityResolver2Wrapper", false, EntityResolver2WrapperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver"), "has interface com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("getExternalSubset"), "method getExternalSubset");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
