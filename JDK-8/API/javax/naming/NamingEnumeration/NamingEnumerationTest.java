/*
 * @test
 * @summary javax.naming.NamingEnumeration conformance: interface hierarchy and members.
 * @run main NamingEnumerationTest
 */


public class NamingEnumerationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.NamingEnumeration");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.Enumeration");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("close"), "has method close");
        check(names.contains("hasMore"), "has method hasMore");
        check(names.contains("hasMoreElements"), "has method hasMoreElements");        System.out.println("PASS (" + passed + " assertions)");
    }
}
