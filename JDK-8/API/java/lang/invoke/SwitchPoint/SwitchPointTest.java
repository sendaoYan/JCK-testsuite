/*
 * @test
 * @summary java.lang.invoke.SwitchPoint conformance: invalidation and guard.
 * @run main SwitchPointTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SwitchPoint;

public class SwitchPointTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSwitchPoint() throws Throwable {
        SwitchPoint sp = new SwitchPoint();
        check(!sp.hasBeenInvalidated(), "initially valid");
        MethodHandle target = MethodHandles.constant(String.class, "target");
        MethodHandle fallback = MethodHandles.constant(String.class, "fallback");
        MethodHandle guard = sp.guardWithTest(target, fallback);
        check("target".equals((String) guard.invoke()), "guard picks target while valid");
        SwitchPoint.invalidateAll(new SwitchPoint[] {sp});
        check(sp.hasBeenInvalidated(), "invalidated");
        check("fallback".equals((String) guard.invoke()), "guard picks fallback after invalidation");
    }

    public static void main(String[] args) throws Throwable {
        testSwitchPoint();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
