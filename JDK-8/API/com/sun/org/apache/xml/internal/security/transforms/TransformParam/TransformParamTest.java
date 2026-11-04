/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.TransformParam conformance: interface hierarchy and members.
 * @run main TransformParamTest
 */
public class TransformParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.TransformParam", false, TransformParamTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
