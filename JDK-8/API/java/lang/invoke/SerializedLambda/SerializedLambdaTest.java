/*
 * @test
 * @summary java.lang.invoke.SerializedLambda conformance: structure, field accessors, writeReplace capture (zero-arg/captured javac lambdas), factory lambdas and serialization round-trips.
 * @run main SerializedLambdaTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SerializedLambdaTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSerializedLambda() {
        Object[] args = {1, "two"};
        SerializedLambda sl = new SerializedLambda(
                SerializedLambdaTest.class, "java.lang.Runnable", "run", "()V",
                6, "impl/Class", "runImpl", "()V", "()V", args);
        check(SerializedLambdaTest.class.getName().equals(sl.getCapturingClass()),
              "getCapturingClass");
        check("java.lang.Runnable".equals(sl.getFunctionalInterfaceClass()),
              "getFunctionalInterfaceClass");
        check("run".equals(sl.getFunctionalInterfaceMethodName()),
              "getFunctionalInterfaceMethodName");
        check("()V".equals(sl.getFunctionalInterfaceMethodSignature()),
              "getFunctionalInterfaceMethodSignature");
        check("impl/Class".equals(sl.getImplClass()), "getImplClass");
        check("runImpl".equals(sl.getImplMethodName()), "getImplMethodName");
        check("()V".equals(sl.getImplMethodSignature()), "getImplMethodSignature");
        check(sl.getImplMethodKind() == 6, "getImplMethodKind");
        check("()V".equals(sl.getInstantiatedMethodType()), "getInstantiatedMethodType");
        check(sl.getCapturedArgCount() == 2, "getCapturedArgCount");
        check(sl.getCapturedArg(0).equals(1), "getCapturedArg 0");
        check(sl.getCapturedArg(1).equals("two"), "getCapturedArg 1");
        check(sl instanceof Serializable, "implements Serializable");
        check(sl.toString() != null, "toString");
    }

    // ---- helpers and self-authored serializable shapes ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public interface SF extends Serializable { String apply(String s); }
    public interface SS extends Serializable { String get(); }
    public interface Op extends Serializable { String apply(String s, int n); }

    // javac lambda in a static initializer: zero captured values, static impl method
    static final SF STATIC_FN = s -> s + "-ok";

    static String decorate(String s, int n) { return s + "_" + n; }

    static SerializedLambda writeReplaceOf(Object lambda) throws Exception {
        Method wr = lambda.getClass().getDeclaredMethod("writeReplace");
        wr.setAccessible(true);
        return (SerializedLambda) wr.invoke(lambda);
    }

    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        return back;
    }

    // ---- structure: public constructor shape and final accessor ----
    static void testStructureDeep() throws Exception {
        Class<?> c = SerializedLambda.class;
        check(Modifier.isFinal(c.getModifiers()), "class is final");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?>[] pub = c.getConstructors();
        check(pub.length == 1, "exactly one public constructor");
        check(pub[0].getParameterCount() == 10, "public constructor takes 10 parameters");
        Class<?>[] ps = pub[0].getParameterTypes();
        check(ps[0] == Class.class, "ctor param 0 is Class");
        check(ps[4] == int.class, "ctor param 4 is int");
        check(ps[9] == Object[].class, "ctor param 9 is Object[]");
        Method imt = c.getMethod("getInstantiatedMethodType");
        check(Modifier.isFinal(imt.getModifiers()), "getInstantiatedMethodType is final");
        check(imt.getReturnType() == String.class, "getInstantiatedMethodType returns String");
    }

    // ---- writeReplace of a zero-capture static lambda ----
    static void testWriteReplaceStatic() throws Exception {
        SF f = SerializedLambdaTest.STATIC_FN;
        SerializedLambda sl = writeReplaceOf(f);
        check(sl != null, "writeReplace yields a SerializedLambda");
        check(sl.getFunctionalInterfaceClass().equals("SerializedLambdaTest$SF"),
                "functional interface uses the binary $ name");
        check(sl.getFunctionalInterfaceMethodName().equals("apply"), "fi method name");
        check(sl.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;)Ljava/lang/String;"),
                "fi method signature");
        check(sl.getImplClass().equals("SerializedLambdaTest"), "impl class");
        check(sl.getImplMethodName().startsWith("lambda$"), "impl method is lambda$...");
        check(sl.getImplMethodKind() == java.lang.invoke.MethodHandleInfo.REF_invokeStatic,
                "static-context lambda -> REF_invokeStatic");
        check(sl.getInstantiatedMethodType().equals("(Ljava/lang/String;)Ljava/lang/String;"),
                "instantiated method type");
        check(sl.getCapturedArgCount() == 0, "zero captured arguments");
        check(sl.toString().contains("SerializedLambdaTest"), "toString contains class");
        check(sl.toString().contains("numCaptured=0"), "toString contains numCaptured=0");
        check(f.apply("x").equals("x-ok"), "the lambda itself computes");
    }

    // ---- writeReplace of a capturing lambda (runtime local, not a constant) ----
    static void testWriteReplaceCaptured() throws Exception {
        final int k = Integer.parseInt("7"); // runtime value: forces a real capture
        SS real = () -> "v" + k;
        SerializedLambda sl = writeReplaceOf(real);
        check(sl.getCapturedArgCount() == 1, "one captured argument");
        check(sl.getCapturedArg(0).equals(Integer.valueOf(7)), "captured value is 7");
        check(sl.getImplMethodSignature().equals("(I)Ljava/lang/String;"),
                "impl signature takes the captured int");
        check(sl.getInstantiatedMethodType().equals("()Ljava/lang/String;"),
                "instantiated SAM has no parameters");
        check(sl.getFunctionalInterfaceClass().equals("SerializedLambdaTest$SS"), "capturing fi class");
        check(real.get().equals("v7"), "capturing lambda computes v7");
    }

    // ---- writeReplace is only generated for serializable interfaces ----
    static void testWriteReplaceMissing() throws Exception {
        final java.util.function.Supplier<String> lam = () -> "s";
        checkThrows(NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                lam.getClass().getDeclaredMethod("writeReplace");
            }
        }, "non-serializable lambda has no writeReplace");
        check(lam.get().equals("s"), "the plain lambda still works");
    }

    // ---- captured-arg bounds ----
    static void testCapturedArgBounds() {
        final SerializedLambda one = new SerializedLambda(SerializedLambdaTest.class,
                "SerializedLambdaTest$SF", "apply", "(Ljava/lang/String;)Ljava/lang/String;",
                6, "SerializedLambdaTest", "lambda$b",
                "(Ljava/lang/String;)Ljava/lang/String;",
                "(Ljava/lang/String;)Ljava/lang/String;", new Object[] { "x" });
        checkThrows(ArrayIndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { one.getCapturedArg(1); }
        }, "captured arg out of bounds");
        checkThrows(ArrayIndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { one.getCapturedArg(-1); }
        }, "negative captured arg index");
    }

    // ---- javac lambda serialization round-trip through memory ----
    static void testJavacRoundTrip() throws Exception {
        Object back = roundTrip(SerializedLambdaTest.STATIC_FN);
        check(back instanceof SF, "deserialized object is an SF");
        check(((SF) back).apply("ab").equals("ab-ok"), "deserialized static lambda computes");
        final int k = Integer.parseInt("7");
        SS cap = () -> "v" + k;
        Object back2 = roundTrip(cap);
        check(back2 instanceof SS, "deserialized capturing lambda is an SS");
        check(((SS) back2).get().equals("v7"), "deserialized capturing lambda computes");
    }

    // ---- factory-built (altMetafactory) lambda: capture + SerializedLambda round-trip ----
    static void testFactoryLambda() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(SerializedLambdaTest.class, "decorate",
                MethodType.methodType(String.class, String.class, int.class));
        CallSite cs = LambdaMetafactory.altMetafactory(lookup, "apply",
                MethodType.methodType(Op.class),
                MethodType.methodType(String.class, String.class, int.class),
                impl, MethodType.methodType(String.class, String.class, int.class),
                LambdaMetafactory.FLAG_SERIALIZABLE);
        Op op = (Op) cs.dynamicInvoker().invokeExact();
        check(op.apply("a", 7).equals("a_7"), "factory lambda computes");
        SerializedLambda sl = writeReplaceOf(op);
        check(sl.getFunctionalInterfaceClass().equals("SerializedLambdaTest$Op"),
                "factory fi binary name with $");
        check(sl.getFunctionalInterfaceMethodName().equals("apply"), "factory fi name");
        check(sl.getImplClass().equals("SerializedLambdaTest"), "factory impl class");
        check(sl.getImplMethodName().equals("decorate"), "factory impl method is decorate");
        check(sl.getImplMethodKind() == java.lang.invoke.MethodHandleInfo.REF_invokeStatic,
                "factory impl kind REF_invokeStatic");
        check(sl.getInstantiatedMethodType().equals("(Ljava/lang/String;I)Ljava/lang/String;"),
                "factory instantiated descriptor");
        check(sl.getImplMethodSignature().equals("(Ljava/lang/String;I)Ljava/lang/String;"),
                "factory impl signature");
        check(sl.getCapturedArgCount() == 0, "factory lambda captures nothing");
        check(sl.toString().contains("decorate"), "toString names the implementation");
        // Neither the lambda nor its SerializedLambda form can re-materialize: javac only
        // emits $deserializeLambda$ mappings for lambdas it compiled itself, and the private
        // readResolve routes both through it. Observed wrapper on JDK-8: IOException.
        checkThrows(java.io.IOException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                roundTrip(op);
            }
        }, "factory lambda deserialization fails without a lambda$ mapping (observed)");
        checkThrows(java.io.IOException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                roundTrip(sl);
            }
        }, "SerializedLambda readResolve relinks via $deserializeLambda$ (observed)");
    }

    public static void main(String[] args) throws Throwable {
        testSerializedLambda();
        testStructureDeep();
        testWriteReplaceStatic();
        testWriteReplaceCaptured();
        testWriteReplaceMissing();
        testCapturedArgBounds();
        testJavacRoundTrip();
        testFactoryLambda();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
