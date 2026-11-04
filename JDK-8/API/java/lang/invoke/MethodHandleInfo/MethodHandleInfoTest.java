/*
 * @test
 * @summary java.lang.invoke.MethodHandleInfo conformance: reveal and reference kinds.
 * @run main MethodHandleInfoTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMethodHandleInfo() throws Throwable {
        check(MethodHandleInfo.REF_getField == 1, "REF_getField");
        check(MethodHandleInfo.REF_invokeVirtual == 5, "REF_invokeVirtual");
        check(MethodHandleInfo.REF_newInvokeSpecial == 8, "REF_newInvokeSpecial");
        check(MethodHandleInfo.REF_invokeInterface == 9, "REF_invokeInterface");
        check("invokeVirtual".equals(MethodHandleInfo.referenceKindToString(
                MethodHandleInfo.REF_invokeVirtual)), "referenceKindToString");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh = lookup.findVirtual(
                String.class, "length", MethodType.methodType(int.class));
        MethodHandleInfo info = lookup.revealDirect(mh);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeVirtual,
              "getReferenceKind");
        check(info.getDeclaringClass() == String.class, "getDeclaringClass");
        check("length".equals(info.getName()), "getName");
        check(info.getMethodType().equals(MethodType.methodType(int.class)), "getMethodType");
        check(info.getModifiers() != 0, "getModifiers");
        check(!info.isVarArgs(), "isVarArgs");
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandleInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
