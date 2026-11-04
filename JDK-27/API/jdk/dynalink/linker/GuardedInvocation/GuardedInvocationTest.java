/*
 * @test
 * @summary jdk.dynalink.linker.GuardedInvocation conformance: white-box structure + black-box behavior.
 * @run main GuardedInvocationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardedInvocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardedInvocation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getInvocation"), "getInvocation");
        check(hasMethod(c, "getGuard"), "getGuard");
        check(hasMethod(c, "getSwitchPoints"), "getSwitchPoints");
        check(hasMethod(c, "hasBeenInvalidated"), "hasBeenInvalidated");
        check(hasMethod(c, "asType"), "asType");
        check(hasMethod(c, "compose"), "compose");
        check(hasMethod(c, "replaceMethods"), "replaceMethods");
        check(hasMethod(c, "addSwitchPoint"), "addSwitchPoint");

    }

    static void testBlackBox() throws Exception {
        MethodHandle mh = MethodHandles.identity(String.class);
        GuardedInvocation gi = new GuardedInvocation(mh);
        check(gi.getInvocation() == mh, "invocation");
        check(gi.getGuard() == null, "no guard");
        check(gi.getSwitchPoints() == null, "no switch points");
        check(!gi.hasBeenInvalidated(), "not invalidated");
        check(gi.asType(MethodType.methodType(String.class, String.class)) != null, "asType");
        check(gi.compose(mh) != null, "compose");
        GuardedInvocation gi2 = new GuardedInvocation(mh, mh);
        check(gi2.getGuard() == mh, "guard");
        check(gi2.replaceMethods(mh, null).getInvocation() == mh, "replaceMethods");
        SwitchPoint sp = new SwitchPoint();
        check(gi.addSwitchPoint(sp).getSwitchPoints().length == 1, "addSwitchPoint");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
