/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosKey conformance: white-box structure + black-box behavior.
 * @run main KerberosKeyTest
 */

import javax.crypto.SecretKey;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import java.util.Arrays;

public class KerberosKeyTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.KerberosKey");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "non-final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasInterface(c, SecretKey.class), "implements javax.crypto.SecretKey");
        for (String m : new String[]{"getPrincipal", "getVersionNumber", "getKeyType", "getAlgorithm", "getFormat", "getEncoded", "destroy", "isDestroyed"}) {
            check(c.getMethod(m).getDeclaringClass() == c, "public method " + m + " declared");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KerberosPrincipal p = new KerberosPrincipal("alice@EXAMPLE.COM");
        byte[] keyBytes = new byte[]{10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};
        KerberosKey kk = new KerberosKey(p, keyBytes, 17, 3);
        check("alice@EXAMPLE.COM".equals(kk.getPrincipal().getName()), "getPrincipal name");
        check(kk.getPrincipal().equals(p), "getPrincipal identity");
        check(kk.getVersionNumber() == 3, "getVersionNumber");
        check(kk.getKeyType() == 17, "getKeyType");
        check("aes128-cts-hmac-sha1-96".equals(kk.getAlgorithm()), "algorithm AES128");
        check("RAW".equals(kk.getFormat()), "format RAW");
        check(Arrays.equals(keyBytes, kk.getEncoded()), "getEncoded contents");

        // constructor copies input; getEncoded returns copy
        byte[] in = new byte[]{1,2,3};
        KerberosKey kc = new KerberosKey(p, in, 17, 1);
        in[0] = 99;
        check(kc.getEncoded()[0] == 1, "constructor copies key bytes");
        byte[] g = kc.getEncoded();
        g[0] = 88;
        check(kc.getEncoded()[0] == 1, "getEncoded returns copy");

        // equals / hashCode: principal + version + keyType + material
        KerberosKey k1 = new KerberosKey(p, new byte[]{1,2,3}, 17, 2);
        KerberosKey k2 = new KerberosKey(new KerberosPrincipal("alice@EXAMPLE.COM"), new byte[]{1,2,3}, 17, 2);
        KerberosKey k3 = new KerberosKey(p, new byte[]{1,2,3}, 17, 3);
        KerberosKey k4 = new KerberosKey(p, new byte[]{1,2,3}, 18, 2);
        KerberosKey k5 = new KerberosKey(p, new byte[]{1,2,4}, 17, 2);
        check(k1.equals(k1), "equals self");
        check(k1.equals(k2), "equals same fields");
        check(!k1.equals(k3), "not equals different version");
        check(!k1.equals(k4), "not equals different keyType");
        check(!k1.equals(k5), "not equals different material");
        check(!k1.equals(null), "not equals null");
        check(!k1.equals("x"), "not equals non-key");
        check(k1.hashCode() == k2.hashCode(), "hashCode equal keys");

        // null key bytes -> NPE (KeyImpl clones)
        checkThrows(NullPointerException.class, () -> new KerberosKey(p, null, 17, 1), "null key bytes -> NPE");

        // destroy lifecycle
        KerberosKey dk = new KerberosKey(p, new byte[]{1,2,3,4}, 17, 5);
        check(!dk.isDestroyed(), "not destroyed initially");
        dk.destroy();
        check(dk.isDestroyed(), "destroyed flag");
        check("Destroyed KerberosKey".equals(dk.toString()), "toString after destroy");
        check(dk.hashCode() == 17, "hashCode after destroy == 17");
        check(dk.equals(dk), "destroyed equals self");
        checkThrows(IllegalStateException.class, () -> dk.getPrincipal(), "getPrincipal after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getVersionNumber(), "getVersionNumber after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getKeyType(), "getKeyType after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getAlgorithm(), "getAlgorithm after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getFormat(), "getFormat after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getEncoded(), "getEncoded after destroy -> ISE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
