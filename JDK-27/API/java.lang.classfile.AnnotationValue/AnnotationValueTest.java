/*
 * @test
 * @summary java.lang.classfile.AnnotationValue conformance: white-box structure + black-box behavior.
 * @run main AnnotationValueTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

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
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue");
        check(c != null, "java.lang.classfile.AnnotationValue loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue"), "binary name java.lang.classfile.AnnotationValue");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue has no superclass");
        check(declares(c, "tag"), "declares tag");
        check(declares(c, "ofEnum"), "declares ofEnum");
        check(declares(c, "ofClass"), "declares ofClass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(AnnotationValue.TAG_INT == 73 && AnnotationValue.TAG_BYTE == 66 && AnnotationValue.TAG_CHAR == 67 && AnnotationValue.TAG_BOOLEAN == 90, "tag constants (ASCII)");
        check(AnnotationValue.TAG_STRING == 115 && AnnotationValue.TAG_ENUM == 101 && AnnotationValue.TAG_CLASS == 99 && AnnotationValue.TAG_ANNOTATION == 64 && AnnotationValue.TAG_ARRAY == 91, "tag constants 2");
        check(AnnotationValue.ofInt(5).tag() == AnnotationValue.TAG_INT, "ofInt tag");
        check(AnnotationValue.ofString("s").tag() == AnnotationValue.TAG_STRING, "ofString tag");
        check(AnnotationValue.ofBoolean(true).tag() == AnnotationValue.TAG_BOOLEAN, "ofBoolean tag");
        check(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")).tag() == AnnotationValue.TAG_CLASS, "ofClass tag");
        check(AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME").tag() == AnnotationValue.TAG_ENUM, "ofEnum tag");
        check(AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2)).tag() == AnnotationValue.TAG_ARRAY, "ofArray tag");
        check(AnnotationValue.of(5).tag() == AnnotationValue.TAG_INT, "of(Object) box int");
        check(AnnotationValue.of("s").tag() == AnnotationValue.TAG_STRING, "of(Object) string");
        check(AnnotationValue.of(true).tag() == AnnotationValue.TAG_BOOLEAN, "of(Object) boolean");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
