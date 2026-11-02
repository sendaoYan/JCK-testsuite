/*
 * @test
 * @summary java.lang.classfile.instruction.MonitorInstruction conformance: behavioral.
 * @run main MonitorInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MonitorInstructionTest {
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
        MonitorInstruction i = MonitorInstruction.of(Opcode.MONITORENTER);
        check(i.opcode() == Opcode.MONITORENTER && i.sizeInBytes() == 1, "monitorenter");
        check(MonitorInstruction.of(Opcode.MONITOREXIT).opcode() == Opcode.MONITOREXIT, "monitorexit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
