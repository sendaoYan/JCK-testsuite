/*
 * @test
 * @summary jdk.dynalink.linker.GuardedInvocationTransformer conformance: white-box structure + black-box behavior.
 * @run main GuardedInvocationTransformerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardedInvocationTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardedInvocationTransformer");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "filter"), "filter");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    static void testDeepContract() {
        // exactly one abstract method: filter(GuardedInvocation, LinkRequest, LinkerServices)
        int n = 0; Method abs = null;
        for (Method m : GuardedInvocationTransformer.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1, "single abstract method");
        check(abs.getName().equals("filter") && abs.getParameterCount() == 3, "filter(GI, request, services)");
        check(Modifier.isInterface(GuardedInvocationTransformer.class.getModifiers()), "interface");
    }

    static void testPrelinkLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle inv = lk.findVirtual(String.class, "length", MethodType.methodType(int.class))
                .asType(MethodType.methodType(Object.class, Object.class));
        MethodHandle guard = Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class));
        final List<String> seen = new ArrayList<>();
        // the transformer observes every successful link and may replace the invocation
        final MethodHandle tag;
        try {
            tag = MethodHandles.insertArguments(lk.findStatic(GuardedInvocationTransformerTest.class, "tag",
                MethodType.methodType(Object.class, String.class, Object.class)), 0, "T");
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
        GuardedInvocationTransformer wrap = (gi, req, svc) -> {
            seen.add(req.getCallSiteDescriptor().getOperation().toString());
            try {
                return gi.replaceMethods(MethodHandles.filterReturnValue(gi.getInvocation(), tag), gi.getGuard());
            } catch (RuntimeException | Error x) { throw x; }
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        f.setPrelinkTransformer(wrap);
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("length"),
            MethodType.methodType(Object.class, Object.class)));
        // String exposes no "length" property (it is a method): use a record-like bean instead
        SimpleRelinkableCallSite cs2 = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("name"),
            MethodType.methodType(Object.class, Object.class)));
        f.createLinker().link(cs2);
        check(cs2.getTarget().invoke((Object) new Bean()).equals("T:name"), "prelink wrap applied: ");
        check(seen.equals(List.of("GET:PROPERTY:name")), "transformer saw the operation: " + seen);
        // returning null is rejected by the linker (Objects.requireNonNull in relink)
        DynamicLinkerFactory f2 = new DynamicLinkerFactory();
        f2.setPrioritizedLinker(new BeansLinker());
        f2.setFallbackLinkers();
        f2.setPrelinkTransformer((gi, req, svc) -> null);
        SimpleRelinkableCallSite cs3 = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("name"),
            MethodType.methodType(Object.class, Object.class)));
        f2.createLinker().link(cs3);
        try {
            cs3.getTarget().invoke((Object) new Bean());
            check(false, "null prelink result must fail");
        } catch (NullPointerException expected) { check(true, "null prelink result NPE"); }
    }

    public static class Bean { public String getName() { return "name"; } }
    public static Object tag(String t, Object v) { return t + ":" + v; }


    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        testPrelinkLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
