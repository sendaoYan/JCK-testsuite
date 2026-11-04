/*
 * @test
 * @summary javax.naming.Name conformance: interface hierarchy and members,
 *          plus stable serialVersionUID.
 * @run main NameTest
 */

import javax.naming.Name;

public class NameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.Name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        expIfs.add("java.lang.Cloneable");
        expIfs.add("java.lang.Comparable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("add"), "has method add");
        check(names.contains("addAll"), "has method addAll");
        check(names.contains("endsWith"), "has method endsWith");
    }

    static void testSerialVersionUID() {
        check(Name.serialVersionUID == -3617482732056931635L,
                "stable serialVersionUID");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testSerialVersionUID();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
