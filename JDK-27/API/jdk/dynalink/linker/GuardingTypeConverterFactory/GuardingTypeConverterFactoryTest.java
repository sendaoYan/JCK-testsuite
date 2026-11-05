/*
 * @test
 * @summary jdk.dynalink.linker.GuardingTypeConverterFactory conformance: white-box structure + black-box behavior.
 * @run main GuardingTypeConverterFactoryTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardingTypeConverterFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardingTypeConverterFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "convertToType"), "convertToType");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    static void testFunctionalSurface() throws Exception {
        int n = 0; Method abs = null;
        for (Method m : GuardingTypeConverterFactory.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1 && abs.getName().equals("convertToType"), "single abstract convertToType");
        check(abs.getParameterCount() == 3, "three parameters: source, target, lookup supplier");
        check(abs.getParameterTypes()[2] == java.util.function.Supplier.class, "third parameter is a Supplier<Lookup>");
        check(Modifier.isInterface(GuardingTypeConverterFactory.class.getModifiers()), "interface");
    }

    public static int parseInt(String s) { return Integer.parseInt(s); }
    interface ConvLinker extends GuardingDynamicLinker, GuardingTypeConverterFactory { }

    static void testConverterLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle s2i = lk.findStatic(GuardingTypeConverterFactoryTest.class, "parseInt",
                MethodType.methodType(int.class, String.class));
        final List<Class<?>> asked = new ArrayList<>();
        GuardingDynamicLinker conv = new ConvLinker() {
            public GuardedInvocation getGuardedInvocation(LinkRequest req, LinkerServices svc) { return null; }
            public GuardedInvocation convertToType(Class<?> from, Class<?> to, java.util.function.Supplier<MethodHandles.Lookup> lsup) {
                if (to == int.class && from == String.class) {
                    asked.add(from);
                    return new GuardedInvocation(s2i, (MethodHandle) null);
                }
                return null;
            }
        };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinkers(conv);
        f.setFallbackLinkers();
        DynamicLinker linker = f.createLinker();
        LinkerServices svc = linker.getLinkerServices();
        check(svc.canConvert(String.class, int.class), "custom factory enables canConvert");
        check(!svc.canConvert(String.class, boolean.class), "unhandled pair stays unconvertible");
        MethodHandle converter = svc.getTypeConverter(String.class, int.class);
        check(converter != null, "getTypeConverter returns the custom converter");
        check((Integer) converter.invokeWithArguments((Object) "123") == 123, "converter parses");
        check(asked.equals(List.of(String.class)), "factory asked for the pair: " + asked);
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testFunctionalSurface();
        testConverterLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
