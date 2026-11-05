/*
 * @test
 * @summary java.lang.instrument.ClassFileTransformer conformance: white-box structure + black-box behavior.
 * @run main ClassFileTransformerTest
 */
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassFileTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.ClassFileTransformer");
        check(c.isInterface(), "interface");
        java.lang.reflect.Method m5 = c.getMethod("transform",
                ClassLoader.class, String.class, Class.class, ProtectionDomain.class, byte[].class);
        check(m5.getReturnType() == byte[].class, "5-arg transform returns byte[]");
        check(m5.isDefault(), "5-arg transform is a default method");
        java.lang.reflect.Method m6 = c.getMethod("transform",
                Module.class, ClassLoader.class, String.class, Class.class, ProtectionDomain.class, byte[].class);
        check(m6.getReturnType() == byte[].class, "6-arg transform returns byte[]");
        check(m6.isDefault(), "6-arg transform is a default method");
        check(java.util.Arrays.equals(m5.getExceptionTypes(), new Class[]{IllegalClassFormatException.class}),
              "5-arg transform throws IllegalClassFormatException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] input = {10, 20, 30};
        byte[] marker = {99};

        // Implementation overriding only the legacy 5-arg transform
        ClassFileTransformer echo = new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String name, Class<?> beingRedefined,
                                    ProtectionDomain pd, byte[] buffer) {
                return marker;
            }
        };
        check(echo.transform(null, "java/lang/String", null, null, input) == marker,
              "5-arg transform returns the override's result");

        // 6-arg default method delegates to the 5-arg method
        check(echo.transform(Object.class.getModule(), null, "java/lang/String", null, null, input) == marker,
              "6-arg default transform delegates to 5-arg");

        // An implementation overriding nothing uses the defaults -> null
        ClassFileTransformer passthrough = new ClassFileTransformer() { };
        check(passthrough.transform(null, "java/lang/String", null, null, input) == null,
              "default 5-arg transform returns null");
        check(passthrough.transform(Object.class.getModule(), null, "java/lang/String", null, null, input) == null,
              "default 6-arg transform returns null");
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
        check(ms.length == 2, "JDK 27: two transform overloads (with and without Module)");
        boolean sawFive = false;
        boolean sawSix = false;
        for (java.lang.reflect.Method m : ms) {
            check(m.getReturnType() == byte[].class, "transform returns byte[]");
            check(m.isDefault(), "both overloads are default methods in JDK 9+");
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 5) {
                sawFive = true;
            }
            if (ps.length == 6 && ps[0] == Module.class) {
                sawSix = true;
            }
        }
        check(sawFive && sawSix, "5-arg and (Module,...) 6-arg signatures present");

        // impl overriding nothing: both defaults return null (no transformation)
        ClassFileTransformer empty = new ClassFileTransformer() { };
        check(empty.transform(null, "C", null, null, new byte[0]) == null,
              "5-arg default returns null");
        check(empty.transform(null, null, "D", null, null, new byte[0]) == null,
              "6-arg default returns null when nothing is overridden");

        // impl overriding ONLY the 5-arg method: the 6-arg default delegates to it
        ClassFileTransformer plainOnly = new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> being,
                                    ProtectionDomain pd, byte[] buf) {
                return new byte[]{2};
            }
        };
        byte[] viaSix = plainOnly.transform(null, null, "E", null, null, new byte[0]);
        check(viaSix != null && viaSix.length == 1 && viaSix[0] == 2,
              "6-arg default delegates to the 5-arg override");
        check(plainOnly.transform(null, "F", null, null, new byte[0]) != null,
              "direct 5-arg call hits the override");

        // impl overriding ONLY the 6-arg method: the 5-arg default still returns null
        ClassFileTransformer moduleOnly = new ClassFileTransformer() {
            public byte[] transform(Module module, ClassLoader loader, String className,
                                    Class<?> being, ProtectionDomain pd, byte[] buf) {
                recName = className;
                return new byte[]{1};
            }
        };
        byte[] directSix = moduleOnly.transform(null, null, "A", null, null, new byte[0]);
        check(directSix != null && directSix.length == 1, "direct 6-arg call hits the override");
        check("A".equals(recName), "6-arg override received the class name");
        byte[] viaFive = moduleOnly.transform((ClassLoader) null, "B", null, null, new byte[0]);
        check(viaFive == null, "5-arg default returns null without consulting the 6-arg override");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCallbackContract();
        testDeepInterfaceSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
