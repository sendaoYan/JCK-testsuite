/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute conformance: behavioral.
 * @run main RuntimeInvisibleParameterAnnotationsAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class RuntimeInvisibleParameterAnnotationsAttributeTest {
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
        Annotation ann = Annotation.of(ClassDesc.of("java.lang.Deprecated"));
        RuntimeInvisibleParameterAnnotationsAttribute a = RuntimeInvisibleParameterAnnotationsAttribute.of(List.of(List.of(ann)));
        check(a.parameterAnnotations().size() == 1, "parameterAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
