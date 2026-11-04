/*
 * @test
 * @summary java.security.Key conformance: full contract coverage.
 *          interface extends Serializable, getAlgorithm/getFormat/getEncoded for
 *          RSA public/private keys, serialization round-trip of a public key,
 *          and Key.serialVersionUID presence.
 * @run main KeyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // ---- interface hierarchy ----
        Class<?> c = Class.forName("java.security.Key");
        check(c.isInterface(), "Key is interface");
        check(Serializable.class.isAssignableFrom(c), "Key extends Serializable");
        check(Key.serialVersionUID != 0L, "Key.serialVersionUID defined");

        // ---- generated RSA key metadata ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        Key pub = kp.getPublic();
        Key priv = kp.getPrivate();
        check("RSA".equals(pub.getAlgorithm()), "public getAlgorithm RSA");
        check("X.509".equals(pub.getFormat()), "public getFormat X.509");
        check("RSA".equals(priv.getAlgorithm()), "private getAlgorithm RSA");
        check("PKCS#8".equals(priv.getFormat()), "private getFormat PKCS#8");
        check(pub.getEncoded() != null && pub.getEncoded().length > 64, "public getEncoded non-trivial");
        check(priv.getEncoded() != null && priv.getEncoded().length > 64, "private getEncoded non-trivial");

        // ---- serialization round-trip of public key ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(pub);
        }
        Key back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (Key) ois.readObject();
        }
        check("RSA".equals(back.getAlgorithm()), "deserialized getAlgorithm");
        check("X.509".equals(back.getFormat()), "deserialized getFormat");
        check(java.util.Arrays.equals(pub.getEncoded(), back.getEncoded()), "deserialized getEncoded equal");
        check(pub.equals(back), "deserialized equals original");

        // ==== contract deepening: private-key serialization, defensive copies, cross-key equals ====

        // ---- serialization round-trip of private key (PKCS#8 KeyRep path) ----
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(priv);
        }
        Key privBack;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))) {
            privBack = (Key) ois.readObject();
        }
        check("RSA".equals(privBack.getAlgorithm()), "private deserialized algorithm");
        check("PKCS#8".equals(privBack.getFormat()), "private deserialized format");
        check(java.util.Arrays.equals(priv.getEncoded(), privBack.getEncoded()),
                "private deserialized encoding equal");
        check(priv.equals(privBack), "private deserialized equals original");

        // ---- DSA keys serialize, value-equal, and cross-algorithm keys never equal ----
        java.security.KeyPairGenerator dsaGen = java.security.KeyPairGenerator.getInstance("DSA");
        dsaGen.initialize(1024);
        java.security.KeyPair dkp = dsaGen.generateKeyPair();
        ByteArrayOutputStream dbos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(dbos)) {
            oos.writeObject(dkp.getPublic());
        }
        java.security.Key dpubBack;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dbos.toByteArray()))) {
            dpubBack = (java.security.Key) ois.readObject();
        }
        check(dkp.getPublic().equals(dpubBack), "DSA public serialization equals");
        check(!pub.equals(dkp.getPublic()) && !dkp.getPublic().equals(pub),
                "cross-algorithm keys not equal (symmetric)");

        // ---- getEncoded returns a fresh copy each call (defensive copy) ----
        check(pub.getEncoded() != pub.getEncoded(), "public getEncoded defensive copy");
        check(java.util.Arrays.equals(pub.getEncoded(), pub.getEncoded()),
                "public getEncoded copies value-equal");
        check(priv.getEncoded() != priv.getEncoded(), "private getEncoded defensive copy");

        // ---- hashCode consistent with equals across serialization ----
        check(pub.hashCode() == back.hashCode(), "public hashCode stable across serialization");
        check(priv.hashCode() == privBack.hashCode(), "private hashCode stable across serialization");

        // ---- a rebuilt key from encoded bytes equals the original (KeyFactory path) ----
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        check(pub.equals(kf.generatePublic(new java.security.spec.X509EncodedKeySpec(pub.getEncoded()))),
                "rebuilt public equals original");
        check(priv.equals(kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(priv.getEncoded()))),
                "rebuilt private equals original");

        // ---- interface hierarchy: PublicKey/PrivateKey both extend Key ----
        check(java.security.Key.class.isAssignableFrom(java.security.PublicKey.class)
                && java.security.Key.class.isAssignableFrom(java.security.PrivateKey.class),
                "PublicKey and PrivateKey extend Key");
        check(pub instanceof java.security.PublicKey && priv instanceof java.security.PrivateKey,
                "generated keys implement marker interfaces");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
