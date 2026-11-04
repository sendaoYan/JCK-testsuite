/*
 * @test
 * @summary java.security.KeyStore conformance: white-box structure + black-box behavior.
 * @run main KeyStoreTest
 */

import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.io.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.nio.file.Files;
import java.security.KeyStoreException;


public class KeyStoreTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        check("PKCS12".equals(ks.getType()), "getType");
        check(ks.getProvider() != null, "getProvider");
        ks.load(null, null);
        check(ks.size() == 0, "size 0 after load(null,null)");
        check(!ks.aliases().hasMoreElements(), "no aliases after load");
        check(KeyStore.getDefaultType() != null, "getDefaultType non-null");

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey sk = kg.generateKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection("pw".toCharArray());
        ks.setEntry("secret", ske, pp);
        check(ks.containsAlias("secret"), "containsAlias");
        check(ks.isKeyEntry("secret"), "isKeyEntry");
        KeyStore.Entry got = ks.getEntry("secret", pp);
        check(got instanceof KeyStore.SecretKeyEntry, "getEntry SecretKeyEntry");
        check(ks.getCreationDate("secret") != null, "getCreationDate non-null");
    }

    // Fixed fixtures, generated once with JDK-27 keytool (RSA-2048 self-signed certs CN=jck-root / CN=jck-leaf,
    // validity 2026-11-03T04:09:52Z..2036-10-31T04:09:52Z, sig SHA384withRSA, root serial 0x70bdab2c498601da,
    // leaf serial 0xbc0944b4fd45f023; CRL v1 issued by jck-root, thisUpdate 2026-11-03, nextUpdate 2027-02-01,
    // revoked serial 0; PKCS#8 DER of the root private key). Tests decode at runtime; no external files.

    static final String ROOT_CERT_B64 =
        "MIIDLjCCAhagAwIBAgIIcL2rLEmGAdowDQYJKoZIhvcNAQEMBQAwPTELMAkGA1UEBhMC" + "\n" +
        "VVMxDTALBgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNrLXJvb3Qw" + "\n" +
        "HhcNMjYxMTAzMDQwOTUyWhcNMzYxMDMxMDQwOTUyWjA9MQswCQYDVQQGEwJVUzENMAsG" + "\n" +
        "A1UEChMEVGVzdDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdDCCASIwDQYJ" + "\n" +
        "KoZIhvcNAQEBBQADggEPADCCAQoCggEBALrM4ukZtyCiH9zsjkRMgOt6EYkhMv8IXY7t" + "\n" +
        "wYzWwLJnR9ED7BnleYFAaeSH17SWUAPdUBTH+ig57+2DI2Te3IJo974rgHjq94UXk5ez" + "\n" +
        "y1aUlrpe4SV+1NCnA/zg2E3lIOIaObZrTbBTT16jveIuX3Rqjbz9/z6Xic3Wvn2CqBNp" + "\n" +
        "1rnF1/GqhN6/6O0I+8el3ulMXzqmc21YPQuvpJDYuR1026ht3vqZhuY3elLEdvTQfOQa" + "\n" +
        "5w4xNGg6BlXycQsOXXP7Uqk8s5fTxBEKhR9gfABehJTuHwqEkeaQgT44xtgeaEOnbqH2" + "\n" +
        "thm0cucWQnSsGOJdR2IZ6RYVP+ix4hOZ3vsCAwEAAaMyMDAwHQYDVR0OBBYEFI92/CSa" + "\n" +
        "lBAhQnaSMXtuaF3G4f9TMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggEB" + "\n" +
        "AJy35loMlTYxvzsaqqlXMBOuaMULP/qZXyiZhX0H3sKtYxZVBdHLS/CG2k4mXtOQHNqq" + "\n" +
        "Gz+JQo0LS4VoxNCPrAky9tvubH4eSoF3xGJIdThVDBhcn0T2rYjb9z52xA1p+GIoc9Gi" + "\n" +
        "Fv6vh1l4OkhLq9ymIwnM+CjBUNOW6fwojo7c7o/qxk0+qPijanzhxh9ffIrRqQmQom9W" + "\n" +
        "pelY03y0XZKuAGxb0cuRBOCkH4Jg0RJhbJoR08wn3jEJdQ7t5I65VcOjJ0KzyRzV/g3V" + "\n" +
        "C0Wwnv9j7RSSuxdcwrNYVm3vgDQiZWlXDcYJob2fn2H+q0hohK0mdyw43fyDkxSsWiFq" + "\n" +
        "UF8=";
    static final String LEAF_CERT_B64 =
        "MIIDHjCCAgagAwIBAgIJALwJRLT9RfAjMA0GCSqGSIb3DQEBDAUAMD0xCzAJBgNVBAYT" + "\n" +
        "AlVTMQ0wCwYDVQQKEwRUZXN0MQwwCgYDVQQLEwNKQ0sxETAPBgNVBAMTCGpjay1sZWFm" + "\n" +
        "MB4XDTI2MTEwMzA0MDk1M1oXDTM2MTAzMTA0MDk1M1owPTELMAkGA1UEBhMCVVMxDTAL" + "\n" +
        "BgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNrLWxlYWYwggEiMA0G" + "\n" +
        "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDisnQcLGfp7REpIgOeehP9d5UOBnrncfNL" + "\n" +
        "m7LcJ+fRiK03KU//GKdls18S3ypr2eV13f28vdpPhVWpZtYEpcfEDWyvgX19nQj9+6zl" + "\n" +
        "serGqnnrPqMt6M4SNW028Dr7z+xG2TmanDUbSXzsvEWfb7sScH5KWrSKOIl++SOBa9s0" + "\n" +
        "nTlqadjGcn2PLBXlJUiLgk7+uj7o5tsf1nTmRd7cKpLW4BnIQVSFyOvrO94/SmzE/SQU" + "\n" +
        "a7J/trD30b1ma901A/MtaeYKAWRTlevmckGCZMn6UF8WXr9lxJW0T3UBWYqvBlsiym6p" + "\n" +
        "80/YefCqe+byY55i7WUVN5YTKSk3Um/yCnpFAgMBAAGjITAfMB0GA1UdDgQWBBQJj8/0" + "\n" +
        "KpspnGDPoSQb7ZCn72zWZjANBgkqhkiG9w0BAQwFAAOCAQEApZSVF4/jEZ2LmUCNryZ3" + "\n" +
        "AXrkDNSrWptBixVWu9vT0RGjXW7rK1OONxcrplssjGLVAnIqW+FuLMFzVy59yKQskGe8" + "\n" +
        "MdHNZdSpKhPO/kkSx6I4uQMCOl4wbPnzkjLZrDa2r4Qr8YQw2SZ9Bb+vaijYIk6gEpJ4" + "\n" +
        "cjBOOCyQFO+Dao6j71L4gxbl8zty5eA0jm2KpDULMXS5rRpKS2FOUHYR40gYpqyonHH5" + "\n" +
        "nzwJ4R1y0o/oct42BLim38UMruvf9I97/tShAvGtwfSofaGKWBwtTRmwwhc16NcL6VKk" + "\n" +
        "Ce9m+wx5FUgwcEGNjv+HJ4b+RgCiKtZWziHX4qrkXKGsKiKrwg==";
    static final String ROOT_KEY_B64 =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6zOLpGbcgoh/c7I5E" + "\n" +
        "TIDrehGJITL/CF2O7cGM1sCyZ0fRA+wZ5XmBQGnkh9e0llAD3VAUx/ooOe/tgyNk3tyC" + "\n" +
        "aPe+K4B46veFF5OXs8tWlJa6XuElftTQpwP84NhN5SDiGjm2a02wU09eo73iLl90ao28" + "\n" +
        "/f8+l4nN1r59gqgTada5xdfxqoTev+jtCPvHpd7pTF86pnNtWD0Lr6SQ2LkddNuobd76" + "\n" +
        "mYbmN3pSxHb00HzkGucOMTRoOgZV8nELDl1z+1KpPLOX08QRCoUfYHwAXoSU7h8KhJHm" + "\n" +
        "kIE+OMbYHmhDp26h9rYZtHLnFkJ0rBjiXUdiGekWFT/oseITmd77AgMBAAECggEAA8Px" + "\n" +
        "26o/3nqWJNag/ROR7tDFYZvCUq2hNn/aTKkXxBKj5K6JgMcFV31d9ttIlh1dxsN/ywcA" + "\n" +
        "Q0rDr4TpJcqMepWYwUsXv3vJ7A/TxMAEOOVJi2oWFogDdzEJVnyNkzadV3bDwg3CczL0" + "\n" +
        "fVgp9RhG+/i2G30bc/59OOi0h0u1G4PtbLdrO6vx6lFxJVX56fQiYm1rEGXEByD8diOV" + "\n" +
        "iYnmpxU5w+CAi0cfFzWmqrnd2nFhvbB7/+iX/ITFXO+ffPrMkbLEpiIOcIEWR5yclF6+" + "\n" +
        "cAdPWIqfr2dQz1lBK8rg3EI1HXOZcyxu3MLjgXUFWZQ7DwulCnUbn6JBenvWcIZ8vQKB" + "\n" +
        "gQDLmx5lP+Uqhi5SUJvo5EYgaD9lXv1FhToD/Kb2hXgb78v6Qw29OFr0GG+1wICoNeXZ" + "\n" +
        "8UAMUpyIU0jhpFDcPMQuWXlQX7i549Y+TJYKTMyst2y1gcG2yhxECnHSb2b++0X6KSlD" + "\n" +
        "p2oRuu2KpnBezbLh9YN6B7dPvo/H1JBlVh5mXQKBgQDq3qvDnTpAruJfAXNCP/CWGU58" + "\n" +
        "0RFoIIc0SWhzWNaqvxNO7c1CMhnw/ojuJrJ/OTEo8pMKzR8mykj7oBMqFgf/RYb4IfRY" + "\n" +
        "dnKnO2fmibdih6PCdvIvVph41l6CbBDhulaxwdwIUFJZXAlxJeaNw8ypFgibHF3pMuXg" + "\n" +
        "aBt+rBlVNwKBgDqH5T0GGUMYP8i6E5fnx6EZVuA1ZmnN2te6keTciaK+VM0k6ZYz1THG" + "\n" +
        "tJZzOIkitWjpyZfBR+6IwrLbIcOuVFdesVlBq0EFFlniel+LpGsib8Xd/2okv+tvdTkh" + "\n" +
        "fWJfqcY4fvxL7Focep0ZK8SVLY93nMw/25q5FWxE1x3KnAKhAoGBAK/aKsbauNvnVSyH" + "\n" +
        "HHI1B4CK0B5aNus8t56pe+ApWwka6W0GVdHDS3MKUSHmrWzeOHWNMhU7QaC8gmqMDu1K" + "\n" +
        "84/o0caFPbribLPTodaVtE28extGDcztzFllP/vsx7MK3pBXMtSytwWbCKLEGtx4Botv" + "\n" +
        "KOBiG9pXtpAPws5UMt9/AoGAYjejGOvORTlBCw8e5za2yOP6FfVV6xRkCMbpemv0+vM6" + "\n" +
        "t0C4GQ8WLSqyUh2US0Um8RYYeuym0LcKaRGhEoEURZ8R2evk0Jkdw7EM8JZc5m7w+i8n" + "\n" +
        "3wje9tols+cDrNd4nZd8zjNC7U+t57kCp84zYFdUkGUjpIxOrSSWW1BMC6U=";
    static final String ROOT_CRL_B64 =
        "MIIBmTCBgjANBgkqhkiG9w0BAQwFADA9MQswCQYDVQQGEwJVUzENMAsGA1UEChMEVGVz" + "\n" +
        "dDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdBcNMjYxMTAzMDQwOTU0WhcN" + "\n" +
        "MjcwMjAxMDQwOTU0WjAUMBICAQAXDTI2MTEwMzA0MDk1NFowDQYJKoZIhvcNAQEMBQAD" + "\n" +
        "ggEBAFuf6Ir8rfNZ9yAHZLvA03bOZYbNsSC2JTVkO4ZJtm5hquLYU2YA4X/v/gBOuvqm" + "\n" +
        "NXbroPJG2ZSxObRiS+N8Q3XBJGJK4ME8VmRUNxFw6sSPRGBSFvD7jwJOH+YRkHPfJ/2H" + "\n" +
        "5w7fT+4zgvkviCJ/skB2trCrKm1EwRrT7mSKTY9NR3NTau5T2jQ9eJF7km72Bz0Y1LHo" + "\n" +
        "OKXAOF3d2ajb9fbLuDJV16MTl+j+Ar5X2jalGVg1M00omc5Re2eZQJpH9IlWPnoGHmC1" + "\n" +
        "MvNbcH5d1YcI0sX9AOUzcJItQkv+IipcErE5O81X12CKImLCsjD6Wv1lSz7zhgHr7fgA" + "\n" +
        "/z+EmMM=";
    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }
    static java.security.cert.X509Certificate fixtureCert(String b64) throws Exception {
        return (java.security.cert.X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(fromB64(b64)));
    }
    static java.security.cert.X509CRL fixtureCrl() throws Exception {
        return (java.security.cert.X509CRL) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCRL(new java.io.ByteArrayInputStream(fromB64(ROOT_CRL_B64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // In-memory PKCS12 build -> store -> reload round trip with a private key and a trusted cert
        X509Certificate rc = fixtureCert(ROOT_CERT_B64);
        PrivateKey rk = KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(fromB64(ROOT_KEY_B64)));
        KeyStore ks = KeyStore.getInstance("PKCS12");
        check(ks.getType().equals("PKCS12"), "getType");
        check(ks.getProvider() != null, "provider non-null");
        ks.load(null, null);
        check(ks.size() == 0, "size 0 after load(null,null)");
        ks.setKeyEntry("rootkey", rk, "keypw".toCharArray(), new Certificate[]{rc});
        ks.setCertificateEntry("rootcert", rc);
        check(ks.size() == 2, "size 2");
        check(ks.containsAlias("rootkey") && ks.containsAlias("rootcert"), "containsAlias both");
        check(ks.isKeyEntry("rootkey") && !ks.isCertificateEntry("rootkey"), "key entry classification");
        check(ks.isCertificateEntry("rootcert") && !ks.isKeyEntry("rootcert"), "cert entry classification");
        check(!ks.containsAlias("missing"), "missing alias");
        // key + chain access, wrong password
        PrivateKey back = (PrivateKey) ks.getKey("rootkey", "keypw".toCharArray());
        check(back.equals(rk), "getKey round trip");
        check(ks.getCertificateChain("rootkey").length == 1 && ks.getCertificateChain("rootkey")[0].equals(rc), "chain");
        check(ks.getCertificate("rootcert").equals(rc), "getCertificate");
        // getCertificateAlias returns the first matching alias in iteration order (key chain inserted first)
        check(ks.getCertificateAlias(rc).equals("rootkey"), "getCertificateAlias first match");
        KeyStore solo = KeyStore.getInstance("PKCS12");
        solo.load(null, null);
        solo.setCertificateEntry("only", rc);
        check(solo.getCertificateAlias(rc).equals("only"), "getCertificateAlias cert-only store");
        check(ks.getCertificate("missing") == null, "getCertificate missing null");
        try {
            ks.getKey("rootkey", "wrong".toCharArray());
            check(false, "wrong password throws");
        } catch (UnrecoverableKeyException uke) {
            check(true, "wrong password UnrecoverableKeyException");
        }
        // entry API round trip + attributes
        KeyStore.PrivateKeyEntry pe = (KeyStore.PrivateKeyEntry)
            ks.getEntry("rootkey", new KeyStore.PasswordProtection("keypw".toCharArray()));
        check(pe.getPrivateKey().equals(rk), "entry private key");
        check(pe.getCertificate().equals(rc), "entry chain");
        KeyStore.TrustedCertificateEntry te = (KeyStore.TrustedCertificateEntry) ks.getEntry("rootcert", null);
        check(te.getTrustedCertificate().equals(rc), "entry trusted cert");
        // PKCS12 synthesizes friendlyName/localKeyID attributes on getEntry
        check(pe.getAttributes() != null, "entry attributes non-null");
        boolean friendlyName = false;
        for (KeyStore.Entry.Attribute at : pe.getAttributes()) {
            if (at.getName().equals("1.2.840.113549.1.9.20")) friendlyName = true;
            check(at.getValue() != null, "attribute value non-null");
        }
        check(friendlyName, "friendlyName attribute synthesized");
        check(te.getAttributes() != null, "trusted entry attributes non-null");
        // store/reload
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ks.store(bos, "storepw".toCharArray());
        KeyStore ks2 = KeyStore.getInstance("PKCS12");
        ks2.load(new ByteArrayInputStream(bos.toByteArray()), "storepw".toCharArray());
        check(ks2.size() == 2, "reloaded size");
        check(ks2.getCertificate("rootcert").equals(rc), "reloaded cert equals");
        check(((PrivateKey) ks2.getKey("rootkey", "keypw".toCharArray())).equals(rk), "reloaded key equals");
        // wrong store password on reload
        try {
            KeyStore bad = KeyStore.getInstance("PKCS12");
            bad.load(new ByteArrayInputStream(bos.toByteArray()), "nope".toCharArray());
            check(false, "wrong store password throws");
        } catch (IOException ioe) {
            check(true, "wrong store password IOException");
        }
        // File-based factories (tmp file, cleaned in finally)
        java.nio.file.Path p = Files.createTempFile("jck-ks", ".p12");
        try (OutputStream os = Files.newOutputStream(p)) {
            ks.store(os, "filepw".toCharArray());
        }
        KeyStore ks3 = KeyStore.getInstance(p.toFile(), "filepw".toCharArray());
        check(ks3.containsAlias("rootcert"), "File factory load");
        // wrong password on a key-bearing PKCS12 surfaces as IOException("keystore password was incorrect")
        try {
            KeyStore.getInstance(p.toFile(), "nope".toCharArray());
            check(false, "wrong file password throws");
        } catch (IOException | IllegalArgumentException e) {
            check(true, "wrong file password rejected");
        } finally {
            Files.deleteIfExists(p);
        }
        // getAttributes(String) default
        check(ks2.getAttributes("unknown.type").isEmpty(), "getAttributes unknown type empty");
        // delete + aliases enumeration sorted
        ks2.deleteEntry("rootcert");
        check(ks2.size() == 1 && !ks2.containsAlias("rootcert"), "deleteEntry");
        List<String> aliases = new ArrayList<>();
        for (Enumeration<String> e = ks2.aliases(); e.hasMoreElements(); ) aliases.add(e.nextElement());
        Collections.sort(aliases);
        check(aliases.equals(List.of("rootkey")), "aliases after delete");
        // creation date/instant agree
        check(ks.getCreationDate("rootkey") != null && ks.getCreationInstant("rootkey") != null, "creation date/instant");
        check(ks.getCreationDate("rootkey").toInstant().equals(ks.getCreationInstant("rootkey")), "date equals instant");
        // entryInstanceOf
        check(ks.entryInstanceOf("rootkey", KeyStore.PrivateKeyEntry.class), "entryInstanceOf true");
        check(!ks.entryInstanceOf("rootcert", KeyStore.PrivateKeyEntry.class), "entryInstanceOf false");
        // setKeyEntry replacing an entry keeps classification
        ks.setCertificateEntry("rootcert2", rc);
        check(ks.getCertificateAlias(rc) != null, "alias of reused cert");
        // exceptions: unknown type / not loaded
        try { KeyStore.getInstance("NoSuchType"); check(false, "unknown type"); }
        catch (KeyStoreException kse) { check(true, "unknown type KeyStoreException"); }
        KeyStore unloaded = KeyStore.getInstance("PKCS12");
        try { unloaded.size(); check(false, "not loaded"); }
        catch (KeyStoreException kse) { check(true, "not loaded KeyStoreException"); }
        // null alias / null key / unknown provider
        try { ks.deleteEntry(null); check(false, "null alias NPE"); }
        catch (NullPointerException npe) { check(true, "null alias NPE"); }
        try { ks.setKeyEntry("k", (java.security.Key) null, null, null); check(false, "null key KeyStoreException"); }
        catch (KeyStoreException kse) { check(true, "null key KeyStoreException"); }
        try { KeyStore.getInstance("PKCS12", "NoSuchProvider"); check(false, "unknown provider"); }
        catch (NoSuchProviderException nspe) { check(true, "unknown provider NoSuchProviderException"); }
        check(KeyStore.getInstance("PKCS12", ks.getProvider()).getType().equals("PKCS12"), "provider-object factory");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
