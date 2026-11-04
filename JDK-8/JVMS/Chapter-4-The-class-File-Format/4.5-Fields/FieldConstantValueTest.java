/*
 * @test
 * @summary JVMS 4.7.2/4.5: a ConstantValue attribute whose constant type does not
 *          match the field's descriptor (a CONSTANT_Long on an int field) is
 *          rejected with ClassFormatError
 * @run main FieldConstantValueTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FieldConstantValueTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static Throwable loadError(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        try {
            URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
            Class.forName(name, true, cl);
            return null;
        } catch (Throwable t) { return t; }
    }

    static byte[] build() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 14);                                     // cp_count = 14
        u1(d, 7); u2(d, 2); utf8(d, "X");              // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        utf8(d, "f");               // #5
        utf8(d, "I");               // #6
        utf8(d, "ConstantValue");   // #7
        utf8(d, "<init>");          // #8
        utf8(d, "()V");             // #9
        utf8(d, "Code");            // #10
        u1(d, 10); u2(d, 3); u2(d, 12);  // #11 Methodref Object.<init>
        u1(d, 12); u2(d, 8); u2(d, 9);   // #12 NameAndType <init>:()V
        u1(d, 5); d.writeLong(42);        // #13 Long 42 (wrong type for an int field)

        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0); // access, this, super, interfaces
        u2(d, 1);                                    // fields_count = 1
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1); // public static f:I + 1 attr
        u2(d, 7); u4(d, 2); u2(d, 13);                // ConstantValue attr -> #13 (Long)
        u2(d, 1);                                    // methods_count = 1
        u2(d, 0x0001); u2(d, 8); u2(d, 9); u2(d, 1); // public <init>()V + 1 attr
        u2(d, 10); u4(d, 12 + 5); u2(d, 1); u2(d, 1); u4(d, 5); // Code
        u1(d, 0x2A); u1(d, 0xB7); u2(d, 11); u1(d, 0xB1); // aload_0, invokespecial #11, return
        u2(d, 0); u2(d, 0);                          // exception_table_length, code_attributes
        u2(d, 0);                                    // class attributes_count
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Throwable t = loadError("X", build());
        check(t instanceof ClassFormatError, "ConstantValue type mismatch rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
