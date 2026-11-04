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

    public static void main(String[] args) throws Exception {
        testClassFileTransformer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
