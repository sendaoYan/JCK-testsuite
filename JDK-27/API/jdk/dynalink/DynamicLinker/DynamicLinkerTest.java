/*
 * @test
 * @summary jdk.dynalink.DynamicLinker conformance: white-box structure + black-box behavior.
 * @run main DynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class DynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.DynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "link"), "link");
        check(hasMethod(c, "getLinkerServices"), "getLinkerServices");

    }

    static void testBlackBox() throws Exception {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        DynamicLinker dl = f.createLinker();
        check(dl != null, "linker");
        check(dl.getLinkerServices() != null, "linker services");
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        check(dl.link(cs) == cs, "link returns site");
        check(dl.getLinkerServices().canConvert(String.class, Object.class), "canConvert");

    }


    // ---- shared fixtures for the deep contract sections ----
    public static class POJO {
        public int field = 7;
        public String getName() { return "name-" + field; }
        public String greet(String who) { return "hi " + who; }
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

    static void testRealLinkLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinker linker = beanLinker();
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite ret = linker.link(cs);
        check(ret == cs, "link returns the same call site");
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "GET:PROPERTY:field runs through the linker");
        // link(null) rejects
        try { linker.link(null); check(false, "link(null) must throw"); }
        catch (NullPointerException expected) { check(true, "link(null) NPE"); }
    }

    static void testUnlinkableSite() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinker linker = beanLinker();
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("nope"),
            MethodType.methodType(Object.class, Object.class));
        // an unlinked call site refuses invocation with the uninitialized stub
        try {
            cs.getTarget().invoke((Object) new POJO());
            check(false, "unlinked site must refuse invocation");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().equals("uninitialized call site"), "unlinked site message: " + expected.getMessage());
        }
        linker.link(cs);
        try {
            cs.getTarget().invoke((Object) new POJO());
            check(false, "unknown member must throw NoSuchDynamicMethodException");
        } catch (NoSuchDynamicMethodException expected) {
            check(expected.getMessage().startsWith("GET:PROPERTY:nope(Object)Object@"),
                  "NSDME message is the call site descriptor: " + expected.getMessage());
        }
    }

    static void testLinkerServicesLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinker linker = beanLinker();
        LinkerServices svc = linker.getLinkerServices();
        // a direct relink through the services (no call site involved)
        SimpleLinkRequest req = new SimpleLinkRequest(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class)), false, new POJO());
        GuardedInvocation gi = svc.getGuardedInvocation(req);
        check(gi != null, "services link a real request");
        check(gi.getInvocation().invoke((Object) new POJO()).equals(7), "services-produced invocation runs");
        // getWithLookup runs the supplier under the descriptor lookup
        Object res = svc.getWithLookup(() -> "supplied",
                new CallSiteDescriptor(lk, StandardOperation.GET, MethodType.methodType(Object.class)));
        check(res.equals("supplied"), "getWithLookup returns the supplier value");
    }

    static void testLinkedCallSiteLocation() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<StackTraceElement> seen = new ArrayList<>();
        GuardingDynamicLinker probe = (req, svc) -> {
            seen.add(DynamicLinker.getLinkedCallSiteLocation());
            return null;
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(probe);
        f.setFallbackLinkers();
        SimpleRelinkableCallSite cs = site(lk, StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        f.createLinker().link(cs);
        try { cs.getTarget().invoke(new Object()); } catch (NoSuchDynamicMethodException expected) { }
        check(seen.size() == 1, "probe linker invoked exactly once");
        StackTraceElement ste = seen.get(0);
        check(ste != null, "getLinkedCallSiteLocation is non-null during linking");
        check(ste.getClassName().equals(DynamicLinkerTest.class.getName()),
              "location names the caller class: " + ste);
        check(ste.getMethodName().equals("testLinkedCallSiteLocation"), "location names the caller method: " + ste);
        check(DynamicLinker.getLinkedCallSiteLocation() == null,
              "getLinkedCallSiteLocation is null outside a linking pass");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testRealLinkLoop();
        testUnlinkableSite();
        testLinkerServicesLoop();
        testLinkedCallSiteLocation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
