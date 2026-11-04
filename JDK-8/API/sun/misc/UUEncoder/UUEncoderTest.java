/*
 * @test
 * @summary sun.misc.UUEncoder conformance: class and members.
 * @run main UUEncoderTest
 */

public class UUEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.UUEncoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.UUEncoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.misc.CharacterEncoder"), "superclass sun.misc.CharacterEncoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bytesPerAtom"), "declares method bytesPerAtom");
        check(mnames.contains("bytesPerLine"), "declares method bytesPerLine");
        check(mnames.contains("encodeAtom"), "declares method encodeAtom");
        check(mnames.contains("encodeBufferPrefix"), "declares method encodeBufferPrefix");
        check(mnames.contains("encodeBufferSuffix"), "declares method encodeBufferSuffix");
        check(mnames.contains("encodeLinePrefix"), "declares method encodeLinePrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
