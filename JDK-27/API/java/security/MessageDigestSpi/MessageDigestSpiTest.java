/*
 * @test
 * @summary java.security.MessageDigestSpi conformance: white-box structure + black-box behavior.
 * @run main MessageDigestSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigestSpi;

public class MessageDigestSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigestSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"clone", "engineGetDigestLength", "engineUpdate", "engineDigest", "engineReset"});
    }

    static void testBlackBox() throws Exception {
        MyDigestSpi d = new MyDigestSpi();
        check(d.doGetDigestLength() == 0, "default engineGetDigestLength == 0");
        Object c = d.clone();
        check(c != null && c != d && (c instanceof MessageDigestSpi), "clone distinct instance");
        byte[] buf = new byte[8];
        int n = d.doDigest(buf, 2, 8);
        check(n == 4, "inherited engineDigest returns length");
        check(buf[2] == 9 && buf[3] == 9 && buf[4] == 9 && buf[5] == 9, "digest bytes written");
        ByteBuffer bb = ByteBuffer.wrap(new byte[]{1, 2, 3});
        d.doUpdate(bb);
        check(bb.position() == bb.limit(), "engineUpdate(ByteBuffer) advances position");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MyDigestSpi extends MessageDigestSpi implements Cloneable {
        protected void engineUpdate(byte input) { }
        protected void engineUpdate(byte[] input, int offset, int len) { }
        protected byte[] engineDigest() { return new byte[]{9, 9, 9, 9}; }
        protected void engineReset() { }
        int doGetDigestLength() { return engineGetDigestLength(); }
        int doDigest(byte[] buf, int off, int len) throws DigestException { return engineDigest(buf, off, len); }
        void doUpdate(ByteBuffer b) { engineUpdate(b); }
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
