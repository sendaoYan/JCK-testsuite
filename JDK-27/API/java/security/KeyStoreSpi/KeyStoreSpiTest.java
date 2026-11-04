/*
 * @test
 * @summary java.security.KeyStoreSpi conformance: white-box structure + black-box behavior.
 * @run main KeyStoreSpiTest
 */

import java.lang.reflect.*;

import java.io.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.security.KeyStore;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.Key;


public class KeyStoreSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStoreSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"engineAliases", "engineContainsAlias", "engineDeleteEntry", "engineEntryInstanceOf", "engineGetAttributes", "engineGetCertificate", "engineGetCertificateAlias", "engineGetCertificateChain", "engineGetCreationDate", "engineGetCreationInstant", "engineGetEntry", "engineGetKey", "engineIsCertificateEntry", "engineIsKeyEntry", "engineLoad", "engineProbe", "engineSetCertificateEntry", "engineSetEntry", "engineSetKeyEntry", "engineSize", "engineStore"});
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

        // Minimal concrete KeyStoreSpi exercising the engine-method contracts
        X509Certificate rc = fixtureCert(ROOT_CERT_B64);
        PrivateKey rk = KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(fromB64(ROOT_KEY_B64)));
        class MapSpi extends KeyStoreSpi {
            final java.util.Date created = new java.util.Date(0L);
            final Map<String, KeyStore.Entry> entries = new LinkedHashMap<>();
            public Key engineGetKey(String alias, char[] password) {
                return entries.get(alias) instanceof KeyStore.PrivateKeyEntry pke ? pke.getPrivateKey() : null;
            }
            public Certificate[] engineGetCertificateChain(String alias) {
                return entries.get(alias) instanceof KeyStore.PrivateKeyEntry pke ? pke.getCertificateChain() : null;
            }
            public Certificate engineGetCertificate(String alias) {
                return entries.get(alias) instanceof KeyStore.TrustedCertificateEntry tce ? tce.getTrustedCertificate() : null;
            }
            public java.util.Date engineGetCreationDate(String alias) { return entries.containsKey(alias) ? created : null; }
            public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
                entries.put(alias, new KeyStore.PrivateKeyEntry((PrivateKey) key, chain));
            }
            public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
                throw new KeyStoreException("byte-key entries not supported");
            }
            public void engineSetCertificateEntry(String alias, Certificate cert) {
                entries.put(alias, new KeyStore.TrustedCertificateEntry(cert));
            }
            public void engineDeleteEntry(String alias) { entries.remove(alias); }
            public Enumeration<String> engineAliases() { return Collections.enumeration(entries.keySet()); }
            public boolean engineContainsAlias(String alias) { return entries.containsKey(alias); }
            public int engineSize() { return entries.size(); }
            public boolean engineIsKeyEntry(String alias) {
                return entries.get(alias) instanceof KeyStore.PrivateKeyEntry
                    || entries.get(alias) instanceof KeyStore.SecretKeyEntry;
            }
            public boolean engineIsCertificateEntry(String alias) {
                return entries.get(alias) instanceof KeyStore.TrustedCertificateEntry;
            }
            public String engineGetCertificateAlias(Certificate cert) {
                for (Map.Entry<String, KeyStore.Entry> en : entries.entrySet()) {
                    if (en.getValue() instanceof KeyStore.TrustedCertificateEntry tce
                        && tce.getTrustedCertificate().equals(cert)) return en.getKey();
                }
                return null;
            }
            public void engineStore(OutputStream stream, char[] password) { }
            public void engineLoad(InputStream stream, char[] password) { }
            public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter prot) {
                return entries.get(alias);
            }
            public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter prot) {
                entries.put(alias, entry);
            }
            public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> cls) {
                return cls.isInstance(entries.get(alias));
            }
        }
        MapSpi spi = new MapSpi();
        // trusted-cert path
        spi.engineSetCertificateEntry("t", rc);
        check(spi.engineSize() == 1 && spi.engineContainsAlias("t"), "set/size/contains");
        check(spi.engineIsCertificateEntry("t") && !spi.engineIsKeyEntry("t"), "classification");
        check(spi.engineGetCertificate("t").equals(rc), "getCertificate");
        check(spi.engineGetCertificateAlias(rc).equals("t"), "certificateAlias");
        check(spi.engineGetCertificateAlias(null) == null, "unknown cert null");
        check(spi.engineGetCreationDate("t").getTime() == 0L, "creation date fixed");
        check(spi.engineGetCreationInstant("t").equals(java.time.Instant.EPOCH), "instant derived from date");
        check(spi.engineGetKey("t", null) == null, "no key in cert entry");
        check(spi.engineGetCertificateChain("t") == null, "no chain in cert entry");
        // private-key path
        spi.engineSetKeyEntry("k", rk, "pw".toCharArray(), new Certificate[]{rc});
        check(spi.engineIsKeyEntry("k"), "key entry classification");
        check(spi.engineGetKey("k", null).equals(rk), "engineGetKey");
        check(spi.engineGetCertificateChain("k")[0].equals(rc), "engineGetCertificateChain");
        check(spi.engineGetCertificate("k") == null, "cert entry lookup null for key entry");
        // entry API
        KeyStore.Entry got = spi.engineGetEntry("k", null);
        check(got instanceof KeyStore.PrivateKeyEntry, "engineGetEntry");
        spi.engineSetEntry("s", new KeyStore.SecretKeyEntry(
            javax.crypto.KeyGenerator.getInstance("AES").generateKey()), null);
        check(spi.engineEntryInstanceOf("s", KeyStore.SecretKeyEntry.class), "entryInstanceOf true");
        check(!spi.engineEntryInstanceOf("s", KeyStore.PrivateKeyEntry.class), "entryInstanceOf false");
        check(spi.engineGetAttributes("s").isEmpty(), "default engineGetAttributes empty");
        // aliases ordered by insertion (LinkedHashMap)
        List<String> names = new ArrayList<>();
        for (Enumeration<String> e = spi.engineAliases(); e.hasMoreElements(); ) names.add(e.nextElement());
        check(names.equals(List.of("t", "k", "s")), "aliases ordered");
        // byte-key engineSetKeyEntry throws per contract of this SPI
        try {
            spi.engineSetKeyEntry("b", new byte[1], null);
            check(false, "byte-key set throws");
        } catch (KeyStoreException kse) {
            check(true, "byte-key set KeyStoreException");
        }
        // load/store no-ops accepted
        spi.engineLoad(null, null);
        spi.engineStore(new ByteArrayOutputStream(), null);
        check(spi.engineSize() == 3, "size after load/store no-ops");
        // delete
        spi.engineDeleteEntry("t");
        check(spi.engineSize() == 2 && !spi.engineContainsAlias("t"), "engineDeleteEntry");
        // engineProbe default returns false for a null-free stream it cannot identify
        check(!spi.engineProbe(new ByteArrayInputStream(new byte[0])), "engineProbe default false");

    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void ctMsg(Class<? extends Throwable> expected, String frag, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(frag)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but nothing was thrown");
    }

    static class FakeKey implements PrivateKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "RAW"; }
        public byte[] getEncoded() { return new byte[]{1}; }
    }

    static class FakePublicKey implements java.security.PublicKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "X.509"; }
        public byte[] getEncoded() { return new byte[]{2}; }
    }

    // Minimal Certificate the JDK-default entry adapters can chew on (chain building calls getPublicKey()).
    // Note: qualified because JDK 26+ also has a sealed interface java.security.Certificate.
    static class FakeCertificate extends java.security.cert.Certificate {
        FakeCertificate() { super("Fake"); }
        public byte[] getEncoded() { return new byte[]{3}; }
        public java.security.PublicKey getPublicKey() { return new FakePublicKey(); }
        public String toString() { return "FakeCertificate"; }
        public void verify(java.security.PublicKey key) { }
        public void verify(java.security.PublicKey key, String sigProvider) { }
    }

    // Records every call; does NOT override engineStore(param)/engineLoad(param)/
    // engineGetEntry/engineSetEntry/engineEntryInstanceOf/engineGetCreationInstant/
    // engineGetAttributes/engineProbe so the JDK defaults are exercised.
    static class DefaultsSpi extends KeyStoreSpi {
        final Map<String, Key> keys = new LinkedHashMap<>();
        final Map<String, Certificate> certs = new LinkedHashMap<>();
        int loadCalls; InputStream lastStream; char[] lastPass; Key lastSetKey; char[] lastSetPass;
        Certificate[] lastSetChain; String lastCertAlias;
        public Key engineGetKey(String alias, char[] password) { return keys.get(alias); }
        public Certificate[] engineGetCertificateChain(String alias) { return null; }
        public Certificate engineGetCertificate(String alias) { return certs.get(alias); }
        public java.util.Date engineGetCreationDate(String alias) { return alias.startsWith("noentry") ? null : new java.util.Date(0L); }
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
            keys.put(alias, key); lastSetKey = key; lastSetPass = password; lastSetChain = chain;
        }
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) { }
        public void engineSetCertificateEntry(String alias, Certificate cert) {
            certs.put(alias, cert); lastCertAlias = alias;
        }
        public void engineDeleteEntry(String alias) { keys.remove(alias); certs.remove(alias); }
        public Enumeration<String> engineAliases() { return Collections.enumeration(keys.keySet()); }
        public boolean engineContainsAlias(String alias) { return keys.containsKey(alias) || certs.containsKey(alias); }
        public int engineSize() { return keys.size() + certs.size(); }
        public boolean engineIsKeyEntry(String alias) { return keys.containsKey(alias); }
        public boolean engineIsCertificateEntry(String alias) { return certs.containsKey(alias); }
        public String engineGetCertificateAlias(Certificate cert) { return null; }
        public void engineStore(OutputStream stream, char[] password) { }
        public void engineLoad(InputStream stream, char[] password) { loadCalls++; lastStream = stream; lastPass = password; }
        void doStoreParam(KeyStore.LoadStoreParameter p) throws Exception { engineStore(p); }
        void doLoadParam(KeyStore.LoadStoreParameter p) throws Exception { engineLoad(p); }
        KeyStore.Entry doGetEntry(String a, KeyStore.ProtectionParameter p) throws Exception { return engineGetEntry(a, p); }
        void doSetEntry(String a, KeyStore.Entry e, KeyStore.ProtectionParameter p) throws Exception { engineSetEntry(a, e, p); }
        boolean doEntryInstanceOf(String a, Class<? extends KeyStore.Entry> c) { return engineEntryInstanceOf(a, c); }
        java.time.Instant doGetInstant(String a) { return engineGetCreationInstant(a); }
        Set<KeyStore.Entry.Attribute> doGetAttrs(String a) { return engineGetAttributes(a); }
        boolean doProbe(InputStream in) throws Exception { return engineProbe(in); }
    }

    static KeyStore.ProtectionParameter ppParam(String pw) {
        return new KeyStore.PasswordProtection(pw.toCharArray());
    }

    static KeyStore.LoadStoreParameter pwParam(String pw) {
        return () -> new KeyStore.PasswordProtection(pw.toCharArray());
    }

    static void testContractDeep() throws Exception {
        Class<?> c = KeyStoreSpi.class;

        // ---- structure: 16 abstract engine methods (unchanged since JDK 8); JDK 9+ adds defaults ----
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        Set<String> names = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 16, "16 abstract engine methods, got " + abstractCount + ": " + names);
        Method m = c.getDeclaredMethod("engineStore", KeyStore.LoadStoreParameter.class);
        check(!Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "engineStore(LoadStoreParameter) public default");
        m = c.getDeclaredMethod("engineLoad", KeyStore.LoadStoreParameter.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineLoad(LoadStoreParameter) default");
        m = c.getDeclaredMethod("engineGetEntry", String.class, KeyStore.ProtectionParameter.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineGetEntry default");
        m = c.getDeclaredMethod("engineSetEntry", String.class, KeyStore.Entry.class, KeyStore.ProtectionParameter.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineSetEntry default");
        m = c.getDeclaredMethod("engineEntryInstanceOf", String.class, Class.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineEntryInstanceOf default");
        m = c.getDeclaredMethod("engineGetCreationInstant", String.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineGetCreationInstant default (JDK 9+)");
        m = c.getDeclaredMethod("engineGetAttributes", String.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineGetAttributes default (JDK 9+)");
        m = c.getDeclaredMethod("engineProbe", InputStream.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineProbe default (JDK 9+)");

        DefaultsSpi ks = new DefaultsSpi();
        FakeKey fk = new FakeKey();
        FakeCertificate fc = new FakeCertificate();

        // ---- engineStore(LoadStoreParameter) default: unsupported, also for null ----
        ct(UnsupportedOperationException.class, () -> ks.doStoreParam(null), "default engineStore(null param) -> UOE");
        ct(UnsupportedOperationException.class, () -> ks.doStoreParam(pwParam("p")), "default engineStore(param) -> UOE");

        // ---- engineLoad(LoadStoreParameter) default: delegates with password ----
        ks.doLoadParam(null);
        check(ks.loadCalls == 1 && ks.lastStream == null && ks.lastPass == null,
                "default engineLoad(null param) delegates engineLoad(null,null)");
        ks.doLoadParam(pwParam("pw"));
        check(ks.loadCalls == 2 && ks.lastStream == null && Arrays.equals(ks.lastPass, "pw".toCharArray()),
                "JDK 9+: default engineLoad(param) delegates engineLoad(null,password) (JDK 8 threw UOE)");

        // ---- engineGetEntry defaults ----
        check(ks.doGetEntry("absent", ppParam("p")) == null, "default engineGetEntry absent alias -> null");
        ks.keys.put("k1", fk);
        ctMsg(NullPointerException.class, "invalid null input",
                () -> ks.doGetEntry("k1", ppParam("p")),
                "key entry + PasswordProtection builds PrivateKeyEntry (chain null -> NPE from entry ctor)");
        ctMsg(NullPointerException.class, "handler must not be null",
                () -> ks.doGetEntry("k1", new KeyStore.CallbackHandlerProtection(null)),
                "key entry + CallbackHandlerProtection(null handler) -> NPE");
        ct(java.security.UnrecoverableKeyException.class, () -> ks.doGetEntry("k1", null),
                "key entry + null protection -> UnrecoverableKeyException");
        ks.certs.put("c1", fc);
        KeyStore.Entry certEntry = ks.doGetEntry("c1", null);
        check(certEntry instanceof KeyStore.TrustedCertificateEntry
                && ((KeyStore.TrustedCertificateEntry) certEntry).getTrustedCertificate() == fc,
                "cert entry + null protection -> TrustedCertificateEntry with same cert");

        // ---- engineSetEntry defaults ----
        javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        ks.doSetEntry("s1", new KeyStore.SecretKeyEntry(sk), ppParam("p"));
        check(ks.keys.get("s1") == sk, "default engineSetEntry(SecretKeyEntry) -> engineSetKeyEntry(key)");
        check(Arrays.equals(ks.lastSetPass, "p".toCharArray()), "default engineSetEntry passes password through");
        ks.doSetEntry("t1", new KeyStore.TrustedCertificateEntry(fc), null);
        check(ks.certs.get("t1") == fc && ks.lastCertAlias.equals("t1"),
                "default engineSetEntry(TrustedCertificateEntry, null prot) -> engineSetCertificateEntry");
        ks.doSetEntry("p1", new KeyStore.PrivateKeyEntry(fk, new Certificate[]{fc}), ppParam("p"));
        check(ks.keys.get("p1") == fk && ks.lastSetChain != null && ks.lastSetChain.length == 1,
                "default engineSetEntry(PrivateKeyEntry) -> engineSetKeyEntry with chain");

        // ---- engineEntryInstanceOf defaults ----
        // "k1" is a key entry with a null chain: PrivateKeyEntry requires a chain, so it classifies as SecretKeyEntry.
        check(!ks.doEntryInstanceOf("k1", KeyStore.PrivateKeyEntry.class),
                "default engineEntryInstanceOf: key entry + null chain not PrivateKeyEntry");
        check(ks.doEntryInstanceOf("k1", KeyStore.SecretKeyEntry.class),
                "default engineEntryInstanceOf: key entry + null chain is SecretKeyEntry");
        check(ks.doEntryInstanceOf("t1", KeyStore.TrustedCertificateEntry.class),
                "default engineEntryInstanceOf: cert entry is TrustedCertificateEntry");
        check(!ks.doEntryInstanceOf("absent", KeyStore.TrustedCertificateEntry.class),
                "default engineEntryInstanceOf: absent alias false");

        // ---- JDK 9+ defaults: engineGetCreationInstant / engineGetAttributes / engineProbe ----
        check(java.time.Instant.EPOCH.equals(ks.doGetInstant("k1")),
                "default engineGetCreationInstant derives Instant.EPOCH from engineGetCreationDate");
        check(ks.doGetInstant("noentry") == null,
                "default engineGetCreationInstant is null-safe when engineGetCreationDate returns null");
        check(ks.doGetAttrs("k1").isEmpty(), "default engineGetAttributes returns empty Set");
        ct(NullPointerException.class, () -> ks.doProbe(null), "default engineProbe(null) -> NPE");
        check(!ks.doProbe(new ByteArrayInputStream(new byte[0])), "default engineProbe(unknown stream) -> false");

        // ---- facade loop: real JKS keystore SPI driven through the public KeyStore facade ----
        Certificate realCert = fixtureCert(ROOT_CERT_B64);
        PrivateKey realKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(fromB64(ROOT_KEY_B64)));
        KeyStore jks = KeyStore.getInstance("JKS");
        jks.load(null, null);
        check(jks.size() == 0 && !jks.aliases().hasMoreElements(), "facade fresh JKS empty");
        jks.setKeyEntry("fk", realKey, "pw".toCharArray(), new Certificate[]{realCert});
        check(jks.isKeyEntry("fk"), "facade setKeyEntry classified as key entry");
        Key gotKey = jks.getKey("fk", "pw".toCharArray());
        check(gotKey != null && gotKey.equals(realKey) && "PKCS#8".equals(gotKey.getFormat()),
                "facade getKey round-trips an equal PKCS#8 private key");
        ct(java.security.UnrecoverableKeyException.class,
                () -> jks.getKey("fk", "wrong".toCharArray()),
                "facade JKS getKey with wrong password -> UnrecoverableKeyException");
        check(jks.getCertificateChain("fk").length == 1
                && jks.getCertificateChain("fk")[0].equals(realCert), "facade chain preserved");
        check(jks.getCreationDate("fk") != null, "facade creationDate non-null");
        jks.setCertificateEntry("fc", realCert);
        check(jks.getCertificate("fc") == realCert && jks.isCertificateEntry("fc"), "facade cert entry same instance");
        check(jks.containsAlias("fk") && jks.containsAlias("fc") && jks.size() == 2, "facade size/contains");
        check(jks.containsAlias("FK"), "JKS aliases are case-insensitive (stored lower-case)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        jks.store(bos, "pw".toCharArray());
        check(bos.size() > 0, "facade store produced bytes");
        KeyStore jks2 = KeyStore.getInstance("JKS");
        jks2.load(new ByteArrayInputStream(bos.toByteArray()), "pw".toCharArray());
        check(jks2.size() == 2 && jks2.getKey("fk", "pw".toCharArray()).equals(realKey)
                && jks2.getCertificate("fc").equals(realCert),
                "facade store/load round-trip preserves key and cert entries");
        ct(IOException.class,
                () -> {
                    KeyStore bad = KeyStore.getInstance("JKS");
                    bad.load(new ByteArrayInputStream(bos.toByteArray()), "bad".toCharArray());
                },
                "facade JKS load with wrong password -> IOException");
        jks.deleteEntry("fc");
        check(jks.size() == 1 && !jks.containsAlias("fc"), "facade deleteEntry removes entry");
        KeyStore uninit = KeyStore.getInstance("JKS");
        ct(KeyStoreException.class, () -> uninit.size(), "facade size before load -> KeyStoreException");
        ct(KeyStoreException.class, () -> uninit.getKey("x", null), "facade getKey before load -> KeyStoreException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- contract deepening: JDK default behaviors of the non-abstract engine methods + JKS facade loop ----
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
