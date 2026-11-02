/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction conformance: behavioral.
 * @run main LoadConstantInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LoadConstantInstructionTest {
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
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ConstantInstruction.LoadConstantInstruction i = ConstantInstruction.ofLoad(Opcode.LDC, cpb.intEntry(9));
        check(i.opcode() == Opcode.LDC && Integer.valueOf(9).equals(i.constantValue()), "load");
        check(i.constantEntry() != null, "constantEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
