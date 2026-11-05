/*
 * @test
 * @summary org.omg.CORBA.portable.InputStream conformance: abstract class shape.
 * @run main InputStreamTest
 */

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.InputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.io.InputStream.class.isAssignableFrom(c), "extends java.io.InputStream");

        // ---- deepened: 12-type CDR read matrix via offline ORB streams (jdk8u372 measured) ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        os.write_octet((byte) 100);
        os.write_short((short) -300);
        os.write_ushort((short) 6000);
        os.write_long(-70000);
        os.write_ulong(2100000000);
        os.write_longlong(-5000000000L);
        os.write_ulonglong(9000000000L);
        os.write_float(3.5f);
        os.write_double(4.75);
        os.write_boolean(true);
        os.write_char('c');
        os.write_string("cdr");
        os.write_wstring("wcdr");
        org.omg.CORBA.portable.InputStream is = os.create_input_stream();
        check(is.read_octet() == 100, "CDR octet round-trip");
        check(is.read_short() == (short) -300, "CDR short round-trip");
        check(is.read_ushort() == 6000, "CDR ushort round-trip");
        check(is.read_long() == -70000, "CDR long round-trip");
        check(is.read_ulong() == 2100000000, "CDR ulong round-trip");
        check(is.read_longlong() == -5000000000L, "CDR longlong round-trip");
        check(is.read_ulonglong() == 9000000000L, "CDR ulonglong round-trip");
        check(is.read_float() == 3.5f, "CDR float round-trip");
        check(is.read_double() == 4.75, "CDR double round-trip");
        check(is.read_boolean(), "CDR boolean round-trip");
        check(is.read_char() == 'c', "CDR char round-trip");
        check("cdr".equals(is.read_string()), "CDR string round-trip");
        check("wcdr".equals(is.read_wstring()), "CDR wstring round-trip");
        check(is instanceof org.omg.CORBA_2_3.portable.InputStream,
              "runtime stream is a CORBA_2_3.portable.InputStream (narrowing works)");
        // array reads with offset/length
        org.omg.CORBA.portable.OutputStream osa = orb.create_output_stream();
        osa.write_long_array(new int[]{1, 2, 3, 4}, 1, 2);
        int[] arr = new int[2];
        org.omg.CORBA.portable.InputStream isa = osa.create_input_stream();
        isa.read_long_array(arr, 0, 2);
        check(arr[0] == 2 && arr[1] == 3, "read_long_array(offset,len) honors bounds");
        check(isa.available() == 0, "available()==0 on encapsulated stream");
        // any / TypeCode / Object channels on fresh streams
        org.omg.CORBA.portable.OutputStream osAny = orb.create_output_stream();
        org.omg.CORBA.Any a = orb.create_any();
        a.insert_long(31);
        osAny.write_any(a);
        check(osAny.create_input_stream().read_any().extract_long() == 31, "read_any round-trip");
        org.omg.CORBA.portable.OutputStream osTc = orb.create_output_stream();
        org.omg.CORBA.TypeCode tc = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B"});
        osTc.write_TypeCode(tc);
        check(osTc.create_input_stream().read_TypeCode().equal(tc), "read_TypeCode round-trip equal");
        org.omg.CORBA.portable.OutputStream osObj = orb.create_output_stream();
        osObj.write_Object(null);
        check(osObj.create_input_stream().read_Object() == null, "read_Object after write_Object(null)");
        // method surface anchors
        check(c.getMethod("read_string").getReturnType() == String.class, "read_string returns String");
        check(c.getMethod("read_fixed").getReturnType() == java.math.BigDecimal.class, "read_fixed returns BigDecimal");
        check(c.getMethod("read_Context").getReturnType() == org.omg.CORBA.Context.class, "read_Context returns Context");
        check(c.getMethod("orb").getReturnType() == org.omg.CORBA.ORB.class, "orb() returns ORB");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
