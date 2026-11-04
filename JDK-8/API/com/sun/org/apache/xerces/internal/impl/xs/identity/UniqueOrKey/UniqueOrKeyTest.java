/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.identity.UniqueOrKey conformance: class hierarchy and members.
 * @run main UniqueOrKeyTest
 */
public class UniqueOrKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.identity.UniqueOrKey", false, UniqueOrKeyTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint"), "superclass com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
