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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
