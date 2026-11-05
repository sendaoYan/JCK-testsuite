/*
 * @test
 * @summary org.omg.CORBA.TCKind conformance: enumeration values.
 * @run main TCKindTest
 */

public class TCKindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.TCKind.tk_null != null, "tk_null constant non-null");
        check(org.omg.CORBA.TCKind.tk_null.value() == org.omg.CORBA.TCKind._tk_null, "value() matches int constant");
        check(org.omg.CORBA.TCKind.from_int(org.omg.CORBA.TCKind._tk_null) == org.omg.CORBA.TCKind.tk_null, "from_int round-trips");

        // ---- full-table normalization loop over every declared constant ----
        java.util.List<org.omg.CORBA.TCKind> all = new java.util.ArrayList<org.omg.CORBA.TCKind>();
        java.util.Set<Integer> seen = new java.util.HashSet<Integer>();
        for (java.lang.reflect.Field f : org.omg.CORBA.TCKind.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == org.omg.CORBA.TCKind.class) {
                org.omg.CORBA.TCKind k = (org.omg.CORBA.TCKind) f.get(null);
                all.add(k);
                check(!seen.contains(k.value()), "duplicate TCKind value " + k.value());
                seen.add(k.value());
                check(("tk_" + nameOf(k.value())).equals(f.getName()),
                      "field name " + f.getName() + " matches _" + f.getName() + " int constant");
            }
        }
        check(all.size() == 33, "TCKind declares 33 constants (incl. deprecated tk_longdouble), got " + all.size());
        for (org.omg.CORBA.TCKind k : all) {
            check(org.omg.CORBA.TCKind.from_int(k.value()) == k,
                  "from_int(" + k.value() + ") normalizes to same constant");
        }

        // ---- precise numeric table (16 anchors from the probed values) ----
        check(org.omg.CORBA.TCKind._tk_null == 0, "_tk_null == 0");
        check(org.omg.CORBA.TCKind._tk_void == 1, "_tk_void == 1");
        check(org.omg.CORBA.TCKind._tk_short == 2, "_tk_short == 2");
        check(org.omg.CORBA.TCKind._tk_long == 3, "_tk_long == 3");
        check(org.omg.CORBA.TCKind._tk_ushort == 4, "_tk_ushort == 4");
        check(org.omg.CORBA.TCKind._tk_ulong == 5, "_tk_ulong == 5");
        check(org.omg.CORBA.TCKind._tk_float == 6, "_tk_float == 6");
        check(org.omg.CORBA.TCKind._tk_double == 7, "_tk_double == 7");
        check(org.omg.CORBA.TCKind._tk_boolean == 8, "_tk_boolean == 8");
        check(org.omg.CORBA.TCKind._tk_octet == 10, "_tk_octet == 10");
        check(org.omg.CORBA.TCKind._tk_any == 11, "_tk_any == 11");
        check(org.omg.CORBA.TCKind._tk_TypeCode == 12, "_tk_TypeCode == 12");
        check(org.omg.CORBA.TCKind._tk_struct == 15, "_tk_struct == 15");
        check(org.omg.CORBA.TCKind._tk_enum == 17, "_tk_enum == 17");
        check(org.omg.CORBA.TCKind._tk_string == 18, "_tk_string == 18");
        check(org.omg.CORBA.TCKind._tk_longdouble == 25, "_tk_longdouble == 25 (deprecated)");
        check(org.omg.CORBA.TCKind._tk_wstring == 27, "_tk_wstring == 27");
        check(org.omg.CORBA.TCKind._tk_fixed == 28, "_tk_fixed == 28");
        check(org.omg.CORBA.TCKind._tk_value == 29, "_tk_value == 29");
        check(org.omg.CORBA.TCKind._tk_value_box == 30, "_tk_value_box == 30");
        check(org.omg.CORBA.TCKind._tk_native == 31, "_tk_native == 31");
        check(org.omg.CORBA.TCKind._tk_abstract_interface == 32, "_tk_abstract_interface == 32");

        // constants agree with instance value()
        check(org.omg.CORBA.TCKind.tk_long.value() == org.omg.CORBA.TCKind._tk_long, "tk_long value()==_tk_long");
        check(org.omg.CORBA.TCKind.tk_objref.value() == org.omg.CORBA.TCKind._tk_objref, "tk_objref value()==_tk_objref");
        check(org.omg.CORBA.TCKind.tk_alias.value() == org.omg.CORBA.TCKind._tk_alias, "tk_alias value()==_tk_alias");
        check(org.omg.CORBA.TCKind.tk_except.value() == org.omg.CORBA.TCKind._tk_except, "tk_except value()==_tk_except");
        check(org.omg.CORBA.TCKind.tk_longlong.value() == org.omg.CORBA.TCKind._tk_longlong, "tk_longlong value()==_tk_longlong");
        check(org.omg.CORBA.TCKind.tk_union.value() == org.omg.CORBA.TCKind._tk_union, "tk_union value()==_tk_union");
        check(org.omg.CORBA.TCKind.tk_sequence.value() == org.omg.CORBA.TCKind._tk_sequence, "tk_sequence value()==_tk_sequence");
        check(org.omg.CORBA.TCKind.tk_array.value() == org.omg.CORBA.TCKind._tk_array, "tk_array value()==_tk_array");
        check(org.omg.CORBA.TCKind.tk_wchar.value() == org.omg.CORBA.TCKind._tk_wchar, "tk_wchar value()==_tk_wchar");
        check(org.omg.CORBA.TCKind.tk_ulonglong.value() == org.omg.CORBA.TCKind._tk_ulonglong, "tk_ulonglong value()==_tk_ulonglong");

        // ---- from_int out-of-range -> BAD_PARAM ----
        try {
            org.omg.CORBA.TCKind.from_int(99);
            throw new AssertionError("from_int(99) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(99) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.TCKind.from_int(-1);
            throw new AssertionError("from_int(-1) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.TCKind.from_int(33);
            throw new AssertionError("from_int(33) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(33) -> BAD_PARAM");
        }

        // ---- shape ----
        check(!org.omg.CORBA.TCKind.class.isInterface(), "TCKind is a class (IDL enum mapping)");
        check(!org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.TCKind.class),
              "TCKind does NOT implement IDLEntity (jdk8u372 quirk)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.CompletionStatus.class),
              "CompletionStatus does implement IDLEntity");
        // jdk8u372: toString is the default java.lang.Object form, no name embedded
        check(org.omg.CORBA.TCKind.tk_long.toString().startsWith("org.omg.CORBA.TCKind@"),
              "TCKind toString is default Object form (no name), starts with org.omg.CORBA.TCKind@");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static String nameOf(int v) {
        try {
            java.lang.reflect.Field[] fs = org.omg.CORBA.TCKind.class.getDeclaredFields();
            for (java.lang.reflect.Field f : fs) {
                if (f.getType() == int.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                        && f.getName().startsWith("_tk_") && f.getInt(null) == v) {
                    return f.getName().substring(4);
                }
            }
        } catch (Exception ignore) { }
        return "?" + v;
    }
}
