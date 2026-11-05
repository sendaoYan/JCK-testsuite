/*
 * @test
 * @summary org.omg.CORBA.LocalObject conformance: concrete class behavior.
 * @run main LocalObjectTest
 */

public class LocalObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void noImplement(String what, Runnable r) {
        try {
            r.run();
            throw new AssertionError(what + " should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(true, what + " -> NO_IMPLEMENT('This is a locally constrained object.')");
        }
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LocalObject lo = new org.omg.CORBA.LocalObject();
        check(lo instanceof org.omg.CORBA.Object, "LocalObject implements org.omg.CORBA.Object");
        check(!java.lang.reflect.Modifier.isAbstract(org.omg.CORBA.LocalObject.class.getModifiers()),
              "LocalObject is concrete, directly instantiable");

        // ---- methods that behave normally ----
        check(lo.toString() != null, "toString works (default Object form)");
        check(lo.toString().startsWith("org.omg.CORBA.LocalObject"), "toString prefix");
        check(lo.equals(lo), "equals self true");
        check(!lo.equals(new org.omg.CORBA.LocalObject()), "equals different instance false");
        check(lo.hashCode() == lo.hashCode(), "hashCode stable");
        check(lo._non_existent() == false, "_non_existent() returns false (not NO_IMPLEMENT)");
        check(!lo._is_equivalent(null), "_is_equivalent(null) returns false");
        check(lo._is_equivalent(lo), "_is_equivalent(self) returns true");
        check(lo._hash(0) == lo.hashCode(), "_hash(0) equals hashCode");
        check(lo._hash(Integer.MAX_VALUE) == lo.hashCode(), "_hash(max) equals hashCode");

        // ---- methods throwing NO_IMPLEMENT "This is a locally constrained object." ----
        noImplement("_is_a", () -> lo._is_a("IDL:X:1.0"));
        noImplement("_duplicate", () -> lo._duplicate());
        noImplement("_release", () -> lo._release());
        noImplement("_get_domain_managers", () -> lo._get_domain_managers());
        noImplement("_get_interface_def", () -> lo._get_interface_def());
        noImplement("_get_interface", () -> lo._get_interface());
        noImplement("_get_policy(1)", () -> lo._get_policy(1));
        noImplement("_request(op)", () -> lo._request("op"));
        noImplement("_request(op,resp)", () -> lo._request("op", true));
        noImplement("_create_request(4)", () -> lo._create_request(null, "op", null, null));
        noImplement("_create_request(6)", () -> lo._create_request(null, "op", null, null, null, null));
        noImplement("_set_policy_override", () -> lo._set_policy_override(null,
                org.omg.CORBA.SetOverrideType.SET_OVERRIDE));
        noImplement("_orb", () -> lo._orb());
        noImplement("_is_local", () -> lo._is_local());
        noImplement("_servant_preinvoke", () -> lo._servant_preinvoke("op", java.lang.Object.class));
        noImplement("_servant_postinvoke", () -> lo._servant_postinvoke(null));
        noImplement("_invoke", () -> {
            try { lo._invoke(null); }
            catch (org.omg.CORBA.portable.ApplicationException impossible) { }
            catch (org.omg.CORBA.portable.RemarshalException impossible) { }
        });
        noImplement("_releaseReply", () -> lo._releaseReply(null));
        noImplement("validate_connection", () -> lo.validate_connection());

        try {
            lo._is_a("IDL:X:1.0");
        } catch (org.omg.CORBA.NO_IMPLEMENT ni) {
            check("This is a locally constrained object.".equals(ni.getMessage()),
                  "NO_IMPLEMENT carries exact message 'This is a locally constrained object.'");
            check(ni.minor == 0 && ni.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                  "LocalObject NO_IMPLEMENT minor 0 / COMPLETED_NO");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
