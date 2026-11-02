/*
 * @test
 * @summary sun.text.IntHashtable conformance: class and members.
 * @run main IntHashtableTest
 */

public class IntHashtableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.IntHashtable", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.IntHashtable"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("find"), "declares method find");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getDefaultValue"), "declares method getDefaultValue");
        check(mnames.contains("hashCode"), "declares method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
