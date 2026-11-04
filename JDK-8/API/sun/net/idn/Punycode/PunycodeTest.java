/*
 * @test
 * @summary sun.net.idn.Punycode conformance: class and members.
 * @run main PunycodeTest
 */

public class PunycodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.idn.Punycode", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.idn.Punycode"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("adaptBias"), "declares method adaptBias");
        check(mnames.contains("asciiCaseMap"), "declares method asciiCaseMap");
        check(mnames.contains("decode"), "declares method decode");
        check(mnames.contains("digitToBasic"), "declares method digitToBasic");
        check(mnames.contains("encode"), "declares method encode");
        check(mnames.contains("isBasic"), "declares method isBasic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
