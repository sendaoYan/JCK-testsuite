/*
 * @test
 * @summary jdk.dynalink.beans.MissingMemberHandlerFactory conformance: white-box structure + black-box behavior.
 * @run main MissingMemberHandlerFactoryTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class MissingMemberHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.MissingMemberHandlerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "createMissingMemberHandler"), "createMissingMemberHandler");

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

    static void testFunctionalSurface() throws Exception {
        // exactly one abstract method
        Method[] am = { null };
        int n = 0;
        for (Method m : MissingMemberHandlerFactory.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { am[0] = m; n++; }
        check(n == 1, "single abstract method");
        check(am[0].getName().equals("createMissingMemberHandler"), "method name");
        check(am[0].getParameterCount() == 2, "two parameters: LinkRequest, LinkerServices");
        check(Modifier.isInterface(MissingMemberHandlerFactory.class.getModifiers()), "interface");
        check(MissingMemberHandlerFactory.class.getCanonicalName().equals("jdk.dynalink.beans.MissingMemberHandlerFactory"),
              "lives in jdk.dynalink.beans");
    }

    static void testMissingMemberLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<Object> requested = new ArrayList<>();
        MissingMemberHandlerFactory factory = (req, svc) -> {
            requested.add(NamedOperation.getName(req.getCallSiteDescriptor().getOperation()));
            MethodType mt = req.getCallSiteDescriptor().getMethodType();
            MethodHandle c = MethodHandles.constant(Object.class, "MISSING");
            return MethodHandles.dropArguments(c, 0, mt.parameterList());
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker(factory));
        f.setFallbackLinkers();
        DynamicLinker linker = f.createLinker();
        SimpleRelinkableCallSite cs = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("nope"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cs);
        check(cs.getTarget().invoke((Object) new POJO()).equals("MISSING"), "missing member GET returns handler value");
        check(requested.equals(List.of("nope")), "handler saw the missing name: " + requested);
        // without the factory the same site is unlinkable
        SimpleRelinkableCallSite plain = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("nope"),
            MethodType.methodType(Object.class, Object.class));
        beanLinker().link(plain);
        try {
            plain.getTarget().invoke((Object) new POJO());
            check(false, "no factory -> NSDME expected");
        } catch (NoSuchDynamicMethodException expected) { check(true, "no factory -> NSDME"); }
        // a factory returning null falls back to unlinkable as well
        DynamicLinkerFactory f2 = new DynamicLinkerFactory();
        f2.setPrioritizedLinker(new BeansLinker((req, svc) -> null));
        f2.setFallbackLinkers();
        SimpleRelinkableCallSite cs2 = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("nope"),
            MethodType.methodType(Object.class, Object.class));
        f2.createLinker().link(cs2);
        try {
            cs2.getTarget().invoke((Object) new POJO());
            check(false, "null handler -> NSDME expected");
        } catch (NoSuchDynamicMethodException expected) { check(true, "null handler -> NSDME"); }
        // present members still link normally with a factory installed
        SimpleRelinkableCallSite ok = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(ok);
        check(ok.getTarget().invoke((Object) new POJO()).equals(7), "present member unaffected by factory");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testFunctionalSurface();
        testMissingMemberLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
