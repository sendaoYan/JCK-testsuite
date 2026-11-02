/*
 * @test
 * @summary java.lang.constant.ConstantDescs conformance: named constants,
 *          primitive descriptors, NULL/TRUE/FALSE, and bootstrap constants.
 * @run main ConstantDescsTest
 */

import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;

public class ConstantDescsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check("_".equals(ConstantDescs.DEFAULT_NAME), "DEFAULT_NAME");
        check("<init>".equals(ConstantDescs.INIT_NAME), "INIT_NAME");
        check("<clinit>".equals(ConstantDescs.CLASS_INIT_NAME), "CLASS_INIT_NAME");

        check("String".equals(ConstantDescs.CD_String.displayName()), "CD_String");
        check("Object".equals(ConstantDescs.CD_Object.displayName()), "CD_Object");
        check(ConstantDescs.CD_int.isPrimitive(), "CD_int primitive");
        check("I".equals(ConstantDescs.CD_int.descriptorString()), "CD_int descriptor");
        check("V".equals(ConstantDescs.CD_void.descriptorString()), "CD_void descriptor");
        check("J".equals(ConstantDescs.CD_long.descriptorString()), "CD_long descriptor");

        check("()V".equals(ConstantDescs.MTD_void.descriptorString()), "MTD_void");

        check(ConstantDescs.NULL.resolveConstantDesc(MethodHandles.lookup()) == null,
              "NULL resolves to null");
        check(Boolean.TRUE.equals(
                ConstantDescs.TRUE.resolveConstantDesc(MethodHandles.lookup())),
              "TRUE resolves to true");
        check(Boolean.FALSE.equals(
                ConstantDescs.FALSE.resolveConstantDesc(MethodHandles.lookup())),
              "FALSE resolves to false");

        check(ConstantDescs.BSM_ENUM_CONSTANT != null, "BSM_ENUM_CONSTANT");
        check(ConstantDescs.BSM_PRIMITIVE_CLASS != null, "BSM_PRIMITIVE_CLASS");
        check(ConstantDescs.BSM_GET_STATIC_FINAL != null, "BSM_GET_STATIC_FINAL");
        check(ConstantDescs.BSM_NULL_CONSTANT != null, "BSM_NULL_CONSTANT");
        check(ConstantDescs.BSM_INVOKE != null, "BSM_INVOKE");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
