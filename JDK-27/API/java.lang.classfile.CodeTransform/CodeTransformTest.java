/*
 * @test
 * @summary java.lang.classfile.CodeTransform conformance: behavioral.
 * @run main CodeTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class CodeTransformTest {
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
        check(CodeTransform.ACCEPT_ALL != null, "ACCEPT_ALL");
        check(CodeTransform.endHandler(cb -> {}) != null, "endHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
