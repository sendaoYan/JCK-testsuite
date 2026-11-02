/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction conformance: behavioral.
 * @run main ConstantInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ConstantInstructionTest {
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
        ConstantInstruction i = ConstantInstruction.ofIntrinsic(Opcode.ICONST_0);
        check(i.opcode() == Opcode.ICONST_0 && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 1, "ofIntrinsic");
        check(ConstantInstruction.ofArgument(Opcode.BIPUSH, 7).sizeInBytes() == 2, "ofArgument size");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        check(ConstantInstruction.ofLoad(Opcode.LDC, cpb.intEntry(9)).sizeInBytes() == 2, "ofLoad size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
