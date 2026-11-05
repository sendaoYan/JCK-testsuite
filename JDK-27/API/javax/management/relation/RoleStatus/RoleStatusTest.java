/*
 * @test
 * @summary javax.management.relation.RoleStatus conformance: white-box structure + black-box behavior.
 * @run main RoleStatusTest
 */
import javax.management.relation.RoleStatus;

public class RoleStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleStatus");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleStatus must be public");
        check(!c.isInterface(), "RoleStatus must be a class");
        check(c.getConstructor() != null, "RoleStatus has a (deprecated) no-arg constructor");
        c.getField("NO_ROLE_WITH_NAME"); c.getField("ROLE_NOT_READABLE");
        c.getField("ROLE_NOT_WRITABLE"); c.getField("LESS_THAN_MIN_ROLE_DEGREE");
        c.getField("MORE_THAN_MAX_ROLE_DEGREE"); c.getField("REF_MBEAN_OF_INCORRECT_CLASS");
        c.getField("REF_MBEAN_NOT_REGISTERED");
        c.getMethod("isRoleStatus", int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(RoleStatus.NO_ROLE_WITH_NAME == 1, "NO_ROLE_WITH_NAME == 1");
        check(RoleStatus.ROLE_NOT_READABLE == 2, "ROLE_NOT_READABLE == 2");
        check(RoleStatus.ROLE_NOT_WRITABLE == 3, "ROLE_NOT_WRITABLE == 3");
        check(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE == 4, "LESS_THAN_MIN_ROLE_DEGREE == 4");
        check(RoleStatus.MORE_THAN_MAX_ROLE_DEGREE == 5, "MORE_THAN_MAX_ROLE_DEGREE == 5");
        check(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS == 6, "REF_MBEAN_OF_INCORRECT_CLASS == 6");
        check(RoleStatus.REF_MBEAN_NOT_REGISTERED == 7, "REF_MBEAN_NOT_REGISTERED == 7");

        check(!RoleStatus.isRoleStatus(0), "0 is not a role status");
        for (int i = 1; i <= 7; i++) {
            check(RoleStatus.isRoleStatus(i), "isRoleStatus(" + i + ") must be true");
        }
        check(!RoleStatus.isRoleStatus(8), "8 is not a role status");
        check(!RoleStatus.isRoleStatus(-1), "-1 is not a role status");
    }

    // ---- deepened (batch j): constant distinctness, class shape, domain integration ----

    static void testConstantDistinctness() {
        int[] vals = { RoleStatus.NO_ROLE_WITH_NAME, RoleStatus.ROLE_NOT_READABLE,
                       RoleStatus.ROLE_NOT_WRITABLE, RoleStatus.LESS_THAN_MIN_ROLE_DEGREE,
                       RoleStatus.MORE_THAN_MAX_ROLE_DEGREE, RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS,
                       RoleStatus.REF_MBEAN_NOT_REGISTERED };
        for (int i = 0; i < vals.length; i++)
            for (int j = i + 1; j < vals.length; j++)
                check(vals[i] != vals[j], "constants " + i + " and " + j + " must be distinct");
        // extended isRoleStatus edges around the [1,7] window
        check(!RoleStatus.isRoleStatus(-2), "-2 is not a role status");
        check(!RoleStatus.isRoleStatus(9), "9 is not a role status");
        check(!RoleStatus.isRoleStatus(Integer.MIN_VALUE), "Integer.MIN_VALUE is not a role status");
        check(!RoleStatus.isRoleStatus(Integer.MAX_VALUE), "Integer.MAX_VALUE is not a role status");
    }

    @SuppressWarnings({"deprecation", "removal"}) // RoleStatus() is deprecated for removal since JDK 16
    static void testClassShapeAndUsage() {
        check(!java.lang.reflect.Modifier.isAbstract(RoleStatus.class.getModifiers()),
              "RoleStatus is a concrete utility class");
        check(new RoleStatus() != null, "deprecated no-arg constructor is instantiable");
        check(!java.io.Serializable.class.isAssignableFrom(RoleStatus.class),
              "RoleStatus is not Serializable (utility class, no state)");
        // integration: the constants form the closed problem-type domain of RoleUnresolved
        for (int t = 1; t <= 7; t++) {
            check(new javax.management.relation.RoleUnresolved("n", null, t).getProblemType() == t,
                  "constant " + t + " usable as a RoleUnresolved problem type");
        }
        boolean threw = false;
        try { new javax.management.relation.RoleUnresolved("n", null, 8); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "constants are a closed domain: 8 is rejected as a problem type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDistinctness();
        testClassShapeAndUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
