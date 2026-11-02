/*
 * @test
 * @summary java.lang.classfile.constantpool.MethodHandleEntry conformance: behavioral.
 * @run main MethodHandleEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodHandleEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        DirectMethodHandleDesc mhd = (DirectMethodHandleDesc) MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "parseInt", MethodTypeDesc.of(ConstantDescs.CD_int, ClassDesc.of("java.lang.String")));
        MethodHandleEntry e = cpb.methodHandleEntry(mhd);
        check(e.reference().name().stringValue().equals("parseInt"), "reference name");
        check(e.asSymbol() instanceof DirectMethodHandleDesc, "asSymbol");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
