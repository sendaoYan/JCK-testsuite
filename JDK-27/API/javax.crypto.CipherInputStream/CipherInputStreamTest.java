/*
 * @test
 * @summary javax.crypto.CipherInputStream conformance: null-cipher input stream passthrough.
 * @run main CipherInputStreamTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;

public class CipherInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NullCipher nc = new NullCipher();
        nc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        CipherOutputStream cos = new CipherOutputStream(bos, nc);
        cos.write("abc".getBytes("UTF-8"));
        cos.close();
        byte[] enc = bos.toByteArray();
        check(enc.length == 3, "null cipher passthrough length");
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(enc), nc);
        check(cis.read() == (int) 'a', "read first byte");
        check(cis.read() == (int) 'b' && cis.read() == (int) 'c' && cis.read() == -1, "read remaining");
        cis.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
