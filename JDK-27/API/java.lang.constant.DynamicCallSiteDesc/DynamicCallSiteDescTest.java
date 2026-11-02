/*
 * @test
 * @summary java.lang.constant.DynamicCallSiteDesc conformance: of(), accessors,
 *          withArgs/withNameAndType, and resolveCallSiteDesc.
 * @run main DynamicCallSiteDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DynamicCallSiteDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static CallSite bootstrap(MethodHandles.Lookup l, String name, MethodType type) {
        return new ConstantCallSite(MethodHandles.constant(int.class, 42));
    }

    public static void main(String[] args) throws Throwable {
        check(DynamicCallSiteDesc.class != null, "DynamicCallSiteDesc is a final class");

        DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("DynamicCallSiteDescTest"), "bootstrap",
                MethodTypeDesc.of(
                        ClassDesc.of("java.lang.invoke.CallSite"),
                        ClassDesc.of("java.lang.invoke.MethodHandles$Lookup"),
                        ClassDesc.of("java.lang.String"),
                        ClassDesc.of("java.lang.invoke.MethodType")));

        DynamicCallSiteDesc d = DynamicCallSiteDesc.of(
                bsm, "answer", MethodTypeDesc.of(ClassDesc.ofDescriptor("I")));
        check("answer".equals(d.invocationName()), "invocationName()");
        check("()I".equals(d.invocationType().descriptorString()), "invocationType()");
        check(bsm.equals(d.bootstrapMethod()), "bootstrapMethod()");
        check(d.bootstrapArgs().length == 0, "bootstrapArgs() empty");

        DynamicCallSiteDesc withArg = d.withArgs(java.lang.Integer.valueOf(7));
        check(withArg.bootstrapArgs().length == 1, "withArgs adds one arg");

        DynamicCallSiteDesc renamed = d.withNameAndType(
                "other", MethodTypeDesc.of(ClassDesc.ofDescriptor("J")));
        check("other".equals(renamed.invocationName()), "withNameAndType name");
        check("()J".equals(renamed.invocationType().descriptorString()),
              "withNameAndType type");

        CallSite cs = d.resolveCallSiteDesc(MethodHandles.lookup());
        check(cs != null, "resolveCallSiteDesc() non-null");
        check((Integer) cs.dynamicInvoker().invoke() == 42,
              "resolved call site invokes to 42");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
