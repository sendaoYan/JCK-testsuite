/*
 * @test
 * @summary sun.misc.JavaNioAccess conformance: interface and members.
 * @run main JavaNioAccessTest
 */

public class JavaNioAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaNioAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaNioAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getDirectBufferPool"), "declares method getDirectBufferPool");
        check(mnames.contains("newDirectByteBuffer"), "declares method newDirectByteBuffer");
        check(mnames.contains("truncate"), "declares method truncate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
