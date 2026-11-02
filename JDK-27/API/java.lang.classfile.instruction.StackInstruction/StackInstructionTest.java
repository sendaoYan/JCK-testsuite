/*
 * @test
 * @summary java.lang.classfile.instruction.StackInstruction conformance: behavioral.
 * @run main StackInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class StackInstructionTest {
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
        StackInstruction i = StackInstruction.of(Opcode.POP);
        check(i.opcode() == Opcode.POP && i.sizeInBytes() == 1, "pop");
        check(StackInstruction.of(Opcode.DUP2).opcode() == Opcode.DUP2, "dup2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
