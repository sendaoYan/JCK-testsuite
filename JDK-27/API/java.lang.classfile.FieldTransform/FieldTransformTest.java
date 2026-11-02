/*
 * @test
 * @summary java.lang.classfile.FieldTransform conformance: behavioral.
 * @run main FieldTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FieldTransformTest {
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
        check(FieldTransform.ACCEPT_ALL != null, "ACCEPT_ALL");
        check(FieldTransform.dropping(e -> false) != null, "dropping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
