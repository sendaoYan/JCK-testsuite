/*
 * @test
 * @summary org.omg.CORBA.SetOverrideType conformance: enumeration values.
 * @run main SetOverrideTypeTest
 */

public class SetOverrideTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.SetOverrideType");
        check(!c.isInterface(), "is a class");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements IDLEntity");

        // ---- two constants SET_OVERRIDE/ADD_OVERRIDE {0,1} ----
        check(org.omg.CORBA.SetOverrideType.SET_OVERRIDE.value() == 0, "SET_OVERRIDE == 0");
        check(org.omg.CORBA.SetOverrideType.ADD_OVERRIDE.value() == 1, "ADD_OVERRIDE == 1");
        check(org.omg.CORBA.SetOverrideType.SET_OVERRIDE != org.omg.CORBA.SetOverrideType.ADD_OVERRIDE,
              "constants distinct");

        // ---- from_int normalization ----
        check(org.omg.CORBA.SetOverrideType.from_int(0) == org.omg.CORBA.SetOverrideType.SET_OVERRIDE,
              "from_int(0) == SET_OVERRIDE");
        check(org.omg.CORBA.SetOverrideType.from_int(1) == org.omg.CORBA.SetOverrideType.ADD_OVERRIDE,
              "from_int(1) == ADD_OVERRIDE");
        org.omg.CORBA.SetOverrideType[] all = {
            org.omg.CORBA.SetOverrideType.SET_OVERRIDE, org.omg.CORBA.SetOverrideType.ADD_OVERRIDE };
        for (org.omg.CORBA.SetOverrideType s : all) {
            check(org.omg.CORBA.SetOverrideType.from_int(s.value()) == s,
                  "from_int(" + s.value() + ") normalizes to same constant");
        }

        // ---- out-of-range -> BAD_PARAM ----
        try {
            org.omg.CORBA.SetOverrideType.from_int(5);
            throw new AssertionError("from_int(5) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(5) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.SetOverrideType.from_int(2);
            throw new AssertionError("from_int(2) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(2) -> BAD_PARAM");
        }
        try {
            org.omg.CORBA.SetOverrideType.from_int(-1);
            throw new AssertionError("from_int(-1) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> BAD_PARAM");
        }

        // jdk8u372: toString is default Object form
        check(org.omg.CORBA.SetOverrideType.SET_OVERRIDE.toString().startsWith("org.omg.CORBA.SetOverrideType@"),
              "toString is default Object form");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
