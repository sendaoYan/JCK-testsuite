/*
 * @test
 * @summary javax.management.relation.RoleStatus conformance: isRoleStatus.
 * @run main RoleStatusTest
 */

import javax.management.relation.*;
public class RoleStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(RoleStatus.isRoleStatus(RoleStatus.NO_ROLE_WITH_NAME), "valid status");
        check(RoleStatus.isRoleStatus(RoleStatus.REF_MBEAN_NOT_REGISTERED), "valid status 2");
        check(!RoleStatus.isRoleStatus(99999), "invalid status");
        testConstantValueTable();
        testIsRoleStatusMatrix();
        testClassShapeAndUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch j): full contract coverage ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // locked constant value table (verified identical on JDK-8 and JDK-27)
    static void testConstantValueTable() {
        check(RoleStatus.NO_ROLE_WITH_NAME == 1, "NO_ROLE_WITH_NAME == 1");
        check(RoleStatus.ROLE_NOT_READABLE == 2, "ROLE_NOT_READABLE == 2");
        check(RoleStatus.ROLE_NOT_WRITABLE == 3, "ROLE_NOT_WRITABLE == 3");
        check(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE == 4, "LESS_THAN_MIN_ROLE_DEGREE == 4");
        check(RoleStatus.MORE_THAN_MAX_ROLE_DEGREE == 5, "MORE_THAN_MAX_ROLE_DEGREE == 5");
        check(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS == 6, "REF_MBEAN_OF_INCORRECT_CLASS == 6");
        check(RoleStatus.REF_MBEAN_NOT_REGISTERED == 7, "REF_MBEAN_NOT_REGISTERED == 7");
        // all seven constants are pairwise distinct
        int[] vals = { RoleStatus.NO_ROLE_WITH_NAME, RoleStatus.ROLE_NOT_READABLE,
                       RoleStatus.ROLE_NOT_WRITABLE, RoleStatus.LESS_THAN_MIN_ROLE_DEGREE,
                       RoleStatus.MORE_THAN_MAX_ROLE_DEGREE, RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS,
                       RoleStatus.REF_MBEAN_NOT_REGISTERED };
        for (int i = 0; i < vals.length; i++)
            for (int j = i + 1; j < vals.length; j++)
                check(vals[i] != vals[j], "constants " + i + " and " + j + " must be distinct");
    }

    // isRoleStatus accepts exactly the seven constants; rejects 0, negatives, and anything above 7
    static void testIsRoleStatusMatrix() {
        for (int i = 1; i <= 7; i++) {
            check(RoleStatus.isRoleStatus(i), "isRoleStatus(" + i + ") must be true");
        }
        int[] bad = { 0, 8, 9, -1, -2, 100, Integer.MIN_VALUE, Integer.MAX_VALUE };
        for (int v : bad) {
            check(!RoleStatus.isRoleStatus(v), "isRoleStatus(" + v + ") must be false");
        }
    }

    // class shape: concrete, instantiable, deliberately NOT Serializable;
    // every constant is directly usable as a RoleUnresolved problem type
    static void testClassShapeAndUsage() {
        check(!java.lang.reflect.Modifier.isAbstract(RoleStatus.class.getModifiers()),
              "RoleStatus is a concrete utility class");
        check(new RoleStatus() != null, "deprecated no-arg constructor is instantiable");
        check(!java.io.Serializable.class.isAssignableFrom(RoleStatus.class),
              "RoleStatus is not Serializable (utility class, no state)");
        // integration: the constants form the closed problem-type domain of RoleUnresolved
        for (int t = 1; t <= 7; t++) {
            RoleUnresolved ru = new RoleUnresolved("n", null, t);
            check(ru.getProblemType() == t, "constant " + t + " usable as a RoleUnresolved problem type");
        }
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new RoleUnresolved("n", null, 8); }
        }, "constants are a closed domain: 8 is rejected as a problem type");
    }
}
