/*
 * @test
 * @summary sun.management.ConnectorAddressLink conformance: class and members.
 * @run main ConnectorAddressLinkTest
 */

public class ConnectorAddressLinkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.ConnectorAddressLink", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.ConnectorAddressLink"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("export"), "declares method export");
        check(mnames.contains("exportRemote"), "declares method exportRemote");
        check(mnames.contains("importFrom"), "declares method importFrom");
        check(mnames.contains("importRemoteFrom"), "declares method importRemoteFrom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
