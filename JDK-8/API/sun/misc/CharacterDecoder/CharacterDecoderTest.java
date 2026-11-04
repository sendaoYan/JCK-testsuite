/*
 * @test
 * @summary sun.misc.CharacterDecoder conformance: abstract class and members.
 * @run main CharacterDecoderTest
 */

public class CharacterDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.CharacterDecoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.CharacterDecoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bytesPerAtom"), "declares method bytesPerAtom");
        check(mnames.contains("bytesPerLine"), "declares method bytesPerLine");
        check(mnames.contains("decodeAtom"), "declares method decodeAtom");
        check(mnames.contains("decodeBuffer"), "declares method decodeBuffer");
        check(mnames.contains("decodeBufferPrefix"), "declares method decodeBufferPrefix");
        check(mnames.contains("decodeBufferSuffix"), "declares method decodeBufferSuffix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
