/*
 * @test
 * @summary javax.naming.directory.Attribute conformance: interface hierarchy and members.
 * @run main AttributeTest
 */


public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attribute");
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
        check(names.contains("add"), "has method add");
        check(names.contains("clear"), "has method clear");
        check(names.contains("contains"), "has method contains");        System.out.println("PASS (" + passed + " assertions)");
    }
}
