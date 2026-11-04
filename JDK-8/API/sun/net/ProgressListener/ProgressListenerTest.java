/*
 * @test
 * @summary sun.net.ProgressListener conformance: interface and members.
 * @run main ProgressListenerTest
 */

public class ProgressListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ProgressListener", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ProgressListener"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.EventListener"), "interface java.util.EventListener");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("progressFinish"), "declares method progressFinish");
        check(mnames.contains("progressStart"), "declares method progressStart");
        check(mnames.contains("progressUpdate"), "declares method progressUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
