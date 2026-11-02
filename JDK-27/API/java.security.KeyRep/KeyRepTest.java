/*
 * @test
 * @summary java.security.KeyRep conformance: constructors and Serialization marker.
 * @run main KeyRepTest
 */

import java.security.KeyRep;
import java.io.Serializable;

public class KeyRepTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyRep secret = new KeyRep(KeyRep.Type.SECRET, "AES", "RAW", new byte[16]);
        check(secret instanceof Serializable, "KeyRep implements Serializable");
        KeyRep pub = new KeyRep(KeyRep.Type.PUBLIC, "RSA", "X.509", new byte[] { 1 });
        check(pub instanceof Serializable, "PUBLIC KeyRep Serializable");
        KeyRep priv = new KeyRep(KeyRep.Type.PRIVATE, "RSA", "PKCS#8", new byte[] { 2 });
        check(priv instanceof Serializable, "PRIVATE KeyRep Serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
