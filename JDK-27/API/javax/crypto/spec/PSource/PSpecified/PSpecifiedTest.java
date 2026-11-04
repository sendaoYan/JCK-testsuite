/*
 * @test
 * @summary javax.crypto.spec.PSource.PSpecified conformance: white-box structure + black-box behavior.
 * @run main PSpecifiedTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class PSpecifiedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> outer = Class.forName("javax.crypto.spec.PSource");
        Class<?> c = Class.forName("javax.crypto.spec.PSource$PSpecified");
        check(c.getDeclaringClass() == outer, "declared inside javax.crypto.spec.PSource");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isStatic(c.getModifiers())
                && Modifier.isFinal(c.getModifiers()), "public static final class");
        check(c.getSuperclass() == outer, "extends javax.crypto.spec.PSource");
        check(c.getDeclaredConstructor(byte[].class).getModifiers() == Modifier.PUBLIC,
                "public ctor (byte[])");
        Field def = c.getDeclaredField("DEFAULT");
        check(Modifier.isPublic(def.getModifiers()) && Modifier.isStatic(def.getModifiers())
                && Modifier.isFinal(def.getModifiers()), "DEFAULT is public static final");
        check(def.getType() == c, "DEFAULT field type PSpecified");
        for (String m : new String[] {"getValue"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- original shallow assertions (kept) ---
        check(PSource.PSpecified.DEFAULT != null, "DEFAULT non-null");
        PSource.PSpecified s = new PSource.PSpecified(new byte[]{1, 2});
        check("PSpecified".equals(s.getAlgorithm()), "getAlgorithm");
        check(Arrays.equals(new byte[]{1, 2}, s.getValue()), "getValue");
        check(PSource.PSpecified.DEFAULT.getValue().length == 0, "DEFAULT empty value");

        // --- constructor defensive copy ---
        byte[] val = {0x10, 0x20, 0x30, 0x40};
        PSource.PSpecified p = new PSource.PSpecified(val);
        val[0] = (byte)0x77;
        check(p.getValue()[0] == (byte)0x10, "ctor copies value (external mutation invisible)");
        byte[] g1 = p.getValue();
        byte[] g2 = p.getValue();
        check(g1 != g2, "getValue returns a new array each call (non-empty value)");
        check(Arrays.equals(g1, g2) && g1.length == 4, "repeated getValue contents equal");
        g1[0] = 0x00;
        check(p.getValue()[0] == (byte)0x10, "getValue result copy is detached");

        // --- DEFAULT constant ---
        check("PSpecified".equals(PSource.PSpecified.DEFAULT.getAlgorithm()),
                "DEFAULT algorithm name");
        check(Arrays.equals(PSource.PSpecified.DEFAULT.getValue(), new byte[0]),
                "DEFAULT value is the empty byte string");
        check(new PSource.PSpecified(new byte[0]).getValue().length == 0,
                "explicit empty value allowed");
        // null value rejected
        try {
            new PSource.PSpecified(null);
            check(false, "null p should throw NPE");
        } catch (NullPointerException e) { passed++; }
        // identity semantics: no equals override
        check(!PSource.PSpecified.DEFAULT.equals(new PSource.PSpecified(new byte[0]))
                && PSource.PSpecified.DEFAULT.equals(PSource.PSpecified.DEFAULT),
                "PSpecified does not override equals");
        // usable as OAEPParameterSpec pSrc (type check against the base)
        check(new PSource.PSpecified(val) instanceof PSource
                && new PSource.PSpecified(val) instanceof javax.crypto.spec.PSource,
                "PSpecified is-a PSource");

        // --- real chain: label flows through RSA-OAEP encrypt/decrypt ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        byte[] msg = new byte[16];
        for (int i = 0; i < 16; i++) msg[i] = (byte) (0x30 + i);
        byte[] label = {0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61};
        PSource.PSpecified src = new PSource.PSpecified(label);
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, src);
        Cipher enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        enc.init(Cipher.ENCRYPT_MODE, kp.getPublic(), spec);
        byte[] ct = enc.doFinal(msg);
        check(ct.length == 256, "2048-bit RSA ciphertext length");
        Cipher dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        dec.init(Cipher.DECRYPT_MODE, kp.getPrivate(),
                new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                        new PSource.PSpecified(label)));
        check(Arrays.equals(msg, dec.doFinal(ct)),
                "equal label (different PSpecified instance) decrypts fine");
        Cipher decEmpty = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        decEmpty.init(Cipher.DECRYPT_MODE, kp.getPrivate(), new OAEPParameterSpec("SHA-256",
                "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        try {
            decEmpty.doFinal(ct);
            check(false, "empty label should fail padding check");
        } catch (javax.crypto.BadPaddingException e) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
