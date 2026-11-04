/*
 * @test
 * @summary java.lang.classfile.Annotation conformance: white-box structure + black-box behavior.
 * @run main AnnotationTest
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
        Class<?> c = Class.forName("java.lang.classfile.Annotation");
        check(c != null, "java.lang.classfile.Annotation loads");
        check(c.getName().equals("java.lang.classfile.Annotation"), "binary name java.lang.classfile.Annotation");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Annotation is public");
        check(c.isInterface(), "java.lang.classfile.Annotation is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Annotation has no superclass");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classSymbol"), "declares classSymbol");
        check(declares(c, "elements"), "declares elements");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Annotation a = Annotation.of(ClassDesc.of("java.lang.Deprecated"), AnnotationElement.ofBoolean("forRemoval", false));
        check(a.className().stringValue().equals("Ljava/lang/Deprecated;"), "className descriptor");
        check(a.classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "classSymbol");
        check(a.elements().size() == 1, "one element");
        check(a.elements().get(0).name().stringValue().equals("forRemoval"), "element name");
        Annotation a2 = Annotation.of(ClassDesc.of("java.lang.SuppressWarnings"), AnnotationElement.ofString("value", "unchecked"));
        check(a2.classSymbol().equals(ClassDesc.of("java.lang.SuppressWarnings")), "SuppressWarnings");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
