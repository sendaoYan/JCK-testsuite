/*
 * @test
 * @summary sun.misc.BASE64Decoder conformance: class and members.
 * @run main BASE64DecoderTest
 */

public class BASE64DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.BASE64Decoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.BASE64Decoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.misc.CharacterDecoder"), "superclass sun.misc.CharacterDecoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bytesPerAtom"), "declares method bytesPerAtom");
        check(mnames.contains("bytesPerLine"), "declares method bytesPerLine");
        check(mnames.contains("decodeAtom"), "declares method decodeAtom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
