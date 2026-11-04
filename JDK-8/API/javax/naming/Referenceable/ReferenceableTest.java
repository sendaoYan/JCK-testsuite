/*
 * @test
 * @summary javax.naming.Referenceable conformance: interface hierarchy and members.
 * @run main ReferenceableTest
 */


public class ReferenceableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.Referenceable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getReference"), "has method getReference");        System.out.println("PASS (" + passed + " assertions)");
    }
}
