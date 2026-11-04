/*
 * @test
 * @summary java.io.UTFDataFormatException conformance (pure black-box):
 *          constructors; real triggers from DataInputStream.readUTF over
 *          hand-crafted modified-UTF-8 byte sequences: illegal leading byte,
 *          broken continuation, encoded length exceeding the 65535 limit.
 * @run main UTFDataFormatExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

public class UTFDataFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] utfBytes(int len, int... body) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write((len >>> 8) & 0xFF);
        b.write(len & 0xFF);
        for (int x : body) b.write(x);
        return b.toByteArray();
    }

    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new UTFDataFormatException().getMessage() == null, "no-arg message null");
        check("bad utf".equals(new UTFDataFormatException("bad utf").getMessage()),
              "(String) message kept");

        // 2. trigger: illegal leading byte 0xFF (declared length 1)
        try {
            new DataInputStream(new ByteArrayInputStream(utfBytes(1, 0xFF))).readUTF();
            check(false, "malformed leading byte must throw");
        } catch (UTFDataFormatException udf) {
            check(udf.getMessage() != null && udf.getMessage().contains("malformed"),
                  "malformed message: " + udf.getMessage());
        }

        // 3. trigger: 2-byte sequence with a non-continuation second byte
        try {
            new DataInputStream(new ByteArrayInputStream(utfBytes(2, 0xC3, 0x41))).readUTF();
            check(false, "broken continuation must throw");
        } catch (UTFDataFormatException expected) {
            passed++;
        }

        // 4. trigger: 3-byte sequence truncated by the encoded length
        try {
            new DataInputStream(new ByteArrayInputStream(utfBytes(2, 0xE2, 0x82))).readUTF();
            check(false, "truncated 3-byte sequence must throw");
        } catch (UTFDataFormatException expected) {
            passed++;
        }

        // 5. trigger: a byte stream shorter than the declared encoded length (EOF, not UTFDF)
        try {
            new DataInputStream(new ByteArrayInputStream(utfBytes(5, 'a', 'b'))).readUTF();
            check(false, "short body must throw");
        } catch (IOException ex) {
            check(ex instanceof java.io.EOFException,
                  "short body reports EOFException: " + ex.getClass().getSimpleName());
        }

        // 6. trigger: writeUTF refuses a string needing more than 65535 encoded bytes
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 65536 + 8; i++) sb.append('é');   // 2 bytes each in modified UTF-8
        try {
            new DataOutputStream(new ByteArrayOutputStream()).writeUTF(sb.toString());
            check(false, "oversized writeUTF must throw");
        } catch (UTFDataFormatException expected) {
            check(expected.getMessage() != null, "oversized message: " + expected.getMessage());
        }

        // 7. contrast: well-formed ASCII, 2-byte and 3-byte modified UTF-8 round-trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bos)) {
            out.writeUTF("plain");
            out.writeUTF("éÿ");                 // two-byte encodings
            out.writeUTF("中文");                 // three-byte encodings
            out.writeUTF("\u0000");                       // overlong 0xC0 0x80 form
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("plain".equals(in.readUTF()), "ascii round-trip");
            check("éÿ".equals(in.readUTF()), "two-byte round-trip");
            check("中文".equals(in.readUTF()), "three-byte round-trip");
            check("\u0000".equals(in.readUTF()), "modified-UTF NUL round-trip");
        }

        // 8. caught as IOException
        try {
            throw new UTFDataFormatException("m");
        } catch (IOException io) {
            check(io instanceof UTFDataFormatException, "catchable as IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
