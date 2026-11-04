/*
 * @test
 * @summary sun.net.www.content.audio.x_aiff conformance: class and members.
 * @run main x_aiffTest
 */

public class x_aiffTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.content.audio.x_aiff", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.content.audio.x_aiff"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.ContentHandler"), "superclass java.net.ContentHandler");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getContent"), "declares method getContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
