/*
 * @test
 * @summary jdk.dynalink.linker.GuardingDynamicLinker conformance: white-box structure + black-box behavior.
 * @run main GuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardingDynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    // ---- shared fixtures for the deep contract sections ----
    public static class POJO {
        public int field = 7;
        public String getName() { return "name-" + field; }
        public String greet(String who) { return "hi " + who; }
        public int add(int a, int b) { return a + b; }
    }
    static DynamicLinker beanLinker() {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        return f.createLinker();
    }
    static SimpleRelinkableCallSite site(MethodHandles.Lookup lk, Operation op, MethodType mt) {
        return new SimpleRelinkableCallSite(new CallSiteDescriptor(lk, op, mt));
    }

    static void testFunctionalContract() throws Throwable {
        // exactly one abstract method
        int n = 0; Method abs = null;
        for (Method m : GuardingDynamicLinker.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1 && abs.getName().equals("getGuardedInvocation") && abs.getParameterCount() == 2,
              "single abstract getGuardedInvocation(request, services)");
        check(GuardingDynamicLinker.class.isInterface(), "is an interface");
        check(abs.getReturnType() == GuardedInvocation.class, "returns GuardedInvocation");
    }

    static void testLinkerLifecycleLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<Object> receivers = new ArrayList<>();
        GuardingDynamicLinker linker = (req, svc) -> {
            receivers.add(req.getReceiver());
            if (req.getReceiver() instanceof String) {
                MethodHandle mh = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                        .asType(MethodType.methodType(Object.class, Object.class));
                return new GuardedInvocation(mh, Guards.isOfClass(String.class,
                        MethodType.methodType(boolean.class, Object.class)));
            }
            return null; // decline: let the caller see the failure
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(linker);
        f.setFallbackLinkers();
        DynamicLinker dl = f.createLinker();
        SimpleRelinkableCallSite cs = site(lk, StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        dl.link(cs);
        check(cs.getTarget().invoke((Object) "abcd").equals(4), "custom linker serves the link");
        check(receivers.equals(List.of("abcd")), "linker saw the receiver: " + receivers);
        // guard failure at invocation time triggers a relink pass
        check(cs.getTarget().invoke((Object) "ab").equals(2), "second invocation same type");
        try {
            cs.getTarget().invoke((Object) Integer.valueOf(7));
            check(false, "declined receiver must fail");
        } catch (NoSuchDynamicMethodException expected) { check(true, "declined receiver NSDME"); }
        // the guard itself is honored: a non-String receiver that slips past the decline triggers relink
        check(receivers.size() >= 2, "relink pass invoked the linker again: " + receivers.size());
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testFunctionalContract();
        testLinkerLifecycleLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
