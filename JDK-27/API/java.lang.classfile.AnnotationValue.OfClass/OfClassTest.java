/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfClass conformance: behavioral.
 * @run main OfClassTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OfClassTest {
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
        AnnotationValue.OfClass v = AnnotationValue.ofClass(ClassDesc.of("java.lang.String"));
        check(ClassDesc.of("java.lang.String").equals(v.classSymbol()), "classSymbol");
        check("Ljava/lang/String;".equals(v.className().stringValue()), "className");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
