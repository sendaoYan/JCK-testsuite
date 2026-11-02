/*
 * @test
 * @summary java.lang.classfile.instruction.NewObjectInstruction conformance: behavioral.
 * @run main NewObjectInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NewObjectInstructionTest {
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
        NewObjectInstruction i = NewObjectInstruction.of(cpb.classEntry(ClassDesc.of("java.lang.String")));
        check(i.opcode() == Opcode.NEW && "java/lang/String".equals(i.className().asInternalName()) && i.sizeInBytes() == 3, "new");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
