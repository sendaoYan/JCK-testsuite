/*
 * @test
 * @summary javax.naming.ldap.Control conformance: interface hierarchy and members.
 * @run main ControlTest
 */


public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.Control");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEncodedValue"), "has method getEncodedValue");
        check(names.contains("getID"), "has method getID");
        check(names.contains("isCritical"), "has method isCritical");        System.out.println("PASS (" + passed + " assertions)");
    }
}
