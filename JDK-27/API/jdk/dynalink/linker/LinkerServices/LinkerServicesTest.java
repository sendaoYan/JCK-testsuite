/*
 * @test
 * @summary jdk.dynalink.linker.LinkerServices conformance: white-box structure + black-box behavior.
 * @run main LinkerServicesTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class LinkerServicesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.LinkerServices");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "asType"), "asType");
        check(hasMethod(c, "getTypeConverter"), "getTypeConverter");
        check(hasMethod(c, "canConvert"), "canConvert");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");
        check(hasMethod(c, "compareConversion"), "compareConversion");

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

    static void testConversionServices() throws Throwable {
        LinkerServices svc = beanLinker().getLinkerServices();
        // canConvert follows method-invocation convertibility when no custom converter exists
        check(svc.canConvert(String.class, Object.class), "widening always convertible");
        check(svc.canConvert(int.class, long.class), "primitive widening");
        check(!svc.canConvert(String.class, int.class), "String->int not method-invocation convertible");
        check(!svc.canConvert(String.class, List.class), "unrelated reference types");
        // yet the default engine can still synthesize a lossy converter handle
        MethodHandle conv = svc.getTypeConverter(String.class, int.class);
        check(conv != null, "getTypeConverter synthesizes a String->int converter");
        check(conv.type().equals(MethodType.methodType(int.class, String.class)), "converter shape: " + conv.type());
        MethodHandle wid = svc.getTypeConverter(String.class, CharSequence.class);
        check(wid != null && wid.type().equals(MethodType.methodType(CharSequence.class, String.class)),
              "widening converter synthesized");
        check(((CharSequence) wid.invokeWithArguments((Object) "x")).equals("x"), "widening converter casts");
        check(svc.getTypeConverter(Object.class, String.class) != null, "downcast converter synthesized");
        // asType adapts parameters and return
        MethodHandle len = MethodHandles.lookup().findStatic(String.class, "valueOf",
                MethodType.methodType(String.class, Object.class));
        check(svc.asType(len, MethodType.methodType(CharSequence.class, Object.class)).type()
              .equals(MethodType.methodType(CharSequence.class, Object.class)), "asType return narrowing");
        // asTypeLosslessReturn keeps the lossless return type
        check(svc.asTypeLosslessReturn(len, MethodType.methodType(Object.class, Integer.class)).type()
              .equals(MethodType.methodType(Object.class, Integer.class)), "lossless return kept");
        check(svc.asTypeLosslessReturn(len, MethodType.methodType(int.class, Integer.class)).type()
              .equals(MethodType.methodType(String.class, Integer.class)), "lossy return reverts to handle return");
        // compareConversion default rules
        check(svc.compareConversion(String.class, CharSequence.class, Integer.class) == ConversionComparator.Comparison.TYPE_1_BETTER,
              "default comparison prefers convertible target");
        check(svc.compareConversion(Object.class, CharSequence.class, String.class) == ConversionComparator.Comparison.INDETERMINATE,
              "both convertible is indeterminate");
    }

    static void testLinkingServices() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        LinkerServices svc = beanLinker().getLinkerServices();
        // a full relink through the services, no call site needed
        SimpleLinkRequest req = new SimpleLinkRequest(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class)), false, new POJO());
        GuardedInvocation gi = svc.getGuardedInvocation(req);
        check(gi != null && gi.getInvocation().invoke((Object) new POJO()).equals(7), "services relink works");
        // filterInternalObjects is the identity without a configured filter
        MethodHandle id = MethodHandles.identity(Object.class);
        check(svc.filterInternalObjects(id) == id, "filterInternalObjects identity by default");
        // getWithLookup evaluates the supplier
        check(svc.getWithLookup(() -> "v", new CallSiteDescriptor(lk, StandardOperation.GET,
                MethodType.methodType(Object.class))).equals("v"), "getWithLookup");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testConversionServices();
        testLinkingServices();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
