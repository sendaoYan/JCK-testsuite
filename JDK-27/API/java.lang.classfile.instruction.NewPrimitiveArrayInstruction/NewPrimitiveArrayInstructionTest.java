/*
 * @test
 * @summary java.lang.classfile.instruction.NewPrimitiveArrayInstruction conformance: behavioral.
 * @run main NewPrimitiveArrayInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NewPrimitiveArrayInstructionTest {
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
        NewPrimitiveArrayInstruction i = NewPrimitiveArrayInstruction.of(TypeKind.INT);
        check(i.opcode() == Opcode.NEWARRAY && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 2, "newarray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
