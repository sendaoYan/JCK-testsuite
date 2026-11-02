/*
 * @test
 * @summary javax.naming.event.EventDirContext conformance: interface hierarchy and members.
 * @run main EventDirContextTest
 */


public class EventDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.event.EventDirContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.directory.DirContext");
        expIfs.add("javax.naming.event.EventContext");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addNamingListener"), "has method addNamingListener");
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");        System.out.println("PASS (" + passed + " assertions)");
    }
}
