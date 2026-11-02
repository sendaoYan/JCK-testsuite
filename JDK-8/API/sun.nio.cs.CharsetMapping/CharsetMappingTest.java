/*
 * @test
 * @summary sun.nio.cs.CharsetMapping conformance: class and members.
 * @run main CharsetMappingTest
 */

public class CharsetMappingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.CharsetMapping", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.CharsetMapping"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decodeComposite"), "declares method decodeComposite");
        check(mnames.contains("decodeDouble"), "declares method decodeDouble");
        check(mnames.contains("decodeSingle"), "declares method decodeSingle");
        check(mnames.contains("decodeSurrogate"), "declares method decodeSurrogate");
        check(mnames.contains("encodeChar"), "declares method encodeChar");
        check(mnames.contains("encodeComposite"), "declares method encodeComposite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
