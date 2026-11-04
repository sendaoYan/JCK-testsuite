/*
 * @test
 * @summary sun.text.normalizer.UCharacter conformance: class and members.
 * @run main UCharacterTest
 */

public class UCharacterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UCharacter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UCharacter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("digit"), "declares method digit");
        check(mnames.contains("getAge"), "declares method getAge");
        check(mnames.contains("getCodePoint"), "declares method getCodePoint");
        check(mnames.contains("getDirection"), "declares method getDirection");
        check(mnames.contains("getEuropeanDigit"), "declares method getEuropeanDigit");
        check(mnames.contains("getNumericType"), "declares method getNumericType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
