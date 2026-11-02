/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation conformance: behavioral.
 * @run main TypeAnnotationTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeAnnotationTest {
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
        TypeAnnotation.TargetInfo ti = TypeAnnotation.TargetInfo.ofField();
        TypeAnnotation.TypePathComponent tpc = TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.ARRAY, 0);
        Annotation ann = Annotation.of(ClassDesc.of("java.lang.Deprecated"));
        TypeAnnotation ta = TypeAnnotation.of(ti, List.of(tpc), ann);
        check(ta.targetInfo() == ti, "targetInfo");
        check(ta.targetPath().size() == 1, "targetPath");
        check(ClassDesc.of("java.lang.Deprecated").equals(ta.annotation().classSymbol()), "annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
