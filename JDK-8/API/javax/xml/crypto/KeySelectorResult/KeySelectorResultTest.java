/*
 * @test
 * @summary javax.xml.crypto.KeySelectorResult conformance.
 * @run main KeySelectorResultTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorResult;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeySelectorResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.KeySelectorResult");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getKey"), "has getKey");
        // custom result carrying the key by identity
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final KeyPair kp = kpg.generateKeyPair();
        KeySelectorResult r = new KeySelectorResult() {
            public java.security.Key getKey() { return kp.getPublic(); }
        };
        check(r.getKey() == kp.getPublic(), "getKey identity round-trip");
        // result permitting a null key (selector could not resolve one)
        KeySelectorResult nullKey = new KeySelectorResult() {
            public java.security.Key getKey() { return null; }
        };
        check(nullKey.getKey() == null, "null key representable");
        // singletonKeySelector results expose the fixed key
        KeySelectorResult sr = KeySelector.singletonKeySelector(kp.getPublic())
            .select(null, KeySelector.Purpose.VERIFY, null, null);
        check(sr != null && sr.getKey() == kp.getPublic(), "singleton result key identity");
        // a secret key flows through equally
        final javax.crypto.SecretKey sk = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        KeySelectorResult skr = new KeySelectorResult() {
            public java.security.Key getKey() { return sk; }
        };
        check(skr.getKey() == sk && skr.getKey().getAlgorithm().equals("HmacSHA256"), "secret key result");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
