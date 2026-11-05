/*
 * @test
 * @summary org.omg.CORBA.Any conformance: abstract class shape.
 * @run main AnyTest
 */

public class AnyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static org.omg.CORBA.ORB orb;

    static void expectBadOp(String what, Runnable r) {
        try {
            r.run();
            throw new AssertionError(what + " should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, what + " -> BAD_OPERATION");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Any");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.IDLEntity");

        orb = org.omg.CORBA.ORB.init(new String[0], null);

        // ---- 14-type insert/extract matrix via offline ORB factory ----
        org.omg.CORBA.Any a = orb.create_any();
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_null, "fresh Any type kind is tk_null");

        a.insert_long(42);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_long, "insert_long kind");
        check(a.extract_long() == 42, "insert_long/extract_long round-trip");
        a.insert_long(Integer.MIN_VALUE);
        check(a.extract_long() == Integer.MIN_VALUE, "long MIN_VALUE round-trip");
        a.insert_long(Integer.MAX_VALUE);
        check(a.extract_long() == Integer.MAX_VALUE, "long MAX_VALUE round-trip");

        a.insert_short((short) -1234);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_short, "insert_short kind");
        check(a.extract_short() == (short) -1234, "short round-trip");
        a.insert_ushort((short) 4000);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_ushort, "insert_ushort kind");
        check(a.extract_ushort() == 4000, "ushort round-trip");
        a.insert_ulong(2100000000);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_ulong, "insert_ulong kind");
        check(a.extract_ulong() == 2100000000, "ulong round-trip");
        a.insert_longlong(-1234567890123L);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_longlong, "insert_longlong kind");
        check(a.extract_longlong() == -1234567890123L, "longlong round-trip");
        a.insert_ulonglong(123456789012345L);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_ulonglong, "insert_ulonglong kind");
        check(a.extract_ulonglong() == 123456789012345L, "ulonglong round-trip");
        a.insert_float(1.5f);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_float, "insert_float kind");
        check(a.extract_float() == 1.5f, "float round-trip");
        a.insert_double(-2.25e100);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_double, "insert_double kind");
        check(a.extract_double() == -2.25e100, "double round-trip");
        a.insert_boolean(true);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_boolean, "insert_boolean kind");
        check(a.extract_boolean(), "boolean true round-trip");
        a.insert_boolean(false);
        check(!a.extract_boolean(), "boolean false round-trip");
        a.insert_char('Z');
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_char, "insert_char kind");
        check(a.extract_char() == 'Z', "char round-trip");
        a.insert_octet((byte) -100);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_octet, "insert_octet kind");
        check(a.extract_octet() == (byte) -100, "octet round-trip");
        a.insert_string("hello");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_string, "insert_string kind");
        check("hello".equals(a.extract_string()), "string round-trip");
        a.insert_string("");
        check("".equals(a.extract_string()), "empty string round-trip");
        a.insert_wstring("wide-中");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_wstring, "insert_wstring kind");
        check("wide-中".equals(a.extract_wstring()), "wstring round-trip");
        a.insert_wchar('W');
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_wchar, "insert_wchar kind");
        check(a.extract_wchar() == 'W', "wchar round-trip");

        // objref(null) is legal, kind becomes tk_objref
        a.insert_Object(null);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_objref, "insert_Object(null) kind tk_objref");
        check(a.extract_Object() == null, "extract_Object after insert null returns null");

        // TypeCode channel
        org.omg.CORBA.TypeCode tc = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A"});
        a.insert_TypeCode(tc);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_TypeCode, "insert_TypeCode kind");
        check(a.extract_TypeCode().equal(tc), "TypeCode round-trip via equal");

        // nested Any channel
        org.omg.CORBA.Any inner = orb.create_any();
        inner.insert_long(99);
        a.insert_any(inner);
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_any, "insert_any kind");
        check(a.extract_any().extract_long() == 99, "nested any round-trip");

        // ---- type-overwrite semantics ----
        org.omg.CORBA.Any w = orb.create_any();
        w.insert_long(42);
        check(w.type().kind() == org.omg.CORBA.TCKind.tk_long, "pre-overwrite kind tk_long");
        w.insert_string("str");
        check(w.type().kind() == org.omg.CORBA.TCKind.tk_string, "insert_string overwrites kind to tk_string");
        check("str".equals(w.extract_string()), "overwritten value extracts as string");
        w.insert_long(7);
        check(w.type().kind() == org.omg.CORBA.TCKind.tk_long, "insert_long overwrites back to tk_long");
        check(w.extract_long() == 7, "second overwrite value wins");

        // ---- extract type mismatch -> BAD_OPERATION (SUN minor 206) ----
        org.omg.CORBA.Any m = orb.create_any();
        m.insert_long(5);
        expectBadOp("extract_string on long any", () -> m.extract_string());
        expectBadOp("extract_double on long any", () -> m.extract_double());
        expectBadOp("extract_any on long any", () -> m.extract_any());
        expectBadOp("extract_Object on long any", () -> m.extract_Object());
        expectBadOp("extract_TypeCode on long any", () -> m.extract_TypeCode());
        expectBadOp("extract_boolean on long any", () -> m.extract_boolean());
        try {
            m.extract_string();
        } catch (org.omg.CORBA.BAD_OPERATION bo) {
            check(bo.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO, "BAD_OPERATION completed=NO");
        }
        org.omg.CORBA.Any ms = orb.create_any();
        ms.insert_string("x");
        expectBadOp("extract_long on string any", () -> ms.extract_long());
        // fresh (tk_null) any rejects every extract
        org.omg.CORBA.Any empty = orb.create_any();
        expectBadOp("extract_long on empty any", () -> empty.extract_long());
        expectBadOp("extract_string on empty any", () -> empty.extract_string());

        // ---- Any.create_input_stream read-back channel ----
        org.omg.CORBA.Any ri = orb.create_any();
        ri.insert_long(777);
        check(ri.create_input_stream().read_long() == 777, "create_input_stream read_long");
        org.omg.CORBA.Any rs = orb.create_any();
        rs.insert_string("strval");
        check("strval".equals(rs.create_input_stream().read_string()), "create_input_stream read_string");
        org.omg.CORBA.Any rb = orb.create_any();
        rb.insert_boolean(true);
        check(rb.create_input_stream().read_boolean(), "create_input_stream read_boolean");
        org.omg.CORBA.Any rd = orb.create_any();
        rd.insert_double(0.5);
        check(rd.create_input_stream().read_double() == 0.5, "create_input_stream read_double");
        check(orb.create_any().create_output_stream() != null, "create_output_stream non-null");

        // ---- equal: value-based across distinct instances ----
        org.omg.CORBA.Any e1 = orb.create_any(); e1.insert_long(5);
        org.omg.CORBA.Any e2 = orb.create_any(); e2.insert_long(5);
        org.omg.CORBA.Any e3 = orb.create_any(); e3.insert_long(6);
        check(e1.equal(e2), "equal: same value distinct instances true");
        check(!e1.equal(e3), "equal: different values false");
        org.omg.CORBA.Any em1 = orb.create_any(), em2 = orb.create_any();
        check(em1.equal(em2), "equal: two empty anys true");
        check(!e1.equal(em1), "equal: typed vs empty false");

        // ---- read_value/write_value exist on the abstract contract ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method mm : org.omg.CORBA.Any.class.getMethods()) names.add(mm.getName());
        check(names.contains("read_value"), "Any declares read_value");
        check(names.contains("write_value"), "Any declares write_value");
        check(names.contains("insert_Streamable"), "Any declares insert_Streamable");
        check(names.contains("extract_Streamable"), "Any declares extract_Streamable");
        check(names.contains("insert_Object"), "Any declares insert_Object");
        check(names.contains("create_input_stream"), "Any declares create_input_stream");
        check(names.contains("create_output_stream"), "Any declares create_output_stream");
        check(names.contains("type"), "Any declares type");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
