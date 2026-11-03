/*
 * @test
 * @summary javax.security.cert.Certificate conformance: white-box structure + black-box behavior.
 * @run main CertificateTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class CertificateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.cert.Certificate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(hasPublicCtor(c), "public constructor");
        checkMethods(c, new String[]{"getEncoded","verify","toString","getPublicKey","equals","hashCode"});
    }

    // Minimal concrete subclass: only the abstract methods are implemented.
    static class TestCert extends javax.security.cert.Certificate {
        final byte[] enc;
        TestCert(byte[] enc) { this.enc = enc; }
        public byte[] getEncoded() { return enc; }
        public java.security.PublicKey getPublicKey() { return null; }
        public void verify(java.security.PublicKey key) {}
        public void verify(java.security.PublicKey key, String sigProvider) {}
        public String toString() { return "TestCert"; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        byte[] a = new byte[]{1, 2, 3};
        byte[] b = new byte[]{1, 2, 3};
        byte[] c = new byte[]{4, 5, 6};
        TestCert x = new TestCert(a);
        TestCert y = new TestCert(b);
        TestCert z = new TestCert(c);

        check(x.getEncoded() == a, "getEncoded returns array");
        check("TestCert".equals(x.toString()), "toString");
        check(x.getPublicKey() == null, "getPublicKey");

        check(x.equals(x), "equals self");
        check(x.equals(y), "equals same encoding");
        check(!x.equals(z), "not equals different encoding");
        check(!x.equals(null), "not equals null");
        check(!x.equals("string"), "not equals non-certificate");
        check(x.hashCode() == y.hashCode(), "hashCode equal for equal encoding");
        check(x.hashCode() == Arrays.hashCode(a), "hashCode derived from encoded");

        try { x.verify(null); check(true, "verify(PublicKey) callable"); }
        catch (Exception e) { check(false, "verify should not throw here"); }
        try { x.verify(null, "provider"); check(true, "verify(PublicKey,String) callable"); }
        catch (Exception e) { check(false, "verify(2-arg) should not throw here"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
