/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfInt conformance: behavioral.
 * @run main OfIntTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfIntTest {
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
        AnnotationValue.OfInt v = AnnotationValue.ofInt(5);
        check(v.intValue() == 5, "intValue");
        check(v.constant().intValue() == 5, "constant");
        check(Integer.valueOf(5).equals(v.resolvedValue()), "resolvedValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
