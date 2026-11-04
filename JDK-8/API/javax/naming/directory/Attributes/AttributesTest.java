/*
 * @test
 * @summary javax.naming.directory.Attributes conformance: interface hierarchy and members.
 * @run main AttributesTest
 */


public class AttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attributes");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        expIfs.add("java.lang.Cloneable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("get"), "has method get");
        check(names.contains("getAll"), "has method getAll");
        check(names.contains("getIDs"), "has method getIDs");        System.out.println("PASS (" + passed + " assertions)");
    }
}
