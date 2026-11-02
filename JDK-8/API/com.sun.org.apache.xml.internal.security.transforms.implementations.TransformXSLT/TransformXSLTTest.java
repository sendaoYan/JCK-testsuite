/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.implementations.TransformXSLT conformance: class hierarchy and members.
 * @run main TransformXSLTTest
 */
public class TransformXSLTTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformXSLT", false, TransformXSLTTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.transforms.TransformSpi"), "superclass com.sun.org.apache.xml.internal.security.transforms.TransformSpi");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineGetURI"), "method engineGetURI");
        check(mn.contains("enginePerformTransform"), "method enginePerformTransform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
