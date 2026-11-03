/*
 * @test
 * @summary java.lang.classfile.MethodSignature conformance: white-box structure + black-box behavior.
 * @run main MethodSignatureTest
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

public class MethodSignatureTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodSignature");
        check(c != null, "java.lang.classfile.MethodSignature loads");
        check(c.getName().equals("java.lang.classfile.MethodSignature"), "binary name java.lang.classfile.MethodSignature");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodSignature is public");
        check(c.isInterface(), "java.lang.classfile.MethodSignature is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodSignature has no superclass");
        check(declares(c, "typeParameters"), "declares typeParameters");
        check(declares(c, "arguments"), "declares arguments");
        check(declares(c, "result"), "declares result");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        MethodSignature ms = MethodSignature.parseFrom("(Ljava/lang/String;)I");
        check(ms.typeParameters().isEmpty(), "no type params");
        check(ms.arguments().size() == 1, "one argument");
        check(ms.arguments().get(0).signatureString().equals("Ljava/lang/String;"), "arg String");
        check(ms.result().signatureString().equals("I"), "result int");
        check(ms.throwableSignatures().isEmpty(), "no throws");
        MethodSignature ms2 = MethodSignature.of(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String));
        check(ms2.arguments().size() == 1 && ms2.result().signatureString().equals("I"), "of(MethodTypeDesc)");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
