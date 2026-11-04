/*
 * @test
 * @summary sun.text.bidi.BidiLine conformance: class and members.
 * @run main BidiLineTest
 */

public class BidiLineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.bidi.BidiLine", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.bidi.BidiLine"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getLevelAt"), "declares method getLevelAt");
        check(mnames.contains("getLevels"), "declares method getLevels");
        check(mnames.contains("getLogicalRun"), "declares method getLogicalRun");
        check(mnames.contains("getRunFromLogicalIndex"), "declares method getRunFromLogicalIndex");
        check(mnames.contains("getRuns"), "declares method getRuns");
        check(mnames.contains("getSingleRun"), "declares method getSingleRun");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
