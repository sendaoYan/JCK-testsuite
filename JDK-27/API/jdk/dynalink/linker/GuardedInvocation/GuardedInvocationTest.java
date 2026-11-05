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


    static void testAccessorsAndLifecycle() throws Exception {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle inv = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                .asType(MethodType.methodType(int.class, Object.class));
        MethodHandle guard = Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class));
        // guard-less and switch-point constructors
        GuardedInvocation plain = new GuardedInvocation(inv);
        check(plain.getInvocation() == inv && plain.getGuard() == null, "bare invocation");
        check(plain.getSwitchPoints() == null && plain.getException() == null, "no switchpoints, no exception");
        check(!plain.hasBeenInvalidated(), "plain is never invalidated");
        GuardedInvocation g = new GuardedInvocation(inv, guard);
        check(g.getGuard() == guard, "guard kept");
        // switchpoint lifecycle
        SwitchPoint sp = new SwitchPoint();
        GuardedInvocation spg = new GuardedInvocation(inv, guard, sp);
        check(spg.getSwitchPoints().length == 1 && spg.getSwitchPoints()[0] == sp, "switchpoint kept");
        check(!spg.hasBeenInvalidated(), "fresh switchpoint not invalidated");
        SwitchPoint.invalidateAll(new SwitchPoint[]{ sp });
        check(spg.hasBeenInvalidated(), "invalidated switchpoint reported");
        // multiple switchpoints, several constructors
        SwitchPoint sp2 = new SwitchPoint();
        GuardedInvocation two = new GuardedInvocation(inv, guard, new SwitchPoint[]{ sp, sp2 }, IllegalStateException.class);
        check(two.getSwitchPoints().length == 2, "both switchpoints kept");
        check(two.getException() == IllegalStateException.class, "exception class kept");
        check(two.hasBeenInvalidated(), "one invalidated switchpoint poisons the invocation");
    }

    static void testReplaceMethodsAndSwitchPoints() throws Exception {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle inv = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                .asType(MethodType.methodType(int.class, Object.class));
        MethodHandle guard = Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class));
        SwitchPoint sp = new SwitchPoint();
        GuardedInvocation g = new GuardedInvocation(inv, guard, sp, IllegalStateException.class);
        GuardedInvocation r = g.replaceMethods(inv, guard);
        check(r != g, "replaceMethods returns a new instance");
        check(r.getSwitchPoints() != null && r.getSwitchPoints()[0] == sp, "replaceMethods keeps switchpoints");
        check(r.getException() == IllegalStateException.class, "replaceMethods keeps the exception");
        GuardedInvocation added = g.addSwitchPoint(new SwitchPoint());
        check(added.getSwitchPoints().length == 2, "addSwitchPoint appends");
        check(g.getSwitchPoints().length == 1, "addSwitchPoint immutable on original");
        check(g.addSwitchPoint(null) == g, "addSwitchPoint(null) returns this");
    }

    static void testAdaptations() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle inv = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                .asType(MethodType.methodType(int.class, Object.class));
        MethodHandle guard = Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class));
        GuardedInvocation g = new GuardedInvocation(inv, guard);
        check(g.asType(MethodType.methodType(Number.class, Object.class)).getInvocation().type()
              .equals(MethodType.methodType(Number.class, Object.class)), "asType adapts return");
        check(g.filterArguments(0, MethodHandles.identity(Object.class)).getInvocation().type()
              .equals(MethodType.methodType(int.class, Object.class)), "filterArguments keeps type");
        check(g.dropArguments(1, String.class).getInvocation().type()
              .equals(MethodType.methodType(int.class, Object.class, String.class)), "dropArguments extends type");
        check(g.dropArguments(1, List.of(String.class)).getInvocation().type()
              .equals(MethodType.methodType(int.class, Object.class, String.class)), "dropArguments list variant");
    }

    static void testCompose() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle inv = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                .asType(MethodType.methodType(int.class, Object.class));
        MethodHandle guard = Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class));
        MethodHandle fallback = MethodHandles.constant(int.class, -1);
        fallback = MethodHandles.dropArguments(fallback, 0, Object.class);
        GuardedInvocation g = new GuardedInvocation(inv, guard);
        MethodHandle composed = g.compose(fallback);
        check((Integer) composed.invoke((Object) "abcd") == 4, "compose guarded path");
        check((Integer) composed.invoke((Object) Integer.valueOf(5)) == -1, "compose fallback path");
        // type mismatches are rejected with exact messages
        try {
            g.compose(MethodHandles.dropArguments(MethodHandles.constant(int.class, -1), 0, String.class));
            check(false, "fallback type mismatch must throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().startsWith("target and fallback types must match"),
                  "fallback mismatch message: " + expected.getMessage());
        }
        try {
            new GuardedInvocation(inv, Guards.isOfClass(String.class, MethodType.methodType(boolean.class, String.class)))
                .compose(fallback);
            check(false, "guard type mismatch must throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().startsWith("target and test types must match"),
                  "guard mismatch message: " + expected.getMessage());
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testAccessorsAndLifecycle();
        testReplaceMethodsAndSwitchPoints();
        testAdaptations();
        testCompose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
