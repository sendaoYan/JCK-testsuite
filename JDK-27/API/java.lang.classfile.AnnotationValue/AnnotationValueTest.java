/*
 * @test
 * @summary java.lang.classfile.AnnotationValue conformance: behavioral.
 * @run main AnnotationValueTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotationValueTest {
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
        check(AnnotationValue.TAG_INT == 73 && AnnotationValue.TAG_STRING == 115 && AnnotationValue.TAG_ARRAY == 91, "tag constants");
        check(AnnotationValue.ofInt(5).tag() == AnnotationValue.TAG_INT, "ofInt tag");
        check(AnnotationValue.ofString("s").tag() == AnnotationValue.TAG_STRING, "ofString tag");
        check(AnnotationValue.ofBoolean(true).tag() == AnnotationValue.TAG_BOOLEAN, "ofBoolean tag");
        check(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")).tag() == AnnotationValue.TAG_CLASS, "ofClass tag");
        check(AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME").tag() == AnnotationValue.TAG_ENUM, "ofEnum tag");
        check(AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2)).tag() == AnnotationValue.TAG_ARRAY, "ofArray tag");
        check(AnnotationValue.of(5).tag() == AnnotationValue.TAG_INT, "of(Object) tag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
