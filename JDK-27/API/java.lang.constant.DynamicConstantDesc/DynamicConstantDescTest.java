/*
 * @test
 * @summary java.lang.constant.DynamicConstantDesc conformance: accessors and
 *          resolution via the ConstantDescs.TRUE instance, plus ofNamed factory.
 * @run main DynamicConstantDescTest
 */

import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.invoke.MethodHandles;

public class DynamicConstantDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(!DynamicConstantDesc.class.isInterface(),
              "DynamicConstantDesc is an abstract class");

        DynamicConstantDesc<Boolean> t = ConstantDescs.TRUE;
        check("Ljava/lang/Boolean;".equals(t.constantType().descriptorString()),
              "constantType()");
        check(t.bootstrapMethod() != null, "bootstrapMethod()");
        check("TRUE".equals(t.constantName()), "constantName()");
        check(Boolean.TRUE.equals(t.resolveConstantDesc(MethodHandles.lookup())),
              "resolveConstantDesc() resolves to true");
        check(t.equals(ConstantDescs.TRUE), "equals()");
        check(t.hashCode() == ConstantDescs.TRUE.hashCode(), "hashCode()");

        DynamicConstantDesc<Boolean> named = DynamicConstantDesc.ofNamed(
                ConstantDescs.BSM_PRIMITIVE_CLASS, "x", ConstantDescs.CD_Boolean);
        check("x".equals(named.constantName()), "ofNamed constantName");
        check("Ljava/lang/Boolean;".equals(named.constantType().descriptorString()),
              "ofNamed constantType");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
