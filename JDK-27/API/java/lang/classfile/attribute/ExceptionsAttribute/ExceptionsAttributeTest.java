/*
 * @test
 * @summary java.lang.classfile.attribute.ExceptionsAttribute conformance: white-box structure + black-box behavior.
 * @run main ExceptionsAttributeTest
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

public class ExceptionsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ExceptionsAttribute");
        check(c != null, "java.lang.classfile.attribute.ExceptionsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ExceptionsAttribute"), "binary name java.lang.classfile.attribute.ExceptionsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ExceptionsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ExceptionsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ExceptionsAttribute has no superclass");
        check(declares(c, "exceptions"), "declares exceptions");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofSymbols"), "declares ofSymbols");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ExceptionsAttribute a = ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.RuntimeException"));
        check(a.exceptions().size() == 2, "size");
        check(a.exceptions().get(0).asInternalName().equals("java/io/IOException"), "IOException");
        check(a.exceptions().get(1).asSymbol().equals(ClassDesc.of("java.lang.RuntimeException")), "RuntimeException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
