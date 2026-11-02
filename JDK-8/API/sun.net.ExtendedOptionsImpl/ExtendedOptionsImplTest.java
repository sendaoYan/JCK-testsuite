/*
 * @test
 * @summary sun.net.ExtendedOptionsImpl conformance: class and members.
 * @run main ExtendedOptionsImplTest
 */

public class ExtendedOptionsImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ExtendedOptionsImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ExtendedOptionsImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkGetOptionPermission"), "declares method checkGetOptionPermission");
        check(mnames.contains("checkSetOptionPermission"), "declares method checkSetOptionPermission");
        check(mnames.contains("checkValueType"), "declares method checkValueType");
        check(mnames.contains("flowSupported"), "declares method flowSupported");
        check(mnames.contains("getFlowOption"), "declares method getFlowOption");
        check(mnames.contains("getTcpKeepAliveIntvl"), "declares method getTcpKeepAliveIntvl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
