/*
 * @test
 * @summary java.lang.classfile.ClassTransform conformance: behavioral.
 * @run main ClassTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassTransformTest {
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
        check(ClassTransform.ACCEPT_ALL != null, "ACCEPT_ALL");
        check(ClassTransform.dropping(e -> false) != null, "dropping");
        check(ClassTransform.transformingMethods(MethodTransform.ACCEPT_ALL) != null, "transformingMethods");
        check(ClassTransform.transformingFields(FieldTransform.ACCEPT_ALL) != null, "transformingFields");
        check(ClassTransform.endHandler(cb -> {}) != null, "endHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
