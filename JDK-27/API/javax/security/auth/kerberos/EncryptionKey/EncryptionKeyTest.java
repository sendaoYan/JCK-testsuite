/*
 * @test
 * @summary javax.security.auth.kerberos.EncryptionKey conformance: white-box structure + black-box behavior.
 * @run main EncryptionKeyTest
 */

import javax.crypto.SecretKey;
import javax.security.auth.kerberos.EncryptionKey;
import java.util.Arrays;

public class EncryptionKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " [expected " + type.getSimpleName() + ", got " + t + "]", t);
        }
        throw new AssertionError(msg + " [expected " + type.getSimpleName() + "]");
    }
    static boolean hasInterface(Class<?> c, Class<?> want) {
        for (Class<?> i : c.getInterfaces()) if (i == want) return true;
        return false;
    }
    static boolean declares(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.EncryptionKey");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasInterface(c, SecretKey.class), "implements javax.crypto.SecretKey");
        for (String m : new String[]{"getKeyType", "getAlgorithm", "getFormat", "getEncoded", "destroy", "isDestroyed", "toString", "equals", "hashCode"}) {
            check(declares(c, m), "method " + m + " declared on class");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // AES256 etype 18, AES128 etype 17 (IANA names verified against JDK-27)
        EncryptionKey ek = new EncryptionKey(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}, 18);
        check(ek.getKeyType() == 18, "getKeyType 18");
        check("aes256-cts-hmac-sha1-96".equals(ek.getAlgorithm()), "algorithm AES256");
        check("RAW".equals(ek.getFormat()), "format RAW");
        check(Arrays.equals(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}, ek.getEncoded()), "getEncoded contents");

        EncryptionKey ek17 = new EncryptionKey(new byte[16], 17);
        check(ek17.getKeyType() == 17, "getKeyType 17");
        check("aes128-cts-hmac-sha1-96".equals(ek17.getAlgorithm()), "algorithm AES128");

        // algorithm naming edge cases (IANA unknown / none / private)
        check("none".equals(new EncryptionKey(new byte[1], 0).getAlgorithm()), "etype 0 -> none");
        check("unknown".equals(new EncryptionKey(new byte[1], 999).getAlgorithm()), "unknown positive etype -> unknown");
        check("private".equals(new EncryptionKey(new byte[1], -1).getAlgorithm()), "negative etype -> private");

        // constructor copies input bytes; getEncoded returns a fresh copy
        byte[] input = new byte[]{5,5,5};
        EncryptionKey cp = new EncryptionKey(input, 18);
        input[0] = 9;
        check(cp.getEncoded()[0] == 5, "constructor copies key bytes");
        byte[] got = cp.getEncoded();
        got[0] = 77;
        check(cp.getEncoded()[0] == 5, "getEncoded returns copy");

        // equals / hashCode: key type + key material
        EncryptionKey e1 = new EncryptionKey(new byte[]{1,2,3}, 17);
        EncryptionKey e2 = new EncryptionKey(new byte[]{1,2,3}, 17);
        EncryptionKey e3 = new EncryptionKey(new byte[]{1,2,3}, 18);
        EncryptionKey e4 = new EncryptionKey(new byte[]{1,2,4}, 17);
        check(e1.equals(e1), "equals self");
        check(e1.equals(e2), "equals same type+material");
        check(!e1.equals(e3), "not equals different type");
        check(!e1.equals(e4), "not equals different material");
        check(!e1.equals(null), "not equals null");
        check(!e1.equals("x"), "not equals non-key");
        check(e1.hashCode() == e2.hashCode(), "hashCode equal keys");

        // NPE for null key bytes
        checkThrows(NullPointerException.class, () -> new EncryptionKey(null, 17), "null key bytes -> NPE");

        // destroy lifecycle
        EncryptionKey dk = new EncryptionKey(new byte[]{1,2,3,4}, 18);
        check(!dk.isDestroyed(), "not destroyed initially");
        dk.destroy();
        check(dk.isDestroyed(), "destroyed flag");
        check("Destroyed EncryptionKey".equals(dk.toString()), "toString after destroy");
        check(dk.hashCode() == 17, "hashCode after destroy == 17");
        check(dk.equals(dk), "destroyed equals self");
        checkThrows(IllegalStateException.class, () -> dk.getKeyType(), "getKeyType after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getAlgorithm(), "getAlgorithm after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getFormat(), "getFormat after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getEncoded(), "getEncoded after destroy -> ISE");
        EncryptionKey dk2 = new EncryptionKey(new byte[]{1,2,3,4}, 18);
        dk2.destroy();
        check(!dk.equals(dk2), "two destroyed keys not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
