/*
 * @test
 * @summary java.lang.constant.ConstantDesc conformance: white-box + black-box behavior.
 * @run main ConstantDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class ConstantDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.ConstantDesc");
        check(c.isInterface(), "is an interface");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1 && ms[0].getName().equals("resolveConstantDesc"),
              "single method resolveConstantDesc");
        check(ms[0].getReturnType() == Object.class, "returns Object");
        check(ms[0].getExceptionTypes().length == 1
              && ms[0].getExceptionTypes()[0] == ReflectiveOperationException.class,
              "declares ReflectiveOperationException");
        check(c.isAssignableFrom(Class.forName("java.lang.constant.ClassDesc")), "ClassDesc subtype");
        check(c.isAssignableFrom(Class.forName("java.lang.constant.MethodHandleDesc")),
              "MethodHandleDesc subtype");
        check(c.isAssignableFrom(Class.forName("java.lang.constant.MethodTypeDesc")),
              "MethodTypeDesc subtype");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // NULL constant resolves to a null reference
        check(ConstantDescs.NULL.resolveConstantDesc(lookup) == null, "NULL resolves to null");

        // TRUE / FALSE dynamic constants resolve to the boxed booleans
        check(ConstantDescs.TRUE.resolveConstantDesc(lookup) == Boolean.TRUE, "TRUE -> true");
        check(ConstantDescs.FALSE.resolveConstantDesc(lookup) == Boolean.FALSE, "FALSE -> false");
        check(!ConstantDescs.TRUE.equals(ConstantDescs.FALSE), "TRUE != FALSE");

        // Integer's nominal form is a ConstantDesc that resolves to the same value
        ConstantDesc seven = Integer.valueOf(7).describeConstable().orElseThrow();
        check(seven.resolveConstantDesc(lookup).equals(7), "Integer nominal form resolves");

        // a ClassDesc used through its ConstantDesc view
        ConstantDesc stringDesc = ClassDesc.of("java.lang.String");
        check(stringDesc.resolveConstantDesc(lookup) == String.class,
              "ClassDesc as ConstantDesc resolves");

        // resolution failure surfaces as ReflectiveOperationException
        ConstantDesc missing = ClassDesc.of("no.such.Thing");
        try {
            missing.resolveConstantDesc(lookup);
            check(false, "missing class must throw");
        } catch (ReflectiveOperationException e) {
            check(e instanceof ClassNotFoundException, "ClassNotFoundException subtype");
        }

        // lambda-style use: map a ConstantDesc to its resolved value
        Optional<Object> resolved = Optional.of(ConstantDescs.NULL)
                .map(d -> { try { return d.resolveConstantDesc(lookup); } catch (ReflectiveOperationException ex) { return null; } });
        check(!resolved.isPresent() || resolved.get() == null,
              "NULL usable as a generic ConstantDesc");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
