/*
 * @test
 * @summary JVMS 4.1: this_class and super_class -- Object has super_class 0 and
 *          this_class java/lang/Object; a normal class has super_class
 *          java/lang/Object and a nonzero this_class
 * @run main ThisSuperClassTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

public class ThisSuperClassTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = in.read(buf)) >= 0) o.write(buf, 0, n);
        return o.toByteArray();
    }

    // Returns { thisClassName, superClassName } (superClassName is null when
    // super_class == 0, which is only legal for java.lang.Object).
    static String[] names(byte[] b) throws Exception {
        DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(b));
        d.readInt(); d.readUnsignedShort(); d.readUnsignedShort();
        int n = d.readUnsignedShort();
        String[] utf = new String[n];
        int[] clsName = new int[n];
        for (int i = 1; i < n; i++) {
            int tag = d.readUnsignedByte();
            if (tag == 1) { int len = d.readUnsignedShort(); byte[] s = new byte[len]; d.readFully(s); utf[i] = new String(s, "UTF-8"); }
            else if (tag == 7) clsName[i] = d.readUnsignedShort();
            else if (tag == 3 || tag == 4) d.readInt();
            else if (tag == 5 || tag == 6) { d.readLong(); i++; }
            else if (tag == 8 || tag == 16 || tag == 19 || tag == 20) d.readUnsignedShort();
            else if (tag == 9 || tag == 10 || tag == 11 || tag == 12 || tag == 17 || tag == 18) { d.readUnsignedShort(); d.readUnsignedShort(); }
            else if (tag == 15) { d.readUnsignedByte(); d.readUnsignedShort(); }
        }
        d.readUnsignedShort(); // access_flags
        int tc = d.readUnsignedShort();
        int sc = d.readUnsignedShort();
        String thisName = (tc != 0 && clsName[tc] != 0) ? utf[clsName[tc]] : null;
        String superName = (sc == 0) ? null : utf[clsName[sc]];
        return new String[]{ thisName, superName };
    }

    public static void main(String[] args) throws Exception {
        String[] o = names(readAll(Object.class.getResourceAsStream("Object.class")));
        check("java/lang/Object".equals(o[0]), "Object this_class is java/lang/Object: " + o[0]);
        check(o[1] == null, "Object super_class == 0");

        String[] s = names(readAll(ThisSuperClassTest.class.getResourceAsStream("ThisSuperClassTest.class")));
        check("ThisSuperClassTest".equals(s[0]), "own this_class name: " + s[0]);
        check("java/lang/Object".equals(s[1]), "own super_class is java/lang/Object: " + s[1]);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
