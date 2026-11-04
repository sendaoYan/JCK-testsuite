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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
