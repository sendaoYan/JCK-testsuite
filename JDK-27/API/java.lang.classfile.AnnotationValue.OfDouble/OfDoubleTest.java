/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfDouble conformance: behavioral.
 * @run main OfDoubleTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfDoubleTest {
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
        AnnotationValue.OfDouble v = AnnotationValue.ofDouble(2.5);
        check(v.doubleValue() == 2.5, "doubleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
