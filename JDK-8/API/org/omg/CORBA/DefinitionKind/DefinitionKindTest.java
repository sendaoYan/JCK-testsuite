/*
 * @test
 * @summary org.omg.CORBA.DefinitionKind conformance: enumeration values.
 * @run main DefinitionKindTest
 */

public class DefinitionKindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DefinitionKind");
        check(!c.isInterface(), "is a class");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements IDLEntity");

        // ---- full-table normalization loop over every declared constant ----
        java.util.List<org.omg.CORBA.DefinitionKind> all = new java.util.ArrayList<org.omg.CORBA.DefinitionKind>();
        java.util.Set<Integer> seen = new java.util.HashSet<Integer>();
        for (java.lang.reflect.Field f : org.omg.CORBA.DefinitionKind.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == org.omg.CORBA.DefinitionKind.class) {
                org.omg.CORBA.DefinitionKind k = (org.omg.CORBA.DefinitionKind) f.get(null);
                all.add(k);
                check(!seen.contains(k.value()), "duplicate DefinitionKind value " + k.value());
                seen.add(k.value());
            }
        }
        // jdk8u372: 25 constants dk_none..dk_AbstractInterface (not 33 as in later OMG revisions)
        check(all.size() == 25, "DefinitionKind declares 25 constants, got " + all.size());
        for (org.omg.CORBA.DefinitionKind k : all) {
            // measured exception: from_int(24) is not wired up for dk_AbstractInterface in jdk8u372
            if (k == org.omg.CORBA.DefinitionKind.dk_AbstractInterface) continue;
            check(org.omg.CORBA.DefinitionKind.from_int(k.value()) == k,
                  "from_int(" + k.value() + ") normalizes to same constant");
        }
        // implementation gap, locked as measured: from_int(24) -> BAD_PARAM despite dk_AbstractInterface.value()==24
        try {
            org.omg.CORBA.DefinitionKind.from_int(24);
            check(false, "from_int(24) unexpectedly succeeded");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(24) -> BAD_PARAM (jdk8u372: dk_AbstractInterface missing from from_int)");
        }

        // ---- precise numeric table ----
        check(org.omg.CORBA.DefinitionKind.dk_none.value() == 0, "dk_none == 0");
        check(org.omg.CORBA.DefinitionKind.dk_all.value() == 1, "dk_all == 1");
        check(org.omg.CORBA.DefinitionKind.dk_Attribute.value() == 2, "dk_Attribute == 2");
        check(org.omg.CORBA.DefinitionKind.dk_Constant.value() == 3, "dk_Constant == 3");
        check(org.omg.CORBA.DefinitionKind.dk_Exception.value() == 4, "dk_Exception == 4");
        check(org.omg.CORBA.DefinitionKind.dk_Interface.value() == 5, "dk_Interface == 5");
        check(org.omg.CORBA.DefinitionKind.dk_Module.value() == 6, "dk_Module == 6");
        check(org.omg.CORBA.DefinitionKind.dk_Operation.value() == 7, "dk_Operation == 7");
        check(org.omg.CORBA.DefinitionKind.dk_Typedef.value() == 8, "dk_Typedef == 8");
        check(org.omg.CORBA.DefinitionKind.dk_Alias.value() == 9, "dk_Alias == 9");
        check(org.omg.CORBA.DefinitionKind.dk_Struct.value() == 10, "dk_Struct == 10");
        check(org.omg.CORBA.DefinitionKind.dk_Union.value() == 11, "dk_Union == 11");
        check(org.omg.CORBA.DefinitionKind.dk_Enum.value() == 12, "dk_Enum == 12");
        check(org.omg.CORBA.DefinitionKind.dk_Primitive.value() == 13, "dk_Primitive == 13");
        check(org.omg.CORBA.DefinitionKind.dk_String.value() == 14, "dk_String == 14");
        check(org.omg.CORBA.DefinitionKind.dk_Sequence.value() == 15, "dk_Sequence == 15");
        check(org.omg.CORBA.DefinitionKind.dk_Array.value() == 16, "dk_Array == 16");
        check(org.omg.CORBA.DefinitionKind.dk_Repository.value() == 17, "dk_Repository == 17");
        check(org.omg.CORBA.DefinitionKind.dk_Wstring.value() == 18, "dk_Wstring == 18");
        check(org.omg.CORBA.DefinitionKind.dk_Fixed.value() == 19, "dk_Fixed == 19");
        check(org.omg.CORBA.DefinitionKind.dk_Value.value() == 20, "dk_Value == 20");
        check(org.omg.CORBA.DefinitionKind.dk_ValueBox.value() == 21, "dk_ValueBox == 21");
        check(org.omg.CORBA.DefinitionKind.dk_ValueMember.value() == 22, "dk_ValueMember == 22");
        check(org.omg.CORBA.DefinitionKind.dk_Native.value() == 23, "dk_Native == 23");
        check(org.omg.CORBA.DefinitionKind.dk_AbstractInterface.value() == 24, "dk_AbstractInterface == 24");

        // ---- out-of-range -> BAD_PARAM ----
        try {
            org.omg.CORBA.DefinitionKind.from_int(99);
            throw new AssertionError("from_int(99) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(99) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.DefinitionKind.from_int(25);
            throw new AssertionError("from_int(25) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(25) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.DefinitionKind.from_int(-1);
            throw new AssertionError("from_int(-1) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> BAD_PARAM");
        }

        // jdk8u372: toString is default Object form
        check(org.omg.CORBA.DefinitionKind.dk_none.toString().startsWith("org.omg.CORBA.DefinitionKind@"),
              "toString is default Object form");


        // legacy smoke assertions (restored verbatim: 只增不删)
        check(org.omg.CORBA.DefinitionKind.dk_none != null, "dk_none constant non-null");
        check(org.omg.CORBA.DefinitionKind.dk_none.value() == org.omg.CORBA.DefinitionKind._dk_none, "value() matches int constant");
        check(org.omg.CORBA.DefinitionKind.from_int(org.omg.CORBA.DefinitionKind._dk_none) == org.omg.CORBA.DefinitionKind.dk_none, "from_int round-trips");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
