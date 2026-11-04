/*
 * @test
 * @summary sun.net.www.MeteredStream conformance: class and members.
 * @run main MeteredStreamTest
 */

public class MeteredStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MeteredStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.MeteredStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass java.io.FilterInputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("available"), "declares method available");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("finalize"), "declares method finalize");
        check(mnames.contains("isMarked"), "declares method isMarked");
        check(mnames.contains("justRead"), "declares method justRead");
        check(mnames.contains("mark"), "declares method mark");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
