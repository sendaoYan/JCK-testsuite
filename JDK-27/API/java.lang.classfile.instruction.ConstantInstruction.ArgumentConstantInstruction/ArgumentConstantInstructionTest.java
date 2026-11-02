/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction conformance: behavioral.
 * @run main ArgumentConstantInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ArgumentConstantInstructionTest {
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
        ConstantInstruction.ArgumentConstantInstruction i = ConstantInstruction.ofArgument(Opcode.BIPUSH, 7);
        check(i.opcode() == Opcode.BIPUSH && i.constantValue() == 7, "argument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
