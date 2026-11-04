/*
 * @test
 * @summary java.lang.instrument.ClassDefinition conformance: class and byte[] accessors.
 * @run main ClassDefinitionTest
 */

import java.lang.instrument.ClassDefinition;

public class ClassDefinitionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassDefinition() {
        byte[] bytes = new byte[] {1, 2, 3};
        ClassDefinition cd = new ClassDefinition(Object.class, bytes);
        check(cd.getDefinitionClass() == Object.class, "getDefinitionClass");
        check(cd.getDefinitionClassFile().length == 3, "getDefinitionClassFile length");
    }

    public static void main(String[] args) {
        testClassDefinition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
