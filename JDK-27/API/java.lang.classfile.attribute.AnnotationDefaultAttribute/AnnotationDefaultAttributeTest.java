/*
 * @test
 * @summary java.lang.classfile.attribute.AnnotationDefaultAttribute conformance: behavioral.
 * @run main AnnotationDefaultAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotationDefaultAttributeTest {
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
        AnnotationDefaultAttribute a = AnnotationDefaultAttribute.of(AnnotationValue.ofInt(9));
        check(a.defaultValue().tag() == AnnotationValue.TAG_INT, "defaultValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
