/*
 * @test
 * @summary sun.misc.RequestProcessor conformance: class and members.
 * @run main RequestProcessorTest
 */

public class RequestProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.RequestProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.RequestProcessor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Runnable"), "interface java.lang.Runnable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("lazyInitialize"), "declares method lazyInitialize");
        check(mnames.contains("postRequest"), "declares method postRequest");
        check(mnames.contains("run"), "declares method run");
        check(mnames.contains("startProcessing"), "declares method startProcessing");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
