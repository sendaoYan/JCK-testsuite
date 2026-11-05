/*
 * @test
 * @summary jdk.dynalink.DynamicLinkerFactory conformance: white-box structure + black-box behavior.
 * @run main DynamicLinkerFactoryTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class DynamicLinkerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.DynamicLinkerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "createLinker"), "createLinker");
        check(hasMethod(c, "getAutoLoadingErrors"), "getAutoLoadingErrors");
        check(hasMethod(c, "setSyncOnRelink"), "setSyncOnRelink");
        check(hasMethod(c, "setUnstableRelinkThreshold"), "setUnstableRelinkThreshold");

    }

    static void testBlackBox() throws Exception {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setSyncOnRelink(true);
        f.setUnstableRelinkThreshold(4);
        f.setPrioritizedLinkers(new BeansLinker());
        check(f.createLinker() != null, "createLinker");
        check(f.getAutoLoadingErrors() != null, "auto loading errors");
        check(f.createLinker().getLinkerServices() != null, "second linker services");

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

    static void testDefaultFallbackIsBeansLinker() throws Throwable {
        // no explicit configuration: the factory still links beans through its default fallback
        DynamicLinker linker = new DynamicLinkerFactory().createLinker();
        SimpleRelinkableCallSite cs = site(MethodHandles.lookup(),
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("out"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cs);
        check(cs.getTarget().invoke(jdk.dynalink.beans.StaticClass.forClass(System.class)) != null,
              "default fallback linker resolves a static property");
    }

    static void testPriorityAndFallbackOrder() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<String> order = new ArrayList<>();
        GuardingDynamicLinker pri = (req, svc) -> { order.add("pri"); return null; };
        GuardingDynamicLinker ok = (req, svc) -> {
            order.add("fb");
            MethodHandle c = MethodHandles.constant(Object.class, "FB");
            return new GuardedInvocation(MethodHandles.dropArguments(c, 0, Object.class), (MethodHandle) null);
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(pri);
        f.setFallbackLinkers(ok);
        SimpleRelinkableCallSite cs = site(lk, StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        f.createLinker().link(cs);
        check(cs.getTarget().invoke(new Object()).equals("FB"), "fallback linker result wins");
        check(order.equals(List.of("pri", "fb")), "prioritized asked first then fallback: " + order);
        // all linkers decline -> NoSuchDynamicMethodException
        DynamicLinkerFactory f2 = new DynamicLinkerFactory();
        f2.setPrioritizedLinkers(pri);
        f2.setFallbackLinkers();
        SimpleRelinkableCallSite cs2 = site(lk, StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        f2.createLinker().link(cs2);
        try {
            cs2.getTarget().invoke(new Object());
            check(false, "all-declining chain must throw NSDME");
        } catch (NoSuchDynamicMethodException expected) { check(true, "all-declining chain NSDME"); }
    }

    static void testPrelinkTransformer() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final int[] count = {0};
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        f.setPrelinkTransformer((gi, req, svc) -> { count[0]++; return gi; });
        DynamicLinker linker = f.createLinker();
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cs);
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "prelink-transformed link still works");
        check(count[0] == 1, "prelink transformer ran exactly once: " + count[0]);
    }

    static void testAutoConversionStrategy() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<String> transitions = new ArrayList<>();
        MethodTypeConversionStrategy strategy = (target, newType) -> {
            transitions.add(target.type() + " -> " + newType);
            return target.asType(newType);
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        f.setAutoConversionStrategy(strategy);
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        f.createLinker().link(cs);
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "auto-conversion link works");
        check(transitions.size() >= 2, "strategy applied to invocation and guard: " + transitions.size());
        check(transitions.get(0).endsWith("(Object)Object"), "first adaptation targets the site type: " + transitions.get(0));
    }

    static void testInternalObjectsFilter() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final int[] applied = {0};
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        f.setInternalObjectsFilter(new MethodHandleTransformer() {
            public MethodHandle transform(MethodHandle target) { applied[0]++; return target; }
        });
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        f.createLinker().link(cs);
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "filtered link works");
        check(applied[0] >= 1, "internal objects filter applied at least once");
    }

    static void testUnstableRelinkAndSync() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final int[] prelink = {0};
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        f.setPrelinkTransformer((gi, req, svc) -> { prelink[0]++; return gi; });
        f.setUnstableRelinkThreshold(1);
        f.setSyncOnRelink(true);
        DynamicLinker linker = f.createLinker();
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cs);
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "unstable-threshold link works");
        // a second, unlinkable receiver relinks and fails; the prelink pass only counts successful links
        try { cs.getTarget().invoke((Object) Integer.valueOf(1)); } catch (NoSuchDynamicMethodException expected) { }
        check(prelink[0] == 1, "failed relink does not pass the prelink transformer: " + prelink[0]);
        // createLinker can produce independent linkers from one factory configuration
        DynamicLinkerFactory f2 = new DynamicLinkerFactory();
        check(f2.createLinker() != f2.createLinker(), "createLinker returns fresh linkers");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDefaultFallbackIsBeansLinker();
        testPriorityAndFallbackOrder();
        testPrelinkTransformer();
        testAutoConversionStrategy();
        testInternalObjectsFilter();
        testUnstableRelinkAndSync();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
