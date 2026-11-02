/*
 * @test
 * @summary java.lang.constant.MethodHandleDesc conformance: ofConstructor,
 *          ofField, ofMethod, and resolution to working MethodHandles.
 * @run main MethodHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class MethodHandleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Throwable {
        check(MethodHandleDesc.class.isInterface(), "MethodHandleDesc is an interface");

        MethodHandleDesc ctor = MethodHandleDesc.ofConstructor(
                ClassDesc.of("java.lang.String"), ClassDesc.of("java.lang.String"));
        MethodHandle ctorMH = (MethodHandle) ctor.resolveConstantDesc(MethodHandles.lookup());
        check("hello".equals(ctorMH.invokeWithArguments("hello")),
              "String(String) constructor resolves and constructs");

        MethodHandleDesc getter = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.STATIC_GETTER, ClassDesc.of("java.lang.Integer"),
                "MAX_VALUE", ClassDesc.ofDescriptor("I"));
        MethodHandle getterMH = (MethodHandle) getter.resolveConstantDesc(MethodHandles.lookup());
        check((Integer) getterMH.invoke() == Integer.MAX_VALUE,
              "Integer.MAX_VALUE getter resolves to MAX_VALUE");

        MethodHandleDesc parse = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"),
                "parseInt", MethodTypeDesc.of(
                        ClassDesc.ofDescriptor("I"), ClassDesc.of("java.lang.String")));
        MethodHandle parseMH = (MethodHandle) parse.resolveConstantDesc(MethodHandles.lookup());
        check((Integer) parseMH.invoke("42") == 42,
              "Integer.parseInt(String) resolves and invokes");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
