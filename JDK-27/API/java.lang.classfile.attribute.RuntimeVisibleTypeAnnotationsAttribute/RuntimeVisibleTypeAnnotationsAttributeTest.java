/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute conformance: behavioral.
 * @run main RuntimeVisibleTypeAnnotationsAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class RuntimeVisibleTypeAnnotationsAttributeTest {
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
        TypeAnnotation ta = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), List.of(), Annotation.of(ClassDesc.of("java.lang.Deprecated")));
        RuntimeVisibleTypeAnnotationsAttribute a = RuntimeVisibleTypeAnnotationsAttribute.of(ta);
        check(a.annotations().size() == 1, "annotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
