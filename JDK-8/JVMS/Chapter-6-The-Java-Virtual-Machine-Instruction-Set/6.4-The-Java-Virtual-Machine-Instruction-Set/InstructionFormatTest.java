/*
 * @test
 * @summary JVMS 6.4 (Java SE 8): each instruction is a one-byte opcode followed by
 *          zero or more operand bytes.  This test hand-parses the class file of a
 *          jasm-generated class and asserts the exact code arrays of two methods:
 *          return encodes as 0xB1 and iconst_0; ireturn as 0x03 0xAC.
 * @compile Simple.jasm
 * @run main InstructionFormatTest
 */
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InstructionFormatTest {
    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = InstructionFormatTest.class.getResourceAsStream("Simple.class")) {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) o.write(buf, 0, n);
            b = o.toByteArray();
        }

        ClassFile cf = new ClassFile(b);
        byte[] ret = cf.codeOf("ret");
        byte[] loadRet = cf.codeOf("loadRet");

        if (!Arrays.equals(ret, new byte[] { (byte) 0xB1 }))
            throw new AssertionError("ret: expected [B1], got " + hex(ret));
        if (!Arrays.equals(loadRet, new byte[] { 0x03, (byte) 0xAC }))
            throw new AssertionError("loadRet: expected [03 AC], got " + hex(loadRet));

        System.out.println("PASS");
    }

    static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) s.append(String.format("%02X ", x));
        return s.toString();
    }

    // Minimal class file parser: extracts the Code attribute bytes of each method.
    static class ClassFile {
        final byte[] b;
        int pos;
        final Map<String, byte[]> codes = new HashMap<String, byte[]>();

        ClassFile(byte[] b) {
            this.b = b;
            pos = 0;
            u4();            // magic
            u2();            // minor_version
            u2();            // major_version
            String[] cp = constantPool();
            pos += 6;        // access_flags, this_class, super_class
            int ifCount = u2();
            pos += ifCount * 2;                    // interfaces
            skipMembers(cp);                      // fields
            parseMethods(cp);                     // methods
        }

        String[] constantPool() {
            int count = u2();
            String[] utf = new String[count];
            for (int i = 1; i < count; i++) {
                int tag = u1();
                switch (tag) {
                    case 1: { // CONSTANT_Utf8
                        int len = u2();
                        utf[i] = new String(b, pos, len, StandardCharsets.UTF_8);
                        pos += len;
                        break;
                    }
                    case 3: case 4:  pos += 4; break; // Integer, Float
                    case 5: case 6:  pos += 8; i++; break; // Long, Double (2 slots)
                    case 7: case 8: case 16: pos += 2; break; // Class, String, MethodType
                    case 9: case 10: case 11: case 12: case 18: pos += 4; break; // refs
                    case 15: pos += 3; break; // MethodHandle
                    default: throw new AssertionError("unknown constant-pool tag " + tag);
                }
            }
            return utf;
        }

        void skipMembers(String[] cp) {
            int count = u2();
            for (int i = 0; i < count; i++) {
                pos += 6;                    // access_flags, name_index, descriptor_index
                int attrs = u2();
                for (int j = 0; j < attrs; j++) skipAttr();
            }
        }

        void parseMethods(String[] cp) {
            int count = u2();
            for (int i = 0; i < count; i++) {
                pos += 2;                    // access_flags
                String name = cp[u2()];
                pos += 2;                    // descriptor_index
                int attrs = u2();
                for (int j = 0; j < attrs; j++) {
                    int nameIdx = u2();
                    int len = u4();
                    if ("Code".equals(cp[nameIdx])) {
                        pos += 4;            // max_stack, max_locals
                        int codeLen = u4();
                        byte[] code = Arrays.copyOfRange(b, pos, pos + codeLen);
                        pos += codeLen;
                        codes.put(name, code);
                        skipExceptionTable();
                        skipCodeAttrs();
                    } else {
                        pos += len;
                    }
                }
            }
        }

        void skipExceptionTable() {
            int n = u2();
            pos += n * 8;   // each entry: start_pc, end_pc, handler_pc, catch_type
        }

        void skipCodeAttrs() {
            int n = u2();
            for (int i = 0; i < n; i++) {
                u2();                    // attribute_name_index
                int len = u4();
                pos += len;
            }
        }

        void skipAttr() {
            u2();
            int len = u4();
            pos += len;
        }

        byte[] codeOf(String name) {
            byte[] c = codes.get(name);
            if (c == null) throw new AssertionError("method not found: " + name);
            return c;
        }

        int u1() { return b[pos++] & 0xFF; }
        int u2() { int v = ((b[pos] & 0xFF) << 8) | (b[pos + 1] & 0xFF); pos += 2; return v; }
        int u4() { int v = ((b[pos] & 0xFF) << 24) | ((b[pos + 1] & 0xFF) << 16)
                | ((b[pos + 2] & 0xFF) << 8) | (b[pos + 3] & 0xFF); pos += 4; return v; }
    }
}
