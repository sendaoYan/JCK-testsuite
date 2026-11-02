/*
 * @test
 * @summary sun.text.SupplementaryCharacterData conformance: class and members.
 * @run main SupplementaryCharacterDataTest
 */

public class SupplementaryCharacterDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.SupplementaryCharacterData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.SupplementaryCharacterData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getArray"), "declares method getArray");
        check(mnames.contains("getValue"), "declares method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
