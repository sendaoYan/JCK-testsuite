/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.reference.ReferenceData conformance: interface hierarchy and members.
 * @run main ReferenceDataTest
 */
public class ReferenceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.reference.ReferenceData", false, ReferenceDataTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
