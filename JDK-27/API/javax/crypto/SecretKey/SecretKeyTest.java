/*
 * @test
 * @summary javax.crypto.SecretKey conformance: white-box structure + black-box behavior.
 * @run main SecretKeyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.SecretKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass (interface)");
        // direct superinterfaces: java.security.Key, javax.security.auth.Destroyable
        String[] want = {"java.security.Key", "javax.security.auth.Destroyable"};
        for (String n : want) {
            boolean found = false;
            for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) found = true;
            check(found, "extends " + n);
        }
        check(c.getInterfaces().length == 2, "exactly 2 direct superinterfaces");
        // SecretKey adds no methods of its own: getEncoded/getFormat/getAlgorithm come from Key,
        // destroy/isDestroyed from Destroyable
        check(c.getDeclaredMethods().length == 0, "no own methods (marker-style refinement of Key)");
        // the only declared member is the serialVersionUID constant
        Field[] fields = c.getDeclaredFields();
        check(fields.length == 1 && fields[0].getName().equals("serialVersionUID"),
                "single declared field serialVersionUID");
        check(fields[0].getType() == long.class, "serialVersionUID is long");
        check(Modifier.isPublic(fields[0].getModifiers())
                && Modifier.isStatic(fields[0].getModifiers())
                && Modifier.isFinal(fields[0].getModifiers()), "serialVersionUID is public static final");
        check(fields[0].getLong(null) == -4795878709595146952L, "serialVersionUID value");
        // transitively Serializable through Key (not a direct superinterface)
        check(java.io.Serializable.class.isAssignableFrom(c),
                "extends java.io.Serializable via Key");
        // Key's accessors are inherited, not declared here
        boolean hasEncoded = false;
        for (Method m : c.getMethods()) if (m.getName().equals("getEncoded")) hasEncoded = true;
        check(hasEncoded, "getEncoded inherited from Key is callable on the interface type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] keyBytes = new byte[16];
        for (int i = 0; i < 16; i++) keyBytes[i] = (byte) (0x30 + i);

        // --- SecretKeySpec is a SecretKey ---
        SecretKey specKey = new SecretKeySpec(keyBytes, "AES");      // typed as the interface
        check(specKey instanceof SecretKey, "SecretKeySpec is a SecretKey");
        check(specKey instanceof java.security.Key, "SecretKeySpec is a Key");
        check(specKey instanceof java.security.spec.KeySpec, "SecretKeySpec is a KeySpec");
        check(specKey instanceof Serializable, "SecretKeySpec is Serializable");
        check("AES".equals(specKey.getAlgorithm()), "interface getAlgorithm dispatches");
        check("RAW".equals(specKey.getFormat()), "interface getFormat dispatches");
        check(Arrays.equals(specKey.getEncoded(), keyBytes), "interface getEncoded dispatches");

        // --- keys from KeyGenerator and SecretKeyFactory are SecretKeys too ---
        SecretKey generated = KeyGenerator.getInstance("AES").generateKey();
        check(generated instanceof SecretKey, "KeyGenerator output is a SecretKey");
        check(generated.getEncoded() != null && generated.getEncoded().length > 0,
                "generated SecretKey exposes encoded material");
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 0x55);
        SecretKey derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(new PBEKeySpec("pw".toCharArray(), salt, 1000, 128));
        check(derived instanceof SecretKey, "SecretKeyFactory output is a SecretKey");
        check(derived.getEncoded().length == 16, "derived SecretKey exposes encoded material");

        // --- Destroyable default behavior on a SecretKey implementation that does not override it ---
        check(!specKey.isDestroyed(), "default isDestroyed() is false");
        try {
            specKey.destroy();
            check(false, "default destroy() must throw DestroyFailedException");
        } catch (javax.security.auth.DestroyFailedException e) {
            check(true, "default destroy() -> DestroyFailedException");
        }
        check(!specKey.isDestroyed(), "isDestroyed() still false after failed destroy");

        // --- a SecretKey-typed reference drives a Cipher roundtrip ---
        byte[] iv = new byte[12];
        Arrays.fill(iv, (byte) 0x11);
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, specKey, new GCMParameterSpec(128, iv));
        byte[] plain = "secret key interface test".getBytes(StandardCharsets.UTF_8);
        byte[] ct = enc.doFinal(plain);
        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, (SecretKey) new SecretKeySpec(keyBytes, "AES"),
                new GCMParameterSpec(128, iv));
        check(Arrays.equals(dec.doFinal(ct), plain), "SecretKey-typed key completes AES/GCM roundtrip");
        check(ct.length == plain.length + 16, "GCM appends a 128-bit tag");

        // --- serialization through the interface reference (Serializable via Key) ---
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(specKey);
        }
        try (ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            SecretKey copy = (SecretKey) ois.readObject();          // deserialized as the interface
            check(copy instanceof SecretKey, "deserialized object is a SecretKey");
            check(Arrays.equals(copy.getEncoded(), keyBytes), "serialized SecretKey keeps key material");
            check("AES".equals(copy.getAlgorithm()), "serialized SecretKey keeps algorithm");
            check("RAW".equals(copy.getFormat()), "serialized SecretKey keeps format");
            check(specKey.equals(copy), "deserialized SecretKey equals original");
        }

        // --- equality across independent SecretKey producers of the same material ---
        SecretKey same1 = new SecretKeySpec(keyBytes, "AES");
        SecretKey same2 = new SecretKeySpec(keyBytes.clone(), "AES");
        check(same1.equals(same2) && same2.equals(same1),
                "two SecretKeys with equal material and algorithm are equal");
        check(same1.hashCode() == same2.hashCode(), "equal SecretKeys share a hashCode");
        SecretKey different = new SecretKeySpec(keyBytes, "HmacSHA256");
        check(!same1.equals(different), "same material under another algorithm is a different key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
