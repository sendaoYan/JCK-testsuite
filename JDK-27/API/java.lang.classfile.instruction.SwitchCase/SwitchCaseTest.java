/*
 * @test
 * @summary java.lang.classfile.instruction.SwitchCase conformance: behavioral.
 * @run main SwitchCaseTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SwitchCaseTest {
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
        check(SwitchCase.class.isInterface(), "is interface");
        check(declares(SwitchCase.class, "of") && declares(SwitchCase.class, "caseValue"), "declares of/caseValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
