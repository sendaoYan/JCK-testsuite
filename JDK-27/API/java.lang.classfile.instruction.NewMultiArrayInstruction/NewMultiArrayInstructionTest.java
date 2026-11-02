/*
 * @test
 * @summary java.lang.classfile.instruction.NewMultiArrayInstruction conformance: behavioral.
 * @run main NewMultiArrayInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NewMultiArrayInstructionTest {
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
        NewMultiArrayInstruction i = NewMultiArrayInstruction.of(cpb.classEntry(ConstantDescs.CD_int.arrayType(2)), 2);
        check(i.opcode() == Opcode.MULTIANEWARRAY && i.dimensions() == 2 && i.sizeInBytes() == 4, "multianewarray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
