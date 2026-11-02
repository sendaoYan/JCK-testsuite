/*
 * @test
 * @summary sun.text.normalizer.UTF16 conformance: class and members.
 * @run main UTF16Test
 */

public class UTF16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UTF16", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UTF16"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("_charAt"), "declares method _charAt");
        check(mnames.contains("append"), "declares method append");
        check(mnames.contains("charAt"), "declares method charAt");
        check(mnames.contains("getCharCount"), "declares method getCharCount");
        check(mnames.contains("getLeadSurrogate"), "declares method getLeadSurrogate");
        check(mnames.contains("getTrailSurrogate"), "declares method getTrailSurrogate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
