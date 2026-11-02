/*
 * @test
 * @summary java.lang.classfile.Instruction conformance: behavioral.
 * @run main InstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InstructionTest {
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
        Instruction i = ReturnInstruction.of(TypeKind.VOID);
        check(i.opcode() == Opcode.RETURN, "opcode");
        check(i.sizeInBytes() == 1, "sizeInBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
