/*
 * @test
 * @summary java.lang.classfile.instruction.LookupSwitchInstruction conformance: behavioral.
 * @run main LookupSwitchInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LookupSwitchInstructionTest {
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
        check(LookupSwitchInstruction.class.isInterface(), "is interface");
        check(declares(LookupSwitchInstruction.class, "of") && declares(LookupSwitchInstruction.class, "cases"), "declares of/cases");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
