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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
