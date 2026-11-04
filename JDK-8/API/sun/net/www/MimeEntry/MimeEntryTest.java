/*
 * @test
 * @summary sun.net.www.MimeEntry conformance: class and members.
 * @run main MimeEntryTest
 */

public class MimeEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MimeEntry", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.MimeEntry"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("getAction"), "declares method getAction");
        check(mnames.contains("getDescription"), "declares method getDescription");
        check(mnames.contains("getExtensions"), "declares method getExtensions");
        check(mnames.contains("getExtensionsAsList"), "declares method getExtensionsAsList");
        check(mnames.contains("getImageFileName"), "declares method getImageFileName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
