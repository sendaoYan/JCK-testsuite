/*
 * @test
 * @summary java.security.KeyPair conformance: full contract coverage.
 *          constructor + getPublic/getPrivate, null-key constructor allowed,
 *          generated RSA keypair non-null with X.509/PKCS#8 formats and
 *          non-trivial encodings.
 * @run main KeyPairTest
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // ---- generated keypair ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair gen = kpg.generateKeyPair();
        check(gen.getPublic() != null && gen.getPrivate() != null, "generated keys non-null");
        check("RSA".equals(gen.getPublic().getAlgorithm()), "public getAlgorithm");
        check("RSA".equals(gen.getPrivate().getAlgorithm()), "private getAlgorithm");
        check("X.509".equals(gen.getPublic().getFormat()), "public format X.509");
        check("PKCS#8".equals(gen.getPrivate().getFormat()), "private format PKCS#8");
        check(gen.getPublic().getEncoded() != null && gen.getPublic().getEncoded().length > 64,
                "public encoded non-trivial");
        check(gen.getPrivate().getEncoded() != null && gen.getPrivate().getEncoded().length > 64,
                "private encoded non-trivial");

        // ---- constructor round-trip ----
        KeyPair kp = new KeyPair(gen.getPublic(), gen.getPrivate());
        check(gen.getPublic().equals(kp.getPublic()), "constructor getPublic equals");
        check(gen.getPrivate().equals(kp.getPrivate()), "constructor getPrivate equals");

        // ---- null-key constructor allowed ----
        KeyPair empty = new KeyPair(null, null);
        check(empty.getPublic() == null && empty.getPrivate() == null, "null-key constructor stores nulls");

        // ---- mixed ----
        PublicKey pub = gen.getPublic();
        PrivateKey priv = gen.getPrivate();
        KeyPair mixed = new KeyPair(pub, null);
        check(mixed.getPublic() == pub && mixed.getPrivate() == null, "public-only keypair");
        KeyPair mixed2 = new KeyPair(null, priv);
        check(mixed2.getPublic() == null && mixed2.getPrivate() == priv, "private-only keypair");

        // ==== contract deepening: equals/hashCode/toString, serialization, signature use ====

        // ---- equals: KeyPair uses identity semantics (no equals/hashCode override) ----
        KeyPair same = new KeyPair(gen.getPublic(), gen.getPrivate());
        check(!same.equals(gen), "identity semantics: same keys, different KeyPair (recorded)");
        check(same.equals(same), "self-equals always true");
        check(gen.equals(gen), "self-equals original");
        check(!gen.equals(null), "not equal to null");
        check(!gen.equals("not a keypair"), "not equal to other type");
        KeyPair otherKeys = kpg.generateKeyPair();
        check(!gen.equals(otherKeys), "different instances differ");

        // ---- toString contains key class info ----
        String ts = gen.toString();
        check(ts != null && ts.contains("KeyPair"), "toString contains class name");

        // ---- serialization round-trip preserves both keys ----
        java.io.ByteArrayOutputStream kbos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(kbos)) {
            oos.writeObject(same);
        }
        KeyPair serBack;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(kbos.toByteArray()))) {
            serBack = (KeyPair) ois.readObject();
        }
        check(gen.getPublic().equals(serBack.getPublic()), "serialized public equals");
        check(gen.getPrivate().equals(serBack.getPrivate()), "serialized private equals");
        check(!serBack.equals(gen), "serialized KeyPair itself stays identity-equal (recorded)");

        // ---- keys from the pair drive a real signature loop ----
        java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
        signer.initSign(gen.getPrivate());
        signer.update("keypair-loop".getBytes());
        byte[] sig = signer.sign();
        java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
        verifier.initVerify(gen.getPublic());
        verifier.update("keypair-loop".getBytes());
        check(verifier.verify(sig), "keypair keys drive signature loop");

        // ---- null-containing pairs serialize too (recorded: nulls preserved) ----
        java.io.ByteArrayOutputStream nbos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(nbos)) {
            oos.writeObject(mixed);
        }
        KeyPair mixedBack;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(nbos.toByteArray()))) {
            mixedBack = (KeyPair) ois.readObject();
        }
        check(mixedBack.getPrivate() == null, "serialized null private stays null");
        check(mixed.getPublic().equals(mixedBack.getPublic()), "serialized mixed public equals");


        System.out.println("PASS (" + passed + " assertions)");
    }
}
