/*
 * @test
 * @summary sun.nio.cs.ext.COMPOUND_TEXT_Encoder conformance: class and members.
 * @run main COMPOUND_TEXT_EncoderTest
 */

public class COMPOUND_TEXT_EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.COMPOUND_TEXT_Encoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.COMPOUND_TEXT_Encoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass java.nio.charset.CharsetEncoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canEncode"), "declares method canEncode");
        check(mnames.contains("encodeLoop"), "declares method encodeLoop");
        check(mnames.contains("flushEncoder"), "declares method flushEncoder");
        check(mnames.contains("flushNonStandardCharsetBuffer"), "declares method flushNonStandardCharsetBuffer");
        check(mnames.contains("getEncoder"), "declares method getEncoder");
        check(mnames.contains("implFlush"), "declares method implFlush");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
