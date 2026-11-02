/*
 * @test
 * @summary java.lang.classfile.instruction.ConvertInstruction conformance: behavioral.
 * @run main ConvertInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ConvertInstructionTest {
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
        ConvertInstruction i = ConvertInstruction.of(TypeKind.INT, TypeKind.LONG);
        check(i.opcode() == Opcode.I2L && i.fromType() == TypeKind.INT && i.toType() == TypeKind.LONG && i.sizeInBytes() == 1, "of(INT,LONG)");
        check(ConvertInstruction.of(Opcode.D2I).fromType() == TypeKind.DOUBLE, "of(D2I)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
