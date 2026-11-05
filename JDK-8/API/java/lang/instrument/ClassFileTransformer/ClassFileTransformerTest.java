/*
 * @test
 * @summary java.lang.instrument.ClassFileTransformer conformance: transform contract.
 * @run main ClassFileTransformerTest
 */

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassFileTransformerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Noop implements ClassFileTransformer {
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain pd, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            return null; // no transformation
        }
    }

    static void testClassFileTransformer() throws Exception {
        ClassFileTransformer t = new Noop();
        check(t.transform(null, "java.lang.Object", null, null, new byte[0]) == null,
              "no-op transform returns null");
    }


    static String recLoader;
    static String recName;
    static String recBeing;
    static String recDomain;
    static byte[] recBuffer;
    static int recCalls;

    static final ClassFileTransformer RECORDER = new ClassFileTransformer() {
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain pd, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            recCalls++;
            recLoader = String.valueOf(loader);
            recName = className;
            recBeing = String.valueOf(classBeingRedefined);
            recDomain = String.valueOf(pd);
            recBuffer = classfileBuffer;
            return null; // documented: null means no transformation
        }
    };

    static final ClassFileTransformer THROWER = new ClassFileTransformer() {
        public byte[] transform(ClassLoader loader, String className, Class<?> being,
                                ProtectionDomain pd, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            throw new IllegalClassFormatException("boom:" + className);
        }
    };

    static void testDeepCallbackContract() throws Exception {
        // every argument is handed to the transformer verbatim; null is legal
        byte[] buf = new byte[]{7, 7, 7};
        byte[] r = RECORDER.transform(null, "java/lang/Foo", null, null, buf);
        check(r == null, "returning null means: leave the class file unchanged");
        check(recCalls == 1, "transform invoked exactly once");
        check(recName.equals("java/lang/Foo"), "className round-trips in internal form");
        check(recBuffer == buf, "classfileBuffer is the same reference (no copy)");
        check(recLoader.equals("null") && recBeing.equals("null") && recDomain.equals("null"),
              "null loader/being/domain are forwarded as null");
        RECORDER.transform(ClassLoader.getSystemClassLoader(), "x/Y", String.class, null, new byte[0]);
        check(recCalls == 2, "second call counted");
        check(recLoader.contains("ClassLoader"), "loader forwarded");
        check(recBeing.equals("class java.lang.String"), "classBeingRedefined forwarded");
        // exceptions raised by the transformer propagate unchanged to the caller
        try {
            THROWER.transform(null, "Z", null, null, new byte[0]);
            check(false, "transformer exception must propagate");
        } catch (IllegalClassFormatException e) {
            check("boom:Z".equals(e.getMessage()), "original exception object relayed verbatim");
        }
    }

    static void testDeepInterfaceSurface() throws Exception {
        java.lang.reflect.Method[] ms = ClassFileTransformer.class.getMethods();
        check(ms.length == 1, "JDK 8: exactly one transform overload");
        java.lang.reflect.Method m = ms[0];
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
              "the 5-arg transform is abstract in JDK 8 (implementors must provide it)");
        check(m.getReturnType() == byte[].class, "transform returns byte[]");
        Class<?>[] ps = m.getParameterTypes();
        check(ps.length == 5 && ps[0] == ClassLoader.class && ps[1] == String.class
                && ps[2] == Class.class && ps[3] == ProtectionDomain.class && ps[4] == byte[].class,
              "transform signature (ClassLoader,String,Class,ProtectionDomain,byte[])");
    }

    public static void main(String[] args) throws Exception {
        testClassFileTransformer();
        testDeepCallbackContract();
        testDeepInterfaceSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
