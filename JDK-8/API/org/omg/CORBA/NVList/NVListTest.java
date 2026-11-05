/*
 * @test
 * @summary org.omg.CORBA.NVList conformance: abstract class shape.
 * @run main NVListTest
 */

public class NVListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.NVList");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        // ---- create_list(n): n is a capacity hint, list starts empty ----
        org.omg.CORBA.NVList nl = orb.create_list(4);
        check(nl.count() == 0, "create_list(4) initial count == 0");
        org.omg.CORBA.NVList nl0 = orb.create_list(0);
        check(nl0.count() == 0, "create_list(0) initial count == 0");

        // ---- three add channels ----
        org.omg.CORBA.Any a1 = orb.create_any(); a1.insert_long(11);
        org.omg.CORBA.Any a2 = orb.create_any(); a2.insert_string("s2");
        org.omg.CORBA.Any a3 = orb.create_any(); a3.insert_boolean(true);

        org.omg.CORBA.NamedValue v1 = nl.add_value("first", a1, org.omg.CORBA.ARG_IN.value);
        check(nl.count() == 1, "add_value increments count");
        check(nl.item(0) == v1, "item(0) is the same NamedValue add_value returned");
        check("first".equals(nl.item(0).name()), "add_value name readback");
        check(nl.item(0).value() == a1, "add_value keeps same Any reference");
        check(nl.item(0).flags() == org.omg.CORBA.ARG_IN.value, "add_value flags readback");
        check(nl.item(0).value().extract_long() == 11, "item Any still readable");

        org.omg.CORBA.NamedValue v2 = nl.add_item("second", org.omg.CORBA.ARG_OUT.value);
        check(nl.count() == 2, "add_item increments count");
        check("second".equals(nl.item(1).name()), "add_item name readback");
        check(nl.item(1).flags() == org.omg.CORBA.ARG_OUT.value, "add_item flags readback");
        check(nl.item(1).value() != null, "add_item synthesizes a fresh (empty) Any");

        org.omg.CORBA.NamedValue v3 = nl.add_value("third", a3, org.omg.CORBA.ARG_INOUT.value);
        check(nl.count() == 3, "add_value second increments count");
        check(nl.item(2).value() == a3, "item(2) same Any");
        check(nl.item(2).flags() == org.omg.CORBA.ARG_INOUT.value, "ARG_INOUT flags readback");

        org.omg.CORBA.NamedValue v4 = nl.add(org.omg.CORBA.ARG_IN.value | org.omg.CORBA.ARG_OUT.value);
        check(nl.count() == 4, "add(flags) increments count");
        check(nl.item(3).flags() == (org.omg.CORBA.ARG_IN.value | org.omg.CORBA.ARG_OUT.value),
              "add(flags) combined flags readback");
        check(nl.item(3).name() != null, "add(flags) name is non-null (empty string)");
        check(nl.item(3).name().isEmpty(), "add(flags) name is empty string");

        // ---- item(i) identity: stable across repeated calls ----
        check(nl.item(0) == nl.item(0), "item(0) stable identity");
        check(nl.item(1) == v2 && nl.item(2) == v3, "items keep add-order positions");

        // ---- out-of-bounds -> org.omg.CORBA.Bounds (checked exception) ----
        try {
            nl.item(4);
            throw new AssertionError("item(4) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(count) -> org.omg.CORBA.Bounds");
        }
        try {
            nl.item(-1);
            throw new AssertionError("item(-1) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(-1) -> org.omg.CORBA.Bounds");
        }
        try {
            nl.remove(4);
            throw new AssertionError("remove(4) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "remove(count) -> org.omg.CORBA.Bounds");
        }

        // ---- remove decrements count and preserves the other items ----
        nl.remove(0); // drop "first"
        check(nl.count() == 3, "count decremented after remove");
        check("second".equals(nl.item(0).name()), "remaining items shift down: old item(1) now item(0)");
        check("third".equals(nl.item(1).name()), "old item(2) now item(1)");
        check(nl.item(1).value() == a3, "shifted item keeps its Any reference");
        check(nl.item(2).flags() == (org.omg.CORBA.ARG_IN.value | org.omg.CORBA.ARG_OUT.value),
              "last item flags unchanged after earlier remove");
        nl.remove(2); // drop the tail
        check(nl.count() == 2, "tail remove decrements count");
        check("third".equals(nl.item(1).name()), "head items untouched by tail remove");
        nl.remove(0);
        nl.remove(0);
        check(nl.count() == 0, "list drains to zero");
        try {
            nl.item(0);
            throw new AssertionError("item(0) on drained list should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "drained list item(0) -> Bounds");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
