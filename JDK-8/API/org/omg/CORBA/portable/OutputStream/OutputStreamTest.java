/*
 * @test
 * @summary org.omg.CORBA.portable.OutputStream conformance: abstract class shape.
 * @run main OutputStreamTest
 */

public class OutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.OutputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.io.OutputStream.class.isAssignableFrom(c), "extends java.io.OutputStream");

        // ---- deepened: CDR write matrix + create_input_stream channel (jdk8u372 measured) ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        check(os instanceof org.omg.CORBA_2_3.portable.OutputStream,
              "runtime stream is a CORBA_2_3.portable.OutputStream");
        os.write_boolean(false);
        os.write_octet((byte) 7);
        os.write_char('x');
        os.write_short((short) 32000);
        os.write_long(Integer.MIN_VALUE);
        os.write_float(0.25f);
        os.write_double(-1.5);
        os.write_string("s");
        os.write_wstring("w");
        org.omg.CORBA.portable.InputStream is = os.create_input_stream();
        check(!is.read_boolean(), "boolean false round-trip");
        check(is.read_octet() == 7, "octet round-trip");
        check(is.read_char() == 'x', "char round-trip");
        check(is.read_short() == (short) 32000, "short round-trip");
        check(is.read_long() == Integer.MIN_VALUE, "long MIN_VALUE round-trip");
        check(is.read_float() == 0.25f, "float round-trip");
        check(is.read_double() == -1.5, "double round-trip");
        check("s".equals(is.read_string()), "string round-trip");
        check("w".equals(is.read_wstring()), "wstring round-trip");
        // octet array with offset/length
        org.omg.CORBA.portable.OutputStream osa = orb.create_output_stream();
        osa.write_octet_array(new byte[]{(byte) 1, (byte) 2, (byte) 3, (byte) 4}, 0, 4);
        byte[] barr = new byte[4];
        osa.create_input_stream().read_octet_array(barr, 0, 4);
        check(barr[0] == 1 && barr[3] == 4, "write/read_octet_array round-trip");
        // any channel round-trip through write_any
        org.omg.CORBA.portable.OutputStream osAny = orb.create_output_stream();
        org.omg.CORBA.Any a = orb.create_any();
        a.insert_string("via-any");
        osAny.write_any(a);
        check("via-any".equals(osAny.create_input_stream().read_any().extract_string()),
              "write_any/read_any string round-trip");
        check(c.getMethod("create_input_stream").getReturnType() == org.omg.CORBA.portable.InputStream.class,
              "create_input_stream returns portable InputStream");
        check(c.getMethod("orb").getReturnType() == org.omg.CORBA.ORB.class, "orb() returns ORB");
        check(c.getMethod("write_fixed", java.math.BigDecimal.class) != null, "write_fixed takes BigDecimal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
