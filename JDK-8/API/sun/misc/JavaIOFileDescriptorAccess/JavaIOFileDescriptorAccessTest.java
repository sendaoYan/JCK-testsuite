/*
 * @test
 * @summary sun.misc.JavaIOFileDescriptorAccess conformance: interface and members.
 * @run main JavaIOFileDescriptorAccessTest
 */

public class JavaIOFileDescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaIOFileDescriptorAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaIOFileDescriptorAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getHandle"), "declares method getHandle");
        check(mnames.contains("set"), "declares method set");
        check(mnames.contains("setHandle"), "declares method setHandle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
