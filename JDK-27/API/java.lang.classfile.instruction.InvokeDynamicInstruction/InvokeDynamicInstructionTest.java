/*
 * @test
 * @summary java.lang.classfile.instruction.InvokeDynamicInstruction conformance: behavioral.
 * @run main InvokeDynamicInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InvokeDynamicInstructionTest {
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
        InvokeDynamicEntry e = cpb.invokeDynamicEntry(DynamicCallSiteDesc.of((DirectMethodHandleDesc) MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.invoke.StringConcatFactory"), "makeConcat", MethodTypeDesc.of(ClassDesc.of("java.lang.invoke.CallSite"), ClassDesc.of("java.lang.invoke.MethodHandles$Lookup"), ClassDesc.of("java.lang.String"), ClassDesc.of("java.lang.invoke.MethodType"))), "concat", MethodTypeDesc.of(ClassDesc.of("java.lang.String"))));
        InvokeDynamicInstruction i = InvokeDynamicInstruction.of(e);
        check(i.opcode() == Opcode.INVOKEDYNAMIC && "concat".equals(i.name().stringValue()), "invokedynamic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
