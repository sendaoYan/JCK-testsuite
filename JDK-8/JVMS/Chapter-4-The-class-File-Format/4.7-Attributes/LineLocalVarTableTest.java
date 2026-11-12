/*
 * @test
 * @summary JVMS 4.7.12/4.7.13: a compiled method body carries a LineNumberTable
 *          attribute (mapping bytecode offsets to source lines) and a
 *          LocalVariableTable attribute (describing the live local variables).
 *          The helper is compiled with -g, then this test parses its class
 *          bytes, locates the Code attribute of a method that spans multiple
 *          source lines and local variables, and asserts both attributes are
 *          present and well-formed.
 * @compile -g LineLocalVarHelper.java
 * @run main LineLocalVarTableTest
 */

import java.io.InputStream;

public class LineLocalVarTableTest {

    static byte[] readAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    static final class Parser {
        final byte[] b;
        int pos;
        String[] cp;

        Parser(byte[] b) { this.b = b; }

        int u1() { return b[pos++] & 0xFF; }
        int u2() { int v = ((b[pos] & 0xFF) << 8) | (b[pos + 1] & 0xFF); pos += 2; return v; }
        long u4() {
            long v = ((long) (b[pos] & 0xFF) << 24) | ((b[pos + 1] & 0xFF) << 16)
                    | ((b[pos + 2] & 0xFF) << 8) | (b[pos + 3] & 0xFF);
            pos += 4; return v;
        }
        String utf8() { int n = u2(); String s = new String(b, pos, n, java.nio.charset.StandardCharsets.UTF_8); pos += n; return s; }

        void readConstantPool() {
            int count = u2();
            cp = new String[count];
            for (int i = 1; i < count; i++) {
                int tag = u1();
                switch (tag) {
                    case 1:  cp[i] = utf8(); break;
                    case 7: case 8: case 16: case 19: case 20: u2(); break;              // Class/String/MethodType/Module/Package
                    case 15: u1(); u2(); break;                                           // MethodHandle
                    case 3: case 4: u4(); break;                                          // Integer/Float
                    case 9: case 10: case 11: case 12: case 17: case 18: u2(); u2(); break; // refs/NameAndType/Dynamic/InvokeDynamic
                    case 5: case 6: u4(); u4(); i++; break;                               // Long/Double take two slots
                    default: throw new AssertionError("unknown cp tag " + tag);
                }
            }
        }

        // Returns the Code attribute offset and length (absolute into b), or
        // -1 if the named method has no Code attribute.
        long[] findCode(String methodName) {
            u4(); // magic
            u2(); u2(); // minor/major
            readConstantPool();
            u2(); u2(); u2(); // access, this, super
            int ifaces = u2(); for (int i = 0; i < ifaces; i++) u2();
            int fields = u2();
            for (int i = 0; i < fields; i++) { u2(); u2(); u2(); skipAttrs(); }
            int methods = u2();
            for (int i = 0; i < methods; i++) {
                u2(); // access
                int name = u2(); u2(); // descriptor
                int attrCount = u2();
                int nameIdx = name;
                String n = cp[nameIdx];
                for (int a = 0; a < attrCount; a++) {
                    int attrName = u2();
                    long attrLen = u4();
                    int attrStart = pos;
                    if (n.equals(methodName) && cp[attrName].equals("Code")) {
                        return new long[]{attrStart, attrLen};
                    }
                    pos = attrStart + (int) attrLen;
                }
            }
            return new long[]{-1, -1};
        }

        void skipAttrs() { int n = u2(); for (int i = 0; i < n; i++) { u2(); pos += (int) u4(); } }
    }

    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = ClassLoader.getSystemResourceAsStream("LineLocalVarHelper.class")) {
            if (in == null) throw new AssertionError("LineLocalVarHelper.class not found");
            b = readAll(in);
        }

        Parser p = new Parser(b);
        long[] code = p.findCode("compute");
        if (code[0] < 0) throw new AssertionError("compute method Code attribute not found");

        int codeEnd = (int) (code[0] + code[1]);
        // Code attribute: max_stack, max_locals, code_length, code, exception
        // table, then sub-attributes.
        int q = (int) code[0];
        int maxStack = readU2(b, q); q += 2;
        int maxLocals = readU2(b, q); q += 2;
        int codeLen = readU4(b, q); q += 4;
        q += codeLen;
        int excLen = readU2(b, q); q += 2;
        q += 8 * excLen;
        int subCount = readU2(b, q); q += 2;

        boolean hasLine = false, hasLocal = false;
        int lineEntries = -1, localEntries = -1;
        for (int i = 0; i < subCount; i++) {
            int nameIdx = readU2(b, q); q += 2;
            int len = readU4(b, q); q += 4;
            String name = p.cp[nameIdx];
            if (name.equals("LineNumberTable")) {
                hasLine = true;
                lineEntries = readU2(b, q);
            } else if (name.equals("LocalVariableTable")) {
                hasLocal = true;
                localEntries = readU2(b, q);
            }
            q += len;
        }

        if (q != codeEnd) throw new AssertionError("Code sub-attributes overrun boundary");

        if (!hasLine) throw new AssertionError("no LineNumberTable attribute");
        if (!hasLocal) throw new AssertionError("no LocalVariableTable attribute");
        if (lineEntries < 1) throw new AssertionError("LineNumberTable has no entries");
        if (localEntries < 3) throw new AssertionError("LocalVariableTable has only " + localEntries + " entries");

        System.out.println("PASS (LineNumberTable " + lineEntries + " entries, LocalVariableTable "
                + localEntries + " entries, max_stack=" + maxStack + ", max_locals=" + maxLocals + ")");
    }

    static int readU2(byte[] b, int p) { return ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF); }
    static int readU4(byte[] b, int p) {
        return ((b[p] & 0xFF) << 24) | ((b[p + 1] & 0xFF) << 16)
                | ((b[p + 2] & 0xFF) << 8) | (b[p + 3] & 0xFF);
    }
}
