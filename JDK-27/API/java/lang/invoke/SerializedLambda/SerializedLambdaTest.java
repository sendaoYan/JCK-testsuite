/*
 * @test
 * @summary java.lang.invoke.SerializedLambda conformance: white-box + black-box behavior.
 * @run main SerializedLambdaTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

    public interface F extends Serializable {
        int len(String s);
    }

    // ObjectOutputStream subclass that intercepts SerializedLambda replacements
    static class CapturingOOS extends ObjectOutputStream {
        SerializedLambda captured;
        CapturingOOS(OutputStream out) throws java.io.IOException {
            super(out);
            enableReplaceObject(true);
        }
        @Override protected Object replaceObject(Object obj) {
            if (obj instanceof SerializedLambda && captured == null) captured = (SerializedLambda) obj;
            return obj;
        }
    }

    static int body(String s) { return s.length() * 2; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.SerializedLambda");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        int pubCtors = 0;
        Constructor<?> pubCtor = null;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) { pubCtors++; pubCtor = ct; }
        }
        check(pubCtors == 1 && pubCtor.getParameterCount() == 10,
              "one public 10-arg constructor");
        check(pubCtor.getParameterTypes()[0] == Class.class
              && pubCtor.getParameterTypes()[4] == int.class
              && pubCtor.getParameterTypes()[9] == Object[].class,
              "ctor shape: (Class, String, String, String, int, String, String, String, String, Object[])");
        for (String m : new String[]{"getCapturingClass", "getCapturedArgCount", "getCapturedArg",
                                      "getFunctionalInterfaceClass", "getFunctionalInterfaceMethodName",
                                      "getFunctionalInterfaceMethodSignature", "getImplClass",
                                      "getImplMethodName", "getImplMethodSignature", "getImplMethodKind",
                                      "getInstantiatedMethodType", "toString"}) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // capture a static-helper lambda (no captured values)
        F plain = s -> body(s);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CapturingOOS oos = new CapturingOOS(bos);
        oos.writeObject(plain);
        SerializedLambda sl = oos.captured;
        check(sl != null, "serialization substitutes a SerializedLambda");
        check(sl.getFunctionalInterfaceClass().equals("SerializedLambdaTest$F"),
              "functional interface class: " + sl.getFunctionalInterfaceClass());
        check(sl.getFunctionalInterfaceMethodName().equals("len"),
              "functional interface method name");
        check(sl.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;)I"),
              "functional interface signature");
        check(sl.getImplClass().equals("SerializedLambdaTest"), "impl class");
        check(sl.getImplMethodName().startsWith("lambda$"), "impl method is lambda$...");
        check(sl.getImplMethodKind() == java.lang.invoke.MethodHandleInfo.REF_invokeStatic,
              "static helper lambda -> REF_invokeStatic");
        check(sl.getInstantiatedMethodType().equals("(Ljava/lang/String;)I"),
              "instantiated method type");
        check(sl.getCapturedArgCount() == 0, "no captured arguments");
        check(sl.toString().startsWith("SerializedLambda[capturingClass="), "toString format");

        // round trip: the lambda deserializes into a working F
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))
                .readObject();
        check(back instanceof F, "deserialized object is an F");
        check(((F) back).len("four") == 8, "deserialized lambda computes");

        // value-capturing lambda: the captured local appears in getCapturedArg
        String prefix = "abc";
        F capturing = s -> s.length() + prefix.length();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        CapturingOOS oos2 = new CapturingOOS(bos2);
        oos2.writeObject(capturing);
        SerializedLambda sl2 = oos2.captured;
        check(sl2.getCapturedArgCount() == 1, "captured one value");
        check("abc".equals(sl2.getCapturedArg(0)), "captured value is the local");
        Object back2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))
                .readObject();
        check(((F) back2).len("de") == 5, "capturing lambda round trip");

        // unbound method reference: virtual dispatch on the first argument
        F mr = String::length;
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        CapturingOOS oos3 = new CapturingOOS(bos3);
        oos3.writeObject(mr);
        SerializedLambda sl3 = oos3.captured;
        check(sl3.getImplClass().equals("java/lang/String"), "method ref impl class");
        check(sl3.getImplMethodName().equals("length"), "method ref impl name");
        check(sl3.getImplMethodKind() == java.lang.invoke.MethodHandleInfo.REF_invokeVirtual,
              "String::length -> REF_invokeVirtual");
        check(sl3.getCapturedArgCount() == 0, "unbound receiver captures nothing");
        Object back3 = new ObjectInputStream(new ByteArrayInputStream(bos3.toByteArray()))
                .readObject();
        check(((F) back3).len("hello") == 5, "method reference round trip");

        // the public constructor builds the replacement form directly
        SerializedLambda direct = new SerializedLambda(SerializedLambdaTest.class,
                "SerializedLambdaTest$F", "len", "(Ljava/lang/String;)I", 6,
                "SerializedLambdaTest", "lambda$direct", "(Ljava/lang/String;)I",
                "(Ljava/lang/String;)I", new Object[] { "x" });
        check("SerializedLambdaTest".equals(direct.getCapturingClass()),
              "direct ctor capturingClass");
        check(direct.getFunctionalInterfaceMethodName().equals("len"), "direct ctor fi name");
        check(direct.getImplMethodKind() == 6, "direct ctor impl kind");
        check(direct.getCapturedArgCount() == 1 && "x".equals(direct.getCapturedArg(0)),
              "direct ctor captured args");

        // non-serializable interface lambdas are never seen as SerializedLambda
        Runnable r = () -> { };
        check(!(r instanceof Serializable), "plain Runnable lambda is not serializable");
    }

    // ---- helpers and further self-authored shapes ----

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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
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
        Constructor<?>[] pub = c.getConstructors();
        check(pub.length == 1, "exactly one public constructor");
        check(pub[0].getParameterCount() == 10, "public constructor takes 10 parameters");
        Method imt = c.getMethod("getInstantiatedMethodType");
        check(Modifier.isFinal(imt.getModifiers()), "getInstantiatedMethodType is final");
        check(c.getMethod("getCapturedArg", int.class).getReturnType() == Object.class,
                "getCapturedArg returns Object");
        check(c.getMethod("getImplMethodKind").getReturnType() == int.class,
                "getImplMethodKind returns int");
        check(c.getMethod("getCapturingClass").getReturnType() == String.class,
                "getCapturingClass returns String");
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
        check(sl.getImplMethodSignature().equals("(Ljava/lang/String;)Ljava/lang/String;"),
                "impl signature");
        check(sl.getInstantiatedMethodType().equals("(Ljava/lang/String;)Ljava/lang/String;"),
                "instantiated method type");
        check(sl.getCapturedArgCount() == 0, "zero captured arguments");
        check(sl.toString().contains("SerializedLambdaTest"), "toString contains class");
        check(sl.toString().contains("numCaptured=0"), "toString contains numCaptured=0");
        check(sl.toString().contains("lambda$"), "toString names the lambda$ impl");
        check(f.apply("x").equals("x-ok"), "the lambda itself computes");
    }

    // ---- writeReplace of capturing lambdas (runtime locals, not constants) ----
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

        // a String local is captured by value
        final String base = "he" + "ll".concat("o");
        SS bound = () -> String.valueOf(base.length());
        SerializedLambda sb = writeReplaceOf(bound);
        check(sb.getCapturedArgCount() == 1, "string-capturing lambda captures one value");
        check(sb.getCapturedArg(0).equals("hello"), "captured string value is hello");
        check(bound.get().equals("5"), "string-capturing lambda computes");
    }

    // ---- writeReplace is only generated for serializable interfaces ----
    static void testWriteReplaceMissing() throws Exception {
        final java.util.function.Supplier<String> lam = () -> "s";
        checkThrows(NoSuchMethodException.class,
                () -> lam.getClass().getDeclaredMethod("writeReplace"),
                "non-serializable lambda has no writeReplace");
        check(lam.get().equals("s"), "the plain lambda still works");
    }

    // ---- captured-arg bounds ----
    static void testCapturedArgBounds() {
        final SerializedLambda one = new SerializedLambda(SerializedLambdaTest.class,
                "SerializedLambdaTest$SF", "apply", "(Ljava/lang/String;)Ljava/lang/String;",
                6, "SerializedLambdaTest", "lambda$b",
                "(Ljava/lang/String;)Ljava/lang/String;",
                "(Ljava/lang/String;)Ljava/lang/String;", new Object[] { "x" });
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> one.getCapturedArg(1),
                "captured arg out of bounds");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> one.getCapturedArg(-1),
                "negative captured arg index");
        check(one.getCapturedArgCount() == 1, "count is exactly one");
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

    // ---- factory-built (altMetafactory) lambda: capture + readResolve semantics ----
    static void testFactoryLambda() throws Throwable {
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodHandle impl = lookup.findStatic(SerializedLambdaTest.class,
                "decorate",
                java.lang.invoke.MethodType.methodType(String.class, String.class, int.class));
        java.lang.invoke.CallSite cs = java.lang.invoke.LambdaMetafactory.altMetafactory(
                lookup, "apply",
                java.lang.invoke.MethodType.methodType(Op.class),
                java.lang.invoke.MethodType.methodType(String.class, String.class, int.class),
                impl, java.lang.invoke.MethodType.methodType(String.class, String.class, int.class),
                java.lang.invoke.LambdaMetafactory.FLAG_SERIALIZABLE);
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
        check(sl.toString().contains("invokeStatic"), "toString names the handle kind");
        // Neither the lambda nor its SerializedLambda form can re-materialize: javac only
        // emits $deserializeLambda$ mappings for lambdas it compiled itself, and the private
        // readResolve routes both through it. Observed wrapper on JDK-9+: InvalidObjectException.
        checkThrows(java.io.InvalidObjectException.class, () -> roundTrip(op),
                "factory lambda deserialization fails without a lambda$ mapping (observed)");
        checkThrows(java.io.InvalidObjectException.class, () -> roundTrip(sl),
                "SerializedLambda readResolve relinks via $deserializeLambda$ (observed)");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
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
