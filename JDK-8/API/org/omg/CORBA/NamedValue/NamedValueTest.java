/*
 * @test
 * @summary org.omg.CORBA.NamedValue conformance: abstract class shape.
 * @run main NamedValueTest
 */

public class NamedValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.NamedValue");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Any any = orb.create_any();
        any.insert_long(42);

        // ---- full-parameter readback ----
        org.omg.CORBA.NamedValue nv = orb.create_named_value("name1", any, org.omg.CORBA.ARG_IN.value);
        check("name1".equals(nv.name()), "name readback");
        check(nv.value() == any, "value() keeps the same Any instance");
        check(nv.value().extract_long() == 42, "Any content still readable through NamedValue");
        check(nv.flags() == org.omg.CORBA.ARG_IN.value, "flags ARG_IN readback");

        org.omg.CORBA.NamedValue nvOut = orb.create_named_value("o", any, org.omg.CORBA.ARG_OUT.value);
        check(nvOut.flags() == org.omg.CORBA.ARG_OUT.value, "flags ARG_OUT readback");
        org.omg.CORBA.NamedValue nvInOut = orb.create_named_value("io", any, org.omg.CORBA.ARG_INOUT.value);
        check(nvInOut.flags() == org.omg.CORBA.ARG_INOUT.value, "flags ARG_INOUT readback");
        org.omg.CORBA.NamedValue nvComb = orb.create_named_value("c", any,
                org.omg.CORBA.ARG_IN.value | org.omg.CORBA.ARG_OUT.value);
        check(nvComb.flags() == 3, "combined ARG_IN|ARG_OUT flags readback (1|2==3)");
        org.omg.CORBA.NamedValue nv0 = orb.create_named_value("z", any, 0);
        check(nv0.flags() == 0, "zero flags accepted");

        // ---- null name / null value semantics (jdk8u372 measured) ----
        org.omg.CORBA.NamedValue nvn = orb.create_named_value(null, null, 0);
        check(nvn.name() == null, "null name passes through");
        check(nvn.value() == null, "null value passes through");
        check(nvn.flags() == 0, "flags still 0");

        // distinct factories are independent objects
        org.omg.CORBA.NamedValue other = orb.create_named_value("name1", any, org.omg.CORBA.ARG_IN.value);
        check(other != nv, "two create_named_value calls yield distinct NamedValue instances");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
