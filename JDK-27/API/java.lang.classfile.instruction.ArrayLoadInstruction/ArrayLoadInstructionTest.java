/*
 * @test
 * @summary java.lang.classfile.instruction.ArrayLoadInstruction conformance: behavioral.
 * @run main ArrayLoadInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ArrayLoadInstructionTest {
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
        ArrayLoadInstruction i = ArrayLoadInstruction.of(Opcode.IALOAD);
        check(i.opcode() == Opcode.IALOAD && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 1, "iaload");
        check(ArrayLoadInstruction.of(Opcode.AALOAD).typeKind() == TypeKind.REFERENCE, "aaload");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
