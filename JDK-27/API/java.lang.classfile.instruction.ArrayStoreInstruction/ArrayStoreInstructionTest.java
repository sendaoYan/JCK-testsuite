/*
 * @test
 * @summary java.lang.classfile.instruction.ArrayStoreInstruction conformance: behavioral.
 * @run main ArrayStoreInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ArrayStoreInstructionTest {
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
        ArrayStoreInstruction i = ArrayStoreInstruction.of(Opcode.IASTORE);
        check(i.opcode() == Opcode.IASTORE && i.typeKind() == TypeKind.INT && i.sizeInBytes() == 1, "iastore");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
