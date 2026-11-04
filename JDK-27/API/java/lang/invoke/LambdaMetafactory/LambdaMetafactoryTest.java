/*
 * @test
 * @summary java.lang.invoke.LambdaMetafactory conformance: white-box + black-box behavior.
 * @run main LambdaMetafactoryTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LambdaMetafactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface F {
        int len(String s);
    }

    static int body(String s) { return s.length(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.LambdaMetafactory");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        check(c.getField("FLAG_SERIALIZABLE").getInt(null) == 1, "FLAG_SERIALIZABLE == 1");
        check(c.getField("FLAG_MARKERS").getInt(null) == 2, "FLAG_MARKERS == 2");
        check(c.getField("FLAG_BRIDGES").getInt(null) == 4, "FLAG_BRIDGES == 4");
        check(c.getDeclaredMethod("metafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, MethodType.class, MethodHandle.class,
                MethodType.class).getReturnType() == CallSite.class,
              "metafactory returns CallSite");
        check(c.getDeclaredMethod("altMetafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, Object[].class) != null, "altMetafactory declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "body",
                MethodType.methodType(int.class, String.class));

        // metafactory: standard non-serializable lambda
        CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(F.class),                       // invoked type: ()F
                MethodType.methodType(int.class, String.class),        // SAM signature
                impl,                                                  // implementation
                MethodType.methodType(int.class, String.class));       // instantiated SAM
        F f = (F) cs.dynamicInvoker().invokeExact();
        check(f.len("method") == 6, "metafactory lambda computes");
        check(f.len("") == 0, "lambda on empty string");
        // each invocation of the dynamic invoker yields a fresh instance
        F f2 = (F) cs.dynamicInvoker().invokeExact();
        check(f2.len("method") == 6, "second instance computes identically");

        // altMetafactory with FLAG_SERIALIZABLE
        CallSite alt = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE);
        F sf = (F) alt.dynamicInvoker().invokeExact();
        check(sf.len("abc") == 3, "altMetafactory lambda computes");
        check(sf instanceof java.io.Serializable, "FLAG_SERIALIZABLE adds Serializable");

        // altMetafactory with FLAG_MARKERS adds extra marker interfaces
        CallSite marked = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class),
                LambdaMetafactory.FLAG_MARKERS, 1, java.io.Serializable.class);
        F mf = (F) marked.dynamicInvoker().invokeExact();
        check(mf instanceof java.io.Serializable, "FLAG_MARKERS adds the marker");

        // conversion failures are LambdaConversionException
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(String.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(int.class, String.class));
            check(false, "factory type must return an interface");
        } catch (LambdaConversionException e) {
            check(true, "non-interface factory type -> LambdaConversionException");
        }
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(F.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(String.class, String.class));
            check(false, "incompatible instantiated type must fail");
        } catch (LambdaConversionException e) {
            check(true, "incompatible instantiated type -> LambdaConversionException");
        }
        // impl method that cannot be adapted to the SAM signature
        MethodHandle bad = lookup.findStatic(LambdaMetafactoryTest.class, "intBody",
                MethodType.methodType(int.class, int.class));
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(F.class),
                    MethodType.methodType(int.class, String.class),
                    bad, MethodType.methodType(int.class, String.class));
            check(false, "mismatched impl signature must fail");
        } catch (LambdaConversionException e) {
            check(true, "mismatched impl -> LambdaConversionException");
        }
    }

    static int intBody(int x) { return x; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
