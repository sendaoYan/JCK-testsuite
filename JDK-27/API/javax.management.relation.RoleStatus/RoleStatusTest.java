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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
