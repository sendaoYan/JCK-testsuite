/*
 * @test
 * @summary sun.invoke.util.ValueConversions conformance: class and members.
 * @run main ValueConversionsTest
 */

public class ValueConversionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.ValueConversions", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.ValueConversions"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("booleanToByte"), "declares method booleanToByte");
        check(mnames.contains("booleanToChar"), "declares method booleanToChar");
        check(mnames.contains("booleanToDouble"), "declares method booleanToDouble");
        check(mnames.contains("booleanToFloat"), "declares method booleanToFloat");
        check(mnames.contains("booleanToInt"), "declares method booleanToInt");
        check(mnames.contains("booleanToLong"), "declares method booleanToLong");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
