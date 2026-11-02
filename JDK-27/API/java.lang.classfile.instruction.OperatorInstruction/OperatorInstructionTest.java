/*
 * @test
 * @summary java.lang.classfile.instruction.OperatorInstruction conformance: behavioral.
 * @run main OperatorInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OperatorInstructionTest {
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
        OperatorInstruction i = OperatorInstruction.of(Opcode.IADD);
        check(i.opcode() == Opcode.IADD && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 1, "iadd");
        check(OperatorInstruction.of(Opcode.DSUB).typeKind() == TypeKind.DOUBLE, "dsub");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
