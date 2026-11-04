/*
 * @test
 * @summary javax.crypto.EncryptedPrivateKeyInfo conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main EncryptedPrivateKeyInfoTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class EncryptedPrivateKeyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final char[] PASSWORD = "jck-passphrase".toCharArray();
    // fixed 16-byte encrypted-data payload
    static final byte[] DATA = new byte[16];
    static {
        for (int i = 0; i < DATA.length; i++) DATA[i] = (byte) (0x10 + i);
    }

    interface Thunk { void run() throws Exception; }
    static void expect(Class<? extends Throwable> t, Thunk r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError("expected " + t.getName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + t.getName() + " but nothing was thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.EncryptedPrivateKeyInfo");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete non-final class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        boolean encodable = false;
        for (Class<?> i : c.getInterfaces())
            if (i.getName().equals("java.security.BinaryEncodable")) encodable = true;
        check(encodable, "implements java.security.BinaryEncodable");
        boolean ctorBytes = false, ctorStringBytes = false, ctorParamsBytes = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            if (ps.length == 1 && ps[0].getName().equals("[B")) ctorBytes = true;
            if (ps.length == 2 && ps[0] == String.class && ps[1].getName().equals("[B")) ctorStringBytes = true;
            if (ps.length == 2 && ps[0].getName().equals("java.security.AlgorithmParameters")
                    && ps[1].getName().equals("[B")) ctorParamsBytes = true;
        }
        check(ctorBytes, "public constructor (byte[])");
        check(ctorStringBytes, "public constructor (String,byte[])");
        check(ctorParamsBytes, "public constructor (AlgorithmParameters,byte[])");
        String[] names = {"getAlgName", "getAlgParameters", "getEncryptedData", "getEncoded",
                "getKeySpec", "getKey", "getKeyPair", "encrypt"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        boolean statEncrypt = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("encrypt") && Modifier.isStatic(m.getModifiers())) statEncrypt = true;
        check(statEncrypt, "static encrypt factories");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        legacyAccessors();
        aesCtorAndRoundTrip();
        constructorErrors();
        pbeKeySpecRoute();
        previewEncryptRoute();
    }

    // legacy flow from the previous shallow test: DES names and AlgorithmParameters
    static void legacyAccessors() throws Exception {
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo("DES", new byte[]{1, 2, 3});
        check(epki.getAlgName() != null, "getAlgName non-null");
        check(epki.getEncryptedData().length == 3, "getEncryptedData length");
        check(epki.getEncoded().length > 0, "getEncoded DER non-empty");
        EncryptedPrivateKeyInfo epki2 = new EncryptedPrivateKeyInfo(AlgorithmParameters.getInstance("DES"), new byte[]{4, 5});
        check(epki2.getEncryptedData().length == 2, "AlgorithmParameters ctor");
        check(epki2.getAlgParameters() != null, "getAlgParameters non-null");
    }

    // new EncryptedPrivateKeyInfo("AES", data): name resolution, defensive copies, DER stability
    static void aesCtorAndRoundTrip() throws Exception {
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo("AES", DATA);
        check("AES".equals(epki.getAlgName()), "getAlgName resolves AES");
        check(epki.getAlgParameters() == null, "no parameters for the name-only constructor");

        byte[] ed = epki.getEncryptedData();
        check(Arrays.equals(ed, DATA), "encrypted data preserved verbatim");
        check(ed != DATA, "constructor copied the input array");
        DATA[0] = (byte) 0x7f;
        check(epki.getEncryptedData()[0] == (byte) 0x10, "later mutation of the input is invisible");
        DATA[0] = (byte) 0x10;
        check(epki.getEncryptedData() != epki.getEncryptedData(), "each getEncryptedData returns a fresh copy");

        byte[] enc1 = epki.getEncoded();
        byte[] enc2 = epki.getEncoded();
        check(enc1.length > 0 && Arrays.equals(enc1, enc2), "getEncoded is stable across calls");

        // DER round trip through the byte[] constructor
        EncryptedPrivateKeyInfo rt = new EncryptedPrivateKeyInfo(enc1);
        check("AES".equals(rt.getAlgName()), "round trip keeps algorithm name");
        check(Arrays.equals(rt.getEncryptedData(), DATA), "round trip keeps encrypted data");
        check(Arrays.equals(rt.getEncoded(), enc1), "round trip reproduces the same DER");
    }

    // documented constructor failure modes
    static void constructorErrors() {
        expect(IllegalArgumentException.class, () -> new EncryptedPrivateKeyInfo("AES", new byte[0]));
        expect(NullPointerException.class, () -> new EncryptedPrivateKeyInfo("AES", null));
        expect(NullPointerException.class, () -> new EncryptedPrivateKeyInfo((String) null, DATA));
        // algorithm names that cannot be resolved to an OID
        expect(java.security.NoSuchAlgorithmException.class,
                () -> new EncryptedPrivateKeyInfo("NoSuchAlgXyz", DATA));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> new EncryptedPrivateKeyInfo("AES/GCM/NoPadding", DATA));
        // ASN.1 constructor failures
        expect(NullPointerException.class, () -> new EncryptedPrivateKeyInfo((byte[]) null));
        expect(java.io.IOException.class, () -> new EncryptedPrivateKeyInfo(new byte[]{1, 2, 3}));
    }

    // classic PKCS12 route: PBEWithSHA1AndDESede + AlgorithmParameters ctor + all four getKeySpec overloads
    static void pbeKeySpecRoute() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        PrivateKey priv = kpg.generateKeyPair().getPrivate();
        byte[] pkcs8 = priv.getEncoded();

        String alg = "PBEWithSHA1AndDESede";
        SecretKeyFactory skf = SecretKeyFactory.getInstance(alg);
        SecretKey pbeKey = skf.generateSecret(new PBEKeySpec(PASSWORD));
        PBEParameterSpec pbeSpec = new PBEParameterSpec(new byte[8], 1024);
        Cipher c = Cipher.getInstance(alg);
        c.init(Cipher.ENCRYPT_MODE, pbeKey, pbeSpec);
        byte[] ct = c.doFinal(pkcs8);
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(c.getParameters(), ct);
        check("PBEWithSHA1AndDESede".equals(epki.getAlgName()), "AlgorithmParameters ctor keeps algorithm name");
        check(epki.getAlgParameters() != null, "AlgorithmParameters ctor keeps parameters");
        check(Arrays.equals(epki.getEncryptedData(), ct), "encrypted data preserved");

        // getKeySpec(Key)
        PKCS8EncodedKeySpec ks1 = epki.getKeySpec(pbeKey);
        check(Arrays.equals(ks1.getEncoded(), pkcs8), "getKeySpec(Key) recovers the PKCS8 encoding");
        // getKeySpec(Cipher) with a cipher initialized for decryption
        Cipher dc = Cipher.getInstance(alg);
        dc.init(Cipher.DECRYPT_MODE, pbeKey, c.getParameters());
        check(Arrays.equals(epki.getKeySpec(dc).getEncoded(), pkcs8), "getKeySpec(Cipher) recovers the PKCS8 encoding");
        // getKeySpec(Key, String) and getKeySpec(Key, Provider)
        Provider sunJce = Security.getProvider("SunJCE");
        check(Arrays.equals(epki.getKeySpec(pbeKey, "SunJCE").getEncoded(), pkcs8),
                "getKeySpec(Key,String) recovers the PKCS8 encoding");
        check(Arrays.equals(epki.getKeySpec(pbeKey, sunJce).getEncoded(), pkcs8),
                "getKeySpec(Key,Provider) recovers the PKCS8 encoding");

        // DER round trip of the PBE object still decrypts
        EncryptedPrivateKeyInfo rt = new EncryptedPrivateKeyInfo(epki.getEncoded());
        check(Arrays.equals(rt.getKeySpec(pbeKey).getEncoded(), pkcs8), "round-tripped PBE object still decrypts");

        // a PBE key derived from the wrong password cannot decrypt
        SecretKey wrongKey = skf.generateSecret(new PBEKeySpec("wrong".toCharArray()));
        expect(java.security.InvalidKeyException.class, () -> epki.getKeySpec(wrongKey));
        expect(java.security.InvalidKeyException.class, () -> epki.getKeySpec(wrongKey, "SunJCE"));
    }

    // JDK-27 preview API: encrypt(...) / getKey(...) / getKeyPair(...)
    static void previewEncryptRoute() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // encrypt(PrivateKey, char[]) picks a default PBE scheme
        EncryptedPrivateKeyInfo e1 = EncryptedPrivateKeyInfo.encrypt(kp.getPrivate(), PASSWORD);
        check("PBEWithHmacSHA256AndAES_128".equals(e1.getAlgName()), "default encrypt uses PBEWithHmacSHA256AndAES_128");
        check(e1.getAlgParameters() != null, "encrypt stores PBE parameters");
        PBEParameterSpec ps = e1.getAlgParameters().getParameterSpec(PBEParameterSpec.class);
        check(ps.getSalt() != null && ps.getIterationCount() > 0, "PBE parameters are readable");

        PrivateKey back = e1.getKey(PASSWORD);
        check("RSA".equals(back.getAlgorithm()), "getKey(char[]) returns the key algorithm");
        check(Arrays.equals(back.getEncoded(), kp.getPrivate().getEncoded()),
                "getKey(char[]) recovers the original PKCS8 encoding");
        expect(java.security.InvalidKeyException.class, () -> e1.getKey("wrong".toCharArray()));

        // DER round trip, then getKey again
        EncryptedPrivateKeyInfo e1rt = new EncryptedPrivateKeyInfo(e1.getEncoded());
        check("PBEWithHmacSHA256AndAES_128".equals(e1rt.getAlgName()), "round trip keeps default PBE name");
        check(Arrays.equals(e1rt.getKey(PASSWORD).getEncoded(), kp.getPrivate().getEncoded()),
                "round-tripped object still decrypts with the password");

        // encrypt(BinaryEncodable, char[], String, AlgorithmParameterSpec, Provider) with explicit PBE
        PBEParameterSpec fixed = new PBEParameterSpec(new byte[8], 1024);
        EncryptedPrivateKeyInfo e2 = EncryptedPrivateKeyInfo.encrypt(kp.getPrivate(), PASSWORD,
                "PBEWithHmacSHA256AndAES_128", fixed, null);
        check("PBEWithHmacSHA256AndAES_128".equals(e2.getAlgName()), "explicit algorithm stored");
        check(Arrays.equals(e2.getKey(PASSWORD).getEncoded(), kp.getPrivate().getEncoded()),
                "explicit-parameter encrypt still decrypts");

        // KeyPair implements BinaryEncodable: encrypt(KeyPair, char[]) + getKeyPair(char[])
        EncryptedPrivateKeyInfo e3 = EncryptedPrivateKeyInfo.encrypt(kp, PASSWORD);
        KeyPair backPair = e3.getKeyPair(PASSWORD);
        check("RSA".equals(backPair.getPrivate().getAlgorithm()) && "RSA".equals(backPair.getPublic().getAlgorithm()),
                "getKeyPair(char[]) returns both RSA keys");
        check(Arrays.equals(backPair.getPublic().getEncoded(), kp.getPublic().getEncoded()),
                "recovered public key encoding is identical");
        RSAPrivateCrtKey o = (RSAPrivateCrtKey) kp.getPrivate();
        RSAPrivateCrtKey r = (RSAPrivateCrtKey) backPair.getPrivate();
        check(o.getModulus().equals(r.getModulus()) && o.getPrivateExponent().equals(r.getPrivateExponent()),
                "recovered private key carries the same key material");
        expect(java.security.InvalidKeyException.class, () -> e3.getKeyPair("wrong".toCharArray()));

        // getKey(Key) with a PBE SecretKey on the explicit-parameter object
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey pbeKey = skf.generateSecret(new PBEKeySpec(PASSWORD));
        EncryptedPrivateKeyInfo e4 = EncryptedPrivateKeyInfo.encrypt(kp.getPrivate(), PASSWORD,
                "PBEWithHmacSHA256AndAES_128", fixed, null);
        check(Arrays.equals(e4.getKey(pbeKey).getEncoded(), kp.getPrivate().getEncoded()),
                "getKey(Key) decrypts with a PBE secret key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
