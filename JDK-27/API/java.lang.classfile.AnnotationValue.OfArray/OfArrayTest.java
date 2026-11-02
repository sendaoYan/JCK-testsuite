/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfArray conformance: behavioral.
 * @run main OfArrayTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfArrayTest {
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
        AnnotationValue.OfArray v = AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2));
        check(v.values().size() == 2, "values size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
