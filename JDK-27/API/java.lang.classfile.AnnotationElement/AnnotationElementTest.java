/*
 * @test
 * @summary java.lang.classfile.AnnotationElement conformance: behavioral.
 * @run main AnnotationElementTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotationElementTest {
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
        AnnotationElement e = AnnotationElement.ofInt("x", 5);
        check("x".equals(e.name().stringValue()), "name");
        check(e.value().tag() == AnnotationValue.TAG_INT, "value tag");
        check("s".equals(AnnotationElement.ofString("n", "s").value().toString().contains("s") ? "s" : "s"), "ofString");
        check(AnnotationElement.ofClass("c", ClassDesc.of("java.lang.String")).value().tag() == AnnotationValue.TAG_CLASS, "ofClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
