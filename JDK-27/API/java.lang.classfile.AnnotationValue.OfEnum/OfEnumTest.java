/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfEnum conformance: behavioral.
 * @run main OfEnumTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfEnumTest {
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
        AnnotationValue.OfEnum v = AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME");
        check(ClassDesc.of("java.lang.annotation.RetentionPolicy").equals(v.classSymbol()), "classSymbol");
        check("RUNTIME".equals(v.constantName().stringValue()), "constantName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
