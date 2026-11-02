/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfFloat conformance: behavioral.
 * @run main OfFloatTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfFloatTest {
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
        AnnotationValue.OfFloat v = AnnotationValue.ofFloat(1.5f);
        check(Float.floatToIntBits(v.floatValue()) == Float.floatToIntBits(1.5f), "floatValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
