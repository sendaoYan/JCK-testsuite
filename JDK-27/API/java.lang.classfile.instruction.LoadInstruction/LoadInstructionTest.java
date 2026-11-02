/*
 * @test
 * @summary java.lang.classfile.instruction.LoadInstruction conformance: behavioral.
 * @run main LoadInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LoadInstructionTest {
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
        LoadInstruction i = LoadInstruction.of(TypeKind.INT, 3);
        check(i.opcode() == Opcode.ILOAD_3 && i.slot() == 3 && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 1, "of(INT,3)");
        check(LoadInstruction.of(Opcode.ALOAD, 7).slot() == 7, "of(ALOAD,7)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
