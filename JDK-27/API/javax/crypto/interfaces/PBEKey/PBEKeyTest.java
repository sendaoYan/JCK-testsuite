/*
 * @test
 * @summary javax.crypto.interfaces.PBEKey conformance: white-box structure + black-box behavior.
 * @run main PBEKeyTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.DestroyFailedException;

public class PBEKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }

    static final char[] PASSWORD = "topsecret12".toCharArray();
    static final byte[] SALT = new byte[16];
    static {
        for (int i = 0; i < 16; i++) SALT[i] = (byte) (0xA0 + i);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.interfaces.PBEKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"javax.crypto.SecretKey"});
        checkMethods(c, new String[]{"getPassword","getSalt","getIterationCount"});
        // exact signatures
        check(c.getDeclaredMethod("getPassword").getReturnType() == char[].class, "getPassword returns char[]");
        check(c.getDeclaredMethod("getSalt").getReturnType() == byte[].class, "getSalt returns byte[]");
        check(c.getDeclaredMethod("getIterationCount").getReturnType() == int.class, "getIterationCount returns int");
        // PBEKey declares exactly its three own methods
        check(c.getDeclaredMethods().length == 3, "exactly three declared methods");
        // serialVersionUID: public static final long, value fixed since J2SE 1.4
        // (field is @Deprecated as of JDK 24+ but still present)
        Field svuid = c.getDeclaredField("serialVersionUID");
        check(Modifier.isPublic(svuid.getModifiers()) && Modifier.isStatic(svuid.getModifiers())
                && Modifier.isFinal(svuid.getModifiers()) && svuid.getType() == long.class,
                "serialVersionUID public static final long");
        check(svuid.getLong(null) == -1430015993304333921L, "serialVersionUID value");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey raw = skf.generateSecret(new PBEKeySpec(PASSWORD, SALT, 10000, 256));
        check(raw instanceof PBEKey, "factory key instanceof PBEKey");
        check(raw instanceof SecretKey, "factory key instanceof SecretKey");
        PBEKey key = (PBEKey) raw;

        // PBE parameter accessors mirror the PBEKeySpec inputs
        check(key.getIterationCount() == 10000, "getIterationCount == 10000");
        byte[] salt1 = key.getSalt();
        check(salt1 != null && salt1.length == 16 && Arrays.equals(salt1, SALT),
                "getSalt equals the spec salt");
        char[] pw1 = key.getPassword();
        check(pw1 != null && Arrays.equals(pw1, PASSWORD), "getPassword equals the spec password");
        // accessors hand out defensive copies
        salt1[0] = (byte) 0xFF;
        pw1[0] = 'X';
        check(Arrays.equals(key.getSalt(), SALT), "getSalt returns an isolated copy");
        check(Arrays.equals(key.getPassword(), PASSWORD), "getPassword returns an isolated copy");

        // Key-level metadata
        check("RAW".equals(key.getFormat()), "getFormat == RAW");
        check("PBKDF2WithHmacSHA256".equals(key.getAlgorithm()), "getAlgorithm == PBKDF2WithHmacSHA256");
        byte[] enc1 = key.getEncoded();
        check(enc1 != null && enc1.length == 32, "getEncoded is the 32-byte derived key");
        check(Arrays.equals(key.getEncoded(), enc1), "getEncoded stable across calls");

        // determinism: same spec inputs always derive the same key and are equals()
        SecretKey raw2 = skf.generateSecret(new PBEKeySpec(PASSWORD, SALT, 10000, 256));
        check(Arrays.equals(raw2.getEncoded(), enc1), "same inputs derive same key bytes");
        check(raw.equals(raw2) && raw2.equals(raw), "keys with same inputs are equals()");
        check(raw.hashCode() == raw2.hashCode(), "equal keys share hashCode");
        // different inputs derive different keys
        PBEKey otherIter = (PBEKey) skf.generateSecret(new PBEKeySpec(PASSWORD, SALT, 10001, 256));
        PBEKey otherPw = (PBEKey) skf.generateSecret(new PBEKeySpec("other".toCharArray(), SALT, 10000, 256));
        check(!raw.equals(otherIter) && !Arrays.equals(otherIter.getEncoded(), enc1),
                "different iteration count derives a different key");
        check(!raw.equals(otherPw) && !Arrays.equals(otherPw.getEncoded(), enc1),
                "different password derives a different key");

        // reverse conversion: key -> PBEKeySpec exposes the original PBE parameters
        KeySpec revSpec = skf.getKeySpec(raw, PBEKeySpec.class);
        check(revSpec instanceof PBEKeySpec, "getKeySpec returns PBEKeySpec");
        PBEKeySpec rev = (PBEKeySpec) revSpec;
        check(rev.getIterationCount() == 10000 && Arrays.equals(rev.getSalt(), SALT)
                && Arrays.equals(rev.getPassword(), PASSWORD),
                "reverse spec carries password/salt/iteration count");

        // Destroyable contract inherited from SecretKey: undestroyed by default,
        // destroy() is not supported by the SunJCE PBKDF2 key
        check(!key.isDestroyed(), "isDestroyed == false before destroy");
        try {
            key.destroy();
            check(false, "destroy() should throw DestroyFailedException");
        } catch (DestroyFailedException expected) {
            check(true, "destroy() throws DestroyFailedException");
        }
        check(!key.isDestroyed(), "isDestroyed still false after failed destroy");

        // clearPassword decouples the key from its spec
        PBEKeySpec cleared = new PBEKeySpec(PASSWORD, SALT, 10000, 256);
        SecretKey beforeClear = skf.generateSecret(cleared);
        check(Arrays.equals(beforeClear.getEncoded(), enc1), "key derived before clear matches");
        cleared.clearPassword();
        check(key.getIterationCount() == 10000 && Arrays.equals(key.getPassword(), PASSWORD)
                && Arrays.equals(key.getEncoded(), enc1),
                "existing key unaffected by spec.clearPassword()");
        try {
            skf.generateSecret(cleared);
            check(false, "factory should reject cleared spec");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(e.getCause() instanceof IllegalStateException,
                    "factory on cleared spec throws InvalidKeySpecException(IllegalStateException)");
        }

        // the PBEKey is usable wherever a SecretKey is accepted (Mac family glue)
        Mac mac1 = Mac.getInstance("HmacSHA256");
        mac1.init(raw);
        byte[] macA = mac1.doFinal("jck-data".getBytes("UTF-8"));
        Mac mac2 = Mac.getInstance("HmacSHA256");
        mac2.init(raw2);
        check(macA.length == 32 && Arrays.equals(mac2.doFinal("jck-data".getBytes("UTF-8")), macA),
                "Mac over equal PBEKeys is deterministic");

        // boundary: PBEKeySpec rejects a zero iteration count
        try {
            new PBEKeySpec(PASSWORD, SALT, 0, 256);
            check(false, "PBEKeySpec should reject iteration count 0");
        } catch (IllegalArgumentException e) {
            check(true, "PBEKeySpec iteration count 0 throws IllegalArgumentException");
        }

        // contrast (SunJCE, observed): the PBEWith...AES cipher variant returns a
        // provider-internal PBE key that does not implement this interface
        SecretKeyFactory aesF = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey aesKey = aesF.generateSecret(new PBEKeySpec(PASSWORD, SALT, 10000));
        check(!(aesKey instanceof PBEKey),
                "PBEWithHmacSHA256AndAES_128 key is not a javax.crypto.interfaces.PBEKey");

        // serialization keeps the Key-level contract (encoded via KeyRep)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(raw); }
        SecretKey keyD;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            keyD = (SecretKey) ois.readObject();
        }
        check(keyD != null && Arrays.equals(keyD.getEncoded(), enc1)
                && "PBKDF2WithHmacSHA256".equals(keyD.getAlgorithm()),
                "deserialized key keeps encoded value and algorithm");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
