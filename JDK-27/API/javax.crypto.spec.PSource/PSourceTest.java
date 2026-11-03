/*
 * @test
 * @summary javax.crypto.spec.PSource conformance: white-box structure + black-box behavior.
 * @run main PSourceTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class PSourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.PSource");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isAbstract(c.getModifiers()), "class is not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "class is not final (subclassable)");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getConstructors().length == 0, "no public constructor");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && ctors[0].getModifiers() == Modifier.PROTECTED,
                "single protected constructor");
        check(ctors[0].getParameterCount() == 1
                && ctors[0].getParameterTypes()[0] == String.class,
                "protected ctor takes (String)");
        boolean found = false;
        for (Method dm : c.getDeclaredMethods()) {
            if (dm.getName().equals("getAlgorithm") && Modifier.isPublic(dm.getModifiers())
                    && dm.getReturnType() == String.class && dm.getParameterCount() == 0) {
                found = true; break;
            }
        }
        check(found, "public String getAlgorithm() exists");
        // nested type structure
        Class<?> psp = Class.forName("javax.crypto.spec.PSource$PSpecified");
        check(psp.getDeclaringClass() == c, "PSpecified is declared inside PSource");
        check(Modifier.isPublic(psp.getModifiers()) && Modifier.isStatic(psp.getModifiers())
                && Modifier.isFinal(psp.getModifiers()), "PSpecified is public static final");
        check(psp.getSuperclass() == c, "PSpecified extends PSource");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- original shallow assertions (kept) ---
        PSource p = new PSource("MyAlg") { };
        check("MyAlg".equals(p.getAlgorithm()), "getAlgorithm");
        check(Class.forName("javax.crypto.spec.PSource").getConstructors().length == 0,
                "no public constructor");
        check(Class.forName("javax.crypto.spec.PSource").getDeclaredConstructors()[0]
                .getModifiers() == Modifier.PROTECTED, "protected constructor");

        // --- subclass semantics ---
        PSource mine = new PSource("CustomPSource") { };
        check("CustomPSource".equals(mine.getAlgorithm()), "anonymous subclass algorithm name");
        check("CustomPSource".equals(mine.getAlgorithm()),
                "getAlgorithm is stable across repeated calls");
        PSource other = new PSource("AnotherAlg") { };
        check(!mine.getAlgorithm().equals(other.getAlgorithm()),
                "distinct subclasses report distinct algorithm names");
        check(!mine.equals(other) && mine.equals(mine),
                "PSource does not override equals (identity semantics)");
        // PSpecified reports its own fixed name through the inherited method
        check("PSpecified".equals(PSource.PSpecified.DEFAULT.getAlgorithm()),
                "PSpecified.DEFAULT algorithm via inherited getAlgorithm");
        // protected ctor rejects null name (observable through subclassing)
        try {
            new PSource(null) { };
            check(false, "null pSrcName should throw NPE");
        } catch (NullPointerException e) { passed++; }

        // --- real chain: PSource feeds OAEPParameterSpec into RSA OAEP cipher ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        byte[] msg = "0123456789abcdef0123456789abcdef".getBytes(
                java.nio.charset.StandardCharsets.US_ASCII);
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        Cipher enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        enc.init(Cipher.ENCRYPT_MODE, kp.getPublic(), spec);
        byte[] ct = enc.doFinal(msg);
        Cipher dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        dec.init(Cipher.DECRYPT_MODE, kp.getPrivate(), spec);
        check(Arrays.equals(msg, dec.doFinal(ct)),
                "RSA OAEP round trip driven by a PSource-backed OAEPParameterSpec");
        // spec built from PSpecified with label also round-trips
        PSource labeled = new PSource.PSpecified(new byte[]{0x0a, 0x0b});
        OAEPParameterSpec labeledSpec = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, labeled);
        Cipher enc2 = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        enc2.init(Cipher.ENCRYPT_MODE, kp.getPublic(), labeledSpec);
        Cipher dec2 = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        dec2.init(Cipher.DECRYPT_MODE, kp.getPrivate(), labeledSpec);
        check(Arrays.equals(msg, dec2.doFinal(enc2.doFinal(msg))),
                "labelled PSpecified round trip through RSA OAEP");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
