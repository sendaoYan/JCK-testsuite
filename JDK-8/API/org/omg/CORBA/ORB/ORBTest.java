/*
 * @test
 * @summary org.omg.CORBA.ORB conformance: abstract class shape.
 * @run main ORBTest
 */

public class ORBTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(String what, Runnable r, Class<? extends Throwable> ex, String msg) {
        try {
            r.run();
            throw new AssertionError(what + " should throw " + ex.getName());
        } catch (Throwable t) {
            if (ex.isInstance(t)) {
                check(true, msg);
            } else {
                throw new AssertionError(what + " threw " + t + " expected " + ex.getName(), t);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ORB");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        // ---- Dual factory forms (jdk8u372 measured) ----
        org.omg.CORBA.ORB o1 = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.ORB o2 = org.omg.CORBA.ORB.init(new String[0], null);
        check(o1 != o2, "init(String[],Properties) creates distinct instances");
        check(o1 != null && o2 != null, "init(String[],null) non-null");
        org.omg.CORBA.ORB s1 = org.omg.CORBA.ORB.init();
        org.omg.CORBA.ORB s2 = org.omg.CORBA.ORB.init();
        check(s1 == s2, "init() no-arg returns same singleton instance");
        check(s1 != o1, "singleton differs from per-call instances");

        // ---- Offline factory matrix: value objects ----
        org.omg.CORBA.Any any = o1.create_any();
        check(any != null, "create_any() non-null");
        any.insert_long(42);
        check(any.extract_long() == 42, "created Any is usable");

        org.omg.CORBA.NVList list = o1.create_list(4);
        check(list != null, "create_list(n) non-null");
        check(list.count() == 0, "create_list(n) starts empty (n is capacity hint)");

        org.omg.CORBA.NamedValue nv = o1.create_named_value("n", any, 1);
        check(nv != null && "n".equals(nv.name()), "create_named_value works offline");

        org.omg.CORBA.portable.OutputStream os = o1.create_output_stream();
        check(os != null, "create_output_stream() non-null");
        os.write_long(7);
        check(os.create_input_stream().read_long() == 7, "output stream CDR round-trip via ORB factory");

        check(o1.create_environment() != null, "create_environment() works offline");
        check(o1.create_exception_list() != null, "create_exception_list() works offline");
        check(o1.create_context_list() != null, "create_context_list() works offline");

        // ---- Offline factory matrix: primitive TypeCode ----
        check(o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_long).kind() == org.omg.CORBA.TCKind.tk_long,
              "get_primitive_tc(tk_long) kind round-trip");
        check(o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_string).kind() == org.omg.CORBA.TCKind.tk_string,
              "get_primitive_tc(tk_string) kind round-trip");
        check(o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_any).kind() == org.omg.CORBA.TCKind.tk_any,
              "get_primitive_tc(tk_any) kind round-trip");

        // ---- Offline factory matrix: compound TypeCode constructors ----
        org.omg.CORBA.StructMember[] sm = {
            new org.omg.CORBA.StructMember("m", o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null) };
        org.omg.CORBA.TypeCode stc = o1.create_struct_tc("IDL:S:1.0", "S", sm);
        check(stc.kind() == org.omg.CORBA.TCKind.tk_struct, "create_struct_tc kind");
        check("IDL:s:1.0".equals(stc.id()) || "IDL:S:1.0".equals(stc.id()), "create_struct_tc id");

        org.omg.CORBA.TypeCode etc = o1.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B"});
        check(etc.kind() == org.omg.CORBA.TCKind.tk_enum, "create_enum_tc kind");
        check(etc.member_count() == 2, "create_enum_tc member count");

        org.omg.CORBA.Any lbl = o1.create_any();
        lbl.insert_long(1);
        org.omg.CORBA.UnionMember[] um = {
            new org.omg.CORBA.UnionMember("f", lbl, o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null) };
        org.omg.CORBA.TypeCode utc = o1.create_union_tc("IDL:U:1.0", "U",
            o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), um);
        check(utc.kind() == org.omg.CORBA.TCKind.tk_union, "create_union_tc kind");

        org.omg.CORBA.TypeCode atc = o1.create_alias_tc("IDL:A:1.0", "A",
            o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
        check(atc.kind() == org.omg.CORBA.TCKind.tk_alias, "create_alias_tc kind");

        org.omg.CORBA.TypeCode sqtc = o1.create_sequence_tc(0, o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        check(sqtc.kind() == org.omg.CORBA.TCKind.tk_sequence, "create_sequence_tc kind");
        org.omg.CORBA.TypeCode artc = o1.create_array_tc(2, o1.get_primitive_tc(org.omg.CORBA.TCKind.tk_octet));
        check(artc.kind() == org.omg.CORBA.TCKind.tk_array, "create_array_tc kind");
        org.omg.CORBA.TypeCode strtc = o1.create_string_tc(5);
        check(strtc.kind() == org.omg.CORBA.TCKind.tk_string, "create_string_tc kind");
        check(strtc.length() == 5, "create_string_tc length");
        org.omg.CORBA.TypeCode wstc = o1.create_wstring_tc(0);
        check(wstc.kind() == org.omg.CORBA.TCKind.tk_wstring, "create_wstring_tc kind");

        // ---- Negative paths with precise exception types (jdk8u372 measured) ----
        String s = o1.object_to_string(null);
        check(s != null && !s.isEmpty(), "object_to_string(null) does not throw, returns non-empty string");
        check(s.startsWith("IOR:"), "object_to_string(null) returns an IOR: string");

        expectThrows("string_to_object(bogus)", () -> o1.string_to_object("bogus"),
                     org.omg.CORBA.BAD_PARAM.class, "string_to_object(bogus) -> BAD_PARAM");
        expectThrows("connect(null)", () -> o1.connect(null),
                     org.omg.CORBA.OBJ_ADAPTER.class, "connect(null) -> OBJ_ADAPTER");
        expectThrows("disconnect(null)", () -> o1.disconnect(null),
                     org.omg.CORBA.OBJ_ADAPTER.class, "disconnect(null) -> OBJ_ADAPTER");
        expectThrows("get_default_context()", () -> o1.get_default_context(),
                     org.omg.CORBA.NO_IMPLEMENT.class, "get_default_context() -> NO_IMPLEMENT");
        expectThrows("get_current()", () -> o1.get_current(),
                     org.omg.CORBA.NO_IMPLEMENT.class, "get_current() -> NO_IMPLEMENT");
        expectThrows("work_pending()", () -> o1.work_pending(),
                     org.omg.CORBA.NO_IMPLEMENT.class, "work_pending() -> NO_IMPLEMENT");
        expectThrows("perform_work()", () -> o1.perform_work(),
                     org.omg.CORBA.NO_IMPLEMENT.class, "perform_work() -> NO_IMPLEMENT");
        expectThrows("create_operation_list((Object)null)", () -> o1.create_operation_list((org.omg.CORBA.Object) null),
                     org.omg.CORBA.NO_IMPLEMENT.class, "create_operation_list(Object) -> NO_IMPLEMENT");
        expectThrows("get_service_information", () -> o1.get_service_information((short) 1,
                     new org.omg.CORBA.ServiceInformationHolder()),
                     org.omg.CORBA.NO_IMPLEMENT.class, "get_service_information -> NO_IMPLEMENT");

        try {
            o1.string_to_object("bogus");
        } catch (org.omg.CORBA.BAD_PARAM bp) {
            check(bp.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                  "string_to_object(bogus) BAD_PARAM completed=COMPLETED_NO");
            check(bp.minor == (org.omg.CORBA.OMGVMCID.value | 7),
                  "string_to_object(bogus) BAD_PARAM minor == OMGVMCID|7");
        }
        try {
            o1.connect(null);
        } catch (org.omg.CORBA.OBJ_ADAPTER oa) {
            check(oa.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                  "connect(null) OBJ_ADAPTER completed=COMPLETED_NO");
        }

        // ---- Lifecycle: shutdown(false)/destroy() do not throw ----
        org.omg.CORBA.ORB o3 = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            o3.shutdown(false);
            check(true, "shutdown(false) does not throw");
        } catch (Throwable t) {
            throw new AssertionError("shutdown(false) threw " + t, t);
        }
        try {
            o3.destroy();
            check(true, "destroy() does not throw");
        } catch (Throwable t) {
            throw new AssertionError("destroy() threw " + t, t);
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
