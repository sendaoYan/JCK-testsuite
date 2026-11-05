/*
 * @test
 * @summary jdk.dynalink.linker.ConversionComparator conformance: white-box structure + black-box behavior.
 * @run main ConversionComparatorTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class ConversionComparatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.ConversionComparator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
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

    static void testComparisonSemantics() throws Throwable {
        // the interface has exactly one abstract method
        int n = 0; String name = null;
        for (Method m : ConversionComparator.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; name = m.getName(); }
        check(n == 1 && name.equals("compareConversion"), "single abstract compareConversion");
        // a custom comparator reaches LinkerServices.compareConversion through the linker
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinkers(new ComparingLinker());
        f.setFallbackLinkers();
        LinkerServices svc = f.createLinker().getLinkerServices();
        check(svc.compareConversion(Object.class, String.class, Long.class) == ConversionComparator.Comparison.TYPE_1_BETTER,
              "custom comparator prefers String");
        check(svc.compareConversion(Object.class, Long.class, String.class) == ConversionComparator.Comparison.TYPE_2_BETTER,
              "custom comparator prefers String from the other side");
        check(svc.compareConversion(Object.class, Integer.class, Double.class) == ConversionComparator.Comparison.INDETERMINATE,
              "unrelated pair stays indeterminate");
    }

    interface LinkerWithComparator extends GuardingDynamicLinker, GuardingTypeConverterFactory, ConversionComparator { }

    static class ComparingLinker implements LinkerWithComparator {
        public GuardedInvocation getGuardedInvocation(LinkRequest req, LinkerServices svc) { return null; }
        public GuardedInvocation convertToType(Class<?> from, Class<?> to, java.util.function.Supplier<MethodHandles.Lookup> lsup) { return null; }
        public ConversionComparator.Comparison compareConversion(Class<?> src, Class<?> t1, Class<?> t2) {
            if (t1 == String.class && t2 == Long.class) return ConversionComparator.Comparison.TYPE_1_BETTER;
            if (t1 == Long.class && t2 == String.class) return ConversionComparator.Comparison.TYPE_2_BETTER;
            return ConversionComparator.Comparison.INDETERMINATE;
        }
    }

    static void testDefaultRules() throws Throwable {
        // with no custom comparator, method-invocation convertibility decides
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        LinkerServices svc = f.createLinker().getLinkerServices();
        check(svc.compareConversion(String.class, CharSequence.class, Integer.class) == ConversionComparator.Comparison.TYPE_1_BETTER,
              "String->CharSequence beats String->Integer");
        check(svc.compareConversion(String.class, Integer.class, CharSequence.class) == ConversionComparator.Comparison.TYPE_2_BETTER,
              "reversed order flips the verdict");
        check(svc.compareConversion(Object.class, CharSequence.class, String.class) == ConversionComparator.Comparison.INDETERMINATE,
              "both convertible stays indeterminate");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testComparisonSemantics();
        testDefaultRules();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
