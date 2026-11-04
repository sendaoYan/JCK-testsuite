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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

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

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    static String trigger(byte[] bytes) throws Exception {
        try {
            new DataInputStream(new ByteArrayInputStream(bytes)).readUTF();
            return null;
        } catch (UTFDataFormatException e) {
            return e.getMessage();
        }
    }

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        UTFDataFormatException e = new UTFDataFormatException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.UTFDataFormatException: m"),
              "toString = FQCN + \": \" + message");
        check(new UTFDataFormatException().toString().equals("java.io.UTFDataFormatException"),
              "no-arg toString carries no colon");
        check(new UTFDataFormatException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new UTFDataFormatException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("D", "readUTF", "D.java", 4)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "readUTF".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new UTFDataFormatException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new UTFDataFormatException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new UTFDataFormatException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        UTFDataFormatException sup = new UTFDataFormatException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in order");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(UTFDataFormatException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(UTFDataFormatException.class).getSerialVersionUID()
                  == 420743449228280612L, "serialVersionUID == 420743449228280612");
        UTFDataFormatException r = (UTFDataFormatException) back(serOf(new UTFDataFormatException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.UTFDataFormatException: kept"),
              "toString survives round-trip");
        check(((UTFDataFormatException) back(serOf(new UTFDataFormatException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        UTFDataFormatException chained = new UTFDataFormatException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((UTFDataFormatException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
    }

    // ---- byte-precise diagnostics (实测双侧一致) ----
    static void testDiagnostics() throws Exception {
        // a message names the FIRST bad byte position, counting from the start of the UTF body
        check("malformed input around byte 0".equals(trigger(utfBytes(1, 0xFF))),
              "leading 0xFF at index 0: " + trigger(utfBytes(1, 0xFF)));
        check("malformed input around byte 1".equals(trigger(utfBytes(2, 'a', 0x80))),
              "stray continuation 0x80 at index 1: " + trigger(utfBytes(2, 'a', 0x80)));
        check("malformed input around byte 2".equals(trigger(utfBytes(2, 0xC3, 0x41))),
              "2-byte sequence with ASCII tail: " + trigger(utfBytes(2, 0xC3, 0x41)));
        check("malformed input around byte 2".equals(trigger(utfBytes(3, 0xE2, 0x28, 0xAC))),
              "3-byte sequence broken in the middle: " + trigger(utfBytes(3, 0xE2, 0x28, 0xAC)));
        check("malformed input around byte 0".equals(trigger(utfBytes(4, 0xF0, 0x90, 0x80, 0x80))),
              "4-byte sequences do not exist in modified UTF-8");
        check("malformed input: partial character at end".equals(trigger(utfBytes(6, 'a','b','c','d','e', 0xC3))),
              "truncated 2-byte at the end");
        check("malformed input: partial character at end".equals(trigger(utfBytes(2, 0xE2, 0x82))),
              "truncated 3-byte at the end");
    }

    // ---- modified-UTF leniency: overlong forms and CESU-8 surrogates decode (实测) ----
    static void testLenientForms() throws Exception {
        // C0 80: the overlong NUL form is REQUIRED by the format and decodes to \u0000
        String s = new DataInputStream(new ByteArrayInputStream(utfBytes(2, 0xC0, 0x80))).readUTF();
        check(s.length() == 1 && s.charAt(0) == '\u0000',
              "overlong C0 80 decodes to a single NUL");
        // E0 80 80: other overlong 3-byte forms are accepted too (unlike strict UTF-8)
        s = new DataInputStream(new ByteArrayInputStream(utfBytes(3, 0xE0, 0x80, 0x80))).readUTF();
        check(s.length() == 1 && s.charAt(0) == '\u0000',
              "overlong E0 80 80 decodes to NUL as well");
        // a lone surrogate travels as a plain 3-byte sequence (CESU-8 style)
        s = new DataInputStream(new ByteArrayInputStream(utfBytes(3, 0xED, 0xA0, 0x80))).readUTF();
        check(s.length() == 1 && Character.isSurrogate(s.charAt(0)),
              "lone high surrogate decodes to one char");
        // a surrogate pair decodes as TWO chars (surrogates are NOT combined)
        s = new DataInputStream(new ByteArrayInputStream(
            utfBytes(6, 0xED, 0xA0, 0x80, 0xED, 0xB0, 0x80))).readUTF();
        check(s.length() == 2 && Character.isHighSurrogate(s.charAt(0))
                  && Character.isLowSurrogate(s.charAt(1)),
              "CESU-8 surrogate pair decodes to two surrogate chars");
    }

    // ---- writeUTF boundary arithmetic (实测双侧一致) ----
    static void testWriteBoundary() throws Exception {
        // exactly 65535 encoded bytes: 32767 two-byte chars + 1 ascii = 65535, accepted
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 32767; i++) b.append('é');
        b.append('x');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bos)) {
            out.writeUTF(b.toString());
        }
        check(bos.size() == 65537, "2-byte header + 65535 body fits: " + bos.size());
        // one more two-byte char (65536 body) is refused
        StringBuilder b2 = new StringBuilder();
        for (int i = 0; i < 32768; i++) b2.append('é');
        try {
            new DataOutputStream(new ByteArrayOutputStream()).writeUTF(b2.toString());
            check(false, "65536-byte body must be refused");
        } catch (UTFDataFormatException udf) {
            check(udf.getMessage().endsWith("too long: 65536 bytes"),
                  "refusal names the encoded size: " + udf.getMessage());
        }
        // NUL inflates the encoding: 65535 NULs need 131070 bytes as C0 80
        StringBuilder b3 = new StringBuilder();
        for (int i = 0; i < 65535; i++) b3.append('\u0000');
        try {
            new DataOutputStream(new ByteArrayOutputStream()).writeUTF(b3.toString());
            check(false, "NUL-inflated body must be refused");
        } catch (UTFDataFormatException udf) {
            check(udf.getMessage().contains("too long"),
                  "NUL case reports an oversize: " + udf.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testDiagnostics();
        testLenientForms();
        testWriteBoundary();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
