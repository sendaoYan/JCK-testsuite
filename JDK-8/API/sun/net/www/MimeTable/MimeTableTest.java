/*
 * @test
 * @summary sun.net.www.MimeTable conformance: class and members.
 * @run main MimeTableTest
 */

public class MimeTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MimeTable", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.MimeTable"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.net.FileNameMap"), "interface java.net.FileNameMap");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("elements"), "declares method elements");
        check(mnames.contains("fill"), "declares method fill");
        check(mnames.contains("find"), "declares method find");
        check(mnames.contains("findByDescription"), "declares method findByDescription");
        check(mnames.contains("findByExt"), "declares method findByExt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
