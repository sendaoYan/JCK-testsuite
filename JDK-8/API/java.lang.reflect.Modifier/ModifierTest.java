/*
 * @test
 * @summary java.lang.reflect.Modifier conformance: constant values and modifier-check helpers.
 * @run main ModifierTest
 */

import java.lang.reflect.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testModifier() {
        check(Modifier.PUBLIC == 0x00000001, "PUBLIC value");
        check(Modifier.PRIVATE == 0x00000002, "PRIVATE value");
        check(Modifier.PROTECTED == 0x00000004, "PROTECTED value");
        check(Modifier.STATIC == 0x00000008, "STATIC value");
        check(Modifier.FINAL == 0x00000010, "FINAL value");
        check(Modifier.ABSTRACT == 0x00000400, "ABSTRACT value");
        check(Modifier.INTERFACE == 0x00000200, "INTERFACE value");

        check(Modifier.isPublic(Modifier.PUBLIC), "isPublic");
        check(Modifier.isPrivate(Modifier.PRIVATE), "isPrivate");
        check(Modifier.isProtected(Modifier.PROTECTED), "isProtected");
        check(Modifier.isStatic(Modifier.STATIC), "isStatic");
        check(Modifier.isFinal(Modifier.FINAL), "isFinal");
        check(Modifier.isSynchronized(Modifier.SYNCHRONIZED), "isSynchronized");
        check(Modifier.isVolatile(Modifier.VOLATILE), "isVolatile");
        check(Modifier.isTransient(Modifier.TRANSIENT), "isTransient");
        check(Modifier.isNative(Modifier.NATIVE), "isNative");
        check(Modifier.isInterface(Modifier.INTERFACE), "isInterface");
        check(Modifier.isAbstract(Modifier.ABSTRACT), "isAbstract");
        check(Modifier.isStrict(Modifier.STRICT), "isStrict");

        String s = Modifier.toString(Modifier.PUBLIC | Modifier.STATIC);
        check(s.contains("public") && s.contains("static"), "toString");
    }

    public static void main(String[] args) {
        testModifier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
