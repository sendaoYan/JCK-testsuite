/*
 * @test
 * @summary java.lang.classfile.instruction.IncrementInstruction conformance: behavioral.
 * @run main IncrementInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class IncrementInstructionTest {
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
        IncrementInstruction i = IncrementInstruction.of(1, 2);
        check(i.opcode() == Opcode.IINC && i.slot() == 1 && i.constant() == 2 && i.sizeInBytes() == 3, "of(1,2)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
