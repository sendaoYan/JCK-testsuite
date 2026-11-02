/*
 * @test
 * @summary java.lang.classfile.instruction.BranchInstruction conformance: behavioral.
 * @run main BranchInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class BranchInstructionTest {
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
        check(BranchInstruction.class.isInterface(), "BranchInstruction is an interface");
        check(declares(BranchInstruction.class, "of") && declares(BranchInstruction.class, "target"), "declares of/target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
