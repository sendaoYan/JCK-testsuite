/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfAnnotation conformance: behavioral.
 * @run main OfAnnotationTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfAnnotationTest {
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
        AnnotationValue.OfAnnotation v = AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("java.lang.Deprecated")));
        check(ClassDesc.of("java.lang.Deprecated").equals(v.annotation().classSymbol()), "annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
