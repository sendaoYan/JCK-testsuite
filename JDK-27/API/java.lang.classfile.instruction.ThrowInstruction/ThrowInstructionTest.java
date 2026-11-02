/*
 * @test
 * @summary java.lang.classfile.instruction.ThrowInstruction conformance: behavioral.
 * @run main ThrowInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ThrowInstructionTest {
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
        ThrowInstruction i = ThrowInstruction.of();
        check(i.opcode() == Opcode.ATHROW && i.sizeInBytes() == 1, "athrow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
