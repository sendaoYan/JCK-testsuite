/*
 * @test
 * @summary sun.text.normalizer.Utility conformance: class and members.
 * @run main UtilityTest
 */

public class UtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.Utility", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.Utility"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("appendNumber"), "declares method appendNumber");
        check(mnames.contains("arrayRegionMatches"), "declares method arrayRegionMatches");
        check(mnames.contains("escape"), "declares method escape");
        check(mnames.contains("escapeUnprintable"), "declares method escapeUnprintable");
        check(mnames.contains("getChars"), "declares method getChars");
        check(mnames.contains("hex"), "declares method hex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
