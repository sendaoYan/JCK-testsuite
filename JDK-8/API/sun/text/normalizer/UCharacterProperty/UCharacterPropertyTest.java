/*
 * @test
 * @summary sun.text.normalizer.UCharacterProperty conformance: class and members.
 * @run main UCharacterPropertyTest
 */

public class UCharacterPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UCharacterProperty", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UCharacterProperty"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAdditional"), "declares method getAdditional");
        check(mnames.contains("getAge"), "declares method getAge");
        check(mnames.contains("getInstance"), "declares method getInstance");
        check(mnames.contains("getProperty"), "declares method getProperty");
        check(mnames.contains("getRawSupplementary"), "declares method getRawSupplementary");
        check(mnames.contains("getUnsignedValue"), "declares method getUnsignedValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
