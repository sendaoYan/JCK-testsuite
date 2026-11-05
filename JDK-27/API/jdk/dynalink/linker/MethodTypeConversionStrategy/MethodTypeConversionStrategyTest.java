/*
 * @test
 * @summary jdk.dynalink.linker.MethodTypeConversionStrategy conformance: white-box structure + black-box behavior.
 * @run main MethodTypeConversionStrategyTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodTypeConversionStrategyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.MethodTypeConversionStrategy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "asType"), "asType");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    static void testFunctionalSurface() throws Exception {
        int n = 0; Method abs = null;
        for (Method m : MethodTypeConversionStrategy.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1 && abs.getName().equals("asType"), "single abstract asType");
        check(abs.getParameterCount() == 2, "two parameters: handle and new type");
        check(abs.getReturnType() == MethodHandle.class, "returns MethodHandle");
        check(Modifier.isInterface(MethodTypeConversionStrategy.class.getModifiers()), "interface");
    }

    static void testStrategyLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<String> transitions = new ArrayList<>();
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        f.setAutoConversionStrategy((target, newType) -> {
            transitions.add(target.type() + "=>" + newType);
            return target.asType(newType);
        });
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("name"),
            MethodType.methodType(Object.class, Object.class)));
        f.createLinker().link(cs);
        check(cs.getTarget().invoke((Object) new Bean()).equals("n"), "strategy-configured link works");
        check(transitions.size() >= 2, "strategy applied to invocation and guard: " + transitions.size());
        for (String t : transitions)
            check(t.endsWith("=>(Object)Object") || t.endsWith("=>(Object)boolean"),
                  "each adaptation reaches a site-compatible type: " + t);
    }

    public static class Bean { public String getName() { return "n"; } }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testFunctionalSurface();
        testStrategyLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
