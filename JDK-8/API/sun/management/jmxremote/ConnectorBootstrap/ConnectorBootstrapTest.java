/*
 * @test
 * @summary sun.management.jmxremote.ConnectorBootstrap conformance: class and members.
 * @run main ConnectorBootstrapTest
 */

public class ConnectorBootstrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jmxremote.ConnectorBootstrap", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jmxremote.ConnectorBootstrap"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkAccessFile"), "declares method checkAccessFile");
        check(mnames.contains("checkPasswordFile"), "declares method checkPasswordFile");
        check(mnames.contains("checkRestrictedFile"), "declares method checkRestrictedFile");
        check(mnames.contains("createSslRMIServerSocketFactory"), "declares method createSslRMIServerSocketFactory");
        check(mnames.contains("exportMBeanServer"), "declares method exportMBeanServer");
        check(mnames.contains("getDefaultFileName"), "declares method getDefaultFileName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
