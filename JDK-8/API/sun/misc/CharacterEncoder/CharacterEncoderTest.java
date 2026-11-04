/*
 * @test
 * @summary sun.misc.CharacterEncoder conformance: abstract class and members.
 * @run main CharacterEncoderTest
 */

public class CharacterEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.CharacterEncoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.CharacterEncoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bytesPerAtom"), "declares method bytesPerAtom");
        check(mnames.contains("bytesPerLine"), "declares method bytesPerLine");
        check(mnames.contains("encode"), "declares method encode");
        check(mnames.contains("encodeAtom"), "declares method encodeAtom");
        check(mnames.contains("encodeBuffer"), "declares method encodeBuffer");
        check(mnames.contains("encodeBufferPrefix"), "declares method encodeBufferPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
