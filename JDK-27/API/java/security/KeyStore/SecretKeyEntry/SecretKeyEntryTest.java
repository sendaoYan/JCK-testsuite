/*
 * @test
 * @summary java.security.KeyStore.SecretKeyEntry conformance: white-box structure + black-box behavior.
 * @run main SecretKeyEntryTest
 */

import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.io.*;
import java.util.*;
import java.security.PKCS12Attribute;


public class SecretKeyEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        SecretKey sk = KeyGenerator.getInstance("AES").generateKey();
        KeyStore.SecretKeyEntry e = new KeyStore.SecretKeyEntry(sk);
        check(sk.equals(e.getSecretKey()), "getSecretKey");
        check(e.getAttributes().isEmpty(), "getAttributes empty");
        check(e instanceof KeyStore.Entry, "implements Entry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // secret key entry: key + optional attributes, equality, round trip
        javax.crypto.SecretKey k1 = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        KeyStore.SecretKeyEntry e = new KeyStore.SecretKeyEntry(k1);
        check(e.getSecretKey() == k1, "secret key");
        check(e.getAttributes().isEmpty(), "no attributes");
        // JDK-27 SecretKeyEntry does not override equals: identity semantics
        check(e.equals(e), "reflexive equals");
        KeyStore.SecretKeyEntry same = new KeyStore.SecretKeyEntry(k1);
        check(!e.equals(same), "identity equality, same key still unequal");
        check(e.toString().contains("AES"), "toString names algorithm");
        javax.crypto.SecretKey other = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        check(!e.equals(new KeyStore.SecretKeyEntry(other)), "different key unequal");
        KeyStore.Entry.Attribute a = new PKCS12Attribute("1.2.840.113549.1.9.20", "0C066A636B2D6E616D65");
        KeyStore.SecretKeyEntry ea = new KeyStore.SecretKeyEntry(k1, Set.of(a));
        check(!e.equals(ea), "attributes affect equality");
        check(ea.getAttributes().contains(a), "attributes retained");
        // full round trip
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection("pw".toCharArray());
        ks.setEntry("s", e, pp);
        KeyStore.SecretKeyEntry back = (KeyStore.SecretKeyEntry) ks.getEntry("s", pp);
        check(Arrays.equals(back.getSecretKey().getEncoded(), k1.getEncoded()), "round trip key bytes");
        // null key rejected
        try { new KeyStore.SecretKeyEntry(null); check(false, "null key NPE"); }
        catch (NullPointerException npe) { check(true, "null key NPE"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        javax.crypto.SecretKey aes = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(aes);

        // JKS rejects secret keys outright (PrivateKey-only format)
        KeyStore jks = KeyStore.getInstance("JKS");
        jks.load(null, null);
        boolean rejected = false;
        try { jks.setKeyEntry("s", aes, "pw".toCharArray(), null); }
        catch (java.security.KeyStoreException kse) { rejected = true; }
        check(rejected, "JKS setKeyEntry SecretKey KeyStoreException");

        // JCEKS full loop: setEntry -> getEntry/getKey + delete lifecycle
        char[] pw = "pw".toCharArray();
        KeyStore.PasswordProtection prot = new KeyStore.PasswordProtection(pw);
        KeyStore jce = KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setEntry("s", entry, prot);
        check(jce.isKeyEntry("s") && !jce.isCertificateEntry("s"),
                "JCEKS entry classification");
        check(jce.getCertificateChain("s") == null, "JCEKS secret entry null chain");
        KeyStore.Entry back = jce.getEntry("s", prot);
        check(back instanceof KeyStore.SecretKeyEntry, "JCEKS getEntry type round-trip");
        check(java.util.Arrays.equals(
                ((KeyStore.SecretKeyEntry) back).getSecretKey().getEncoded(),
                aes.getEncoded()), "JCEKS key bytes round-trip");
        check("AES".equals(jce.getKey("s", pw).getAlgorithm()), "JCEKS getKey algorithm");
        boolean unrecoverable = false;
        try { jce.getKey("s", "wrong".toCharArray()); }
        catch (java.security.UnrecoverableKeyException uke) { unrecoverable = true; }
        check(unrecoverable, "JCEKS wrong password UnrecoverableKeyException");
        jce.deleteEntry("s");
        check(!jce.containsAlias("s") && jce.size() == 0, "deleteEntry removes secret");
        check(jce.getEntry("s", prot) == null, "getEntry after delete null");
    }
}
