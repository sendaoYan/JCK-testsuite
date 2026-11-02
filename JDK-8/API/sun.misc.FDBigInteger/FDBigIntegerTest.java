/*
 * @test
 * @summary sun.misc.FDBigInteger conformance: class and members.
 * @run main FDBigIntegerTest
 */

public class FDBigIntegerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FDBigInteger", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FDBigInteger"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("addAndCmp"), "declares method addAndCmp");
        check(mnames.contains("big5pow"), "declares method big5pow");
        check(mnames.contains("big5powRec"), "declares method big5powRec");
        check(mnames.contains("checkZeroTail"), "declares method checkZeroTail");
        check(mnames.contains("cmp"), "declares method cmp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
