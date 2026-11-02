/*
 * @test
 * @summary java.lang.classfile.Annotation conformance: behavioral.
 * @run main AnnotationTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotationTest {
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
        Annotation a = Annotation.of(ClassDesc.of("java.lang.Deprecated"), AnnotationElement.ofInt("x", 5));
        check(ClassDesc.of("java.lang.Deprecated").equals(a.classSymbol()), "classSymbol");
        check(a.elements().size() == 1, "elements");
        check("Ljava/lang/Deprecated;".equals(a.className().stringValue()), "className");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
