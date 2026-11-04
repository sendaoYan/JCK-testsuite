/*
 * @test
 * @summary sun.corba.JavaCorbaAccess conformance: interface and members.
 * @run main JavaCorbaAccessTest
 */

public class JavaCorbaAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.corba.JavaCorbaAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.corba.JavaCorbaAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("loadClass"), "declares method loadClass");
        check(mnames.contains("newValueHandlerImpl"), "declares method newValueHandlerImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
