/*
 * @test
 * @summary javax.crypto.CipherOutputStream conformance: null-cipher output stream passthrough.
 * @run main CipherOutputStreamTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.Arrays;

public class CipherOutputStreamTest {
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
        cos.write('x');
        cos.write("yz".getBytes("UTF-8"));
        cos.flush();
        cos.close();
        check(Arrays.equals(new byte[]{(byte) 'x', (byte) 'y', (byte) 'z'}, bos.toByteArray()), "null cipher passthrough");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
