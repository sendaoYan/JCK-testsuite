/*
 * @test
 * @summary JVMS 4.3: illegal field descriptor 'V' and malformed method descriptor are rejected with ClassFormatError
 * @run main IllegalDescriptorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class IllegalDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = in.read(buf)) >= 0) o.write(buf, 0, n);
        return o.toByteArray();
    }

    static Class<?> load(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
        return Class.forName(name, true, cl);
    }

    static Throwable loadError(String name, byte[] b) throws Exception {
        try { load(name, b); return null; } catch (Throwable t) { return t; }
    }


    // A class file whose field has the illegal descriptor "V" (void is not a field type)
    static byte[] badFieldDesc() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 7);                                    // cp_count = 7
        u1(d, 7); u2(d, 2);                          // #1 Class -> #2
        utf8(d, "X");                                // #2
        u1(d, 7); u2(d, 4);                          // #3 Class -> #4
        utf8(d, "java/lang/Object");                 // #4
        utf8(d, "f");                                // #5 field name
        utf8(d, "V");                                // #6 field descriptor "V" (illegal)
        u2(d, 0x0021); u2(d, 1); u2(d, 3);           // access, this, super
        u2(d, 0);                                    // interfaces
        u2(d, 1);                                    // fields_count = 1
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 0); // public static f : V
        u2(d, 0);                                    // methods_count
        u2(d, 0);                                    // attributes_count
        return o.toByteArray();
    }

    // A method descriptor whose return type is missing (empty descriptor) is illegal.
    static byte[] badMethodDesc() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 7);                                    // cp_count = 7
        u1(d, 7); u2(d, 2); utf8(d, "X");            // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        utf8(d, "m");                                // #5 method name
        utf8(d, "(");                                // #6 descriptor "(" (illegal, no return)
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);                                    // fields_count
        u2(d, 1);                                    // methods_count = 1
        u2(d, 0x0001); u2(d, 5); u2(d, 6); u2(d, 0); // public m : "("
        u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Throwable t1 = loadError("X", badFieldDesc());
        check(t1 instanceof ClassFormatError, "void field descriptor rejected: " + t1);
        Throwable t2 = loadError("X", badMethodDesc());
        check(t2 instanceof ClassFormatError, "malformed method descriptor rejected: " + t2);
        System.out.println("PASS (" + passed + " assertions)");
    }

}
