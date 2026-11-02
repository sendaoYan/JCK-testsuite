/*
 * @test
 * @summary java.lang.classfile.instruction.ReturnInstruction conformance: behavioral.
 * @run main ReturnInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ReturnInstructionTest {
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
        ReturnInstruction i = ReturnInstruction.of(TypeKind.VOID);
        check(i.opcode() == Opcode.RETURN && i.typeKind() == TypeKind.VOID && i.sizeInBytes() == 1, "of(VOID)");
        check(ReturnInstruction.of(Opcode.IRETURN).typeKind() == TypeKind.INT, "of(IRETURN)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
