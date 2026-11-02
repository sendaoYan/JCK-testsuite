/*
 * @test
 * @summary jdk.dynalink.linker.GuardedInvocation conformance: construct and inspect guarded invocation.
 * @run main GuardedInvocationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class GuardedInvocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MethodHandle mh = MethodHandles.identity(String.class);
        GuardedInvocation gi = new GuardedInvocation(mh);
        check(gi.getInvocation() == mh, "invocation");
        check(gi.getGuard() == null, "no guard");
        check(gi.getSwitchPoints() == null, "no switch points");
        check(!gi.hasBeenInvalidated(), "not invalidated");
        check(gi.asType(MethodType.methodType(String.class, String.class)) != null, "asType");
        check(gi.compose(mh) != null, "compose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
