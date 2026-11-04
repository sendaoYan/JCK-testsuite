/*
 * @test
 * @summary java.lang.classfile.MethodTransform conformance: white-box structure + black-box behavior.
 * @run main MethodTransformTest
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

public class MethodTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodTransform");
        check(c != null, "java.lang.classfile.MethodTransform loads");
        check(c.getName().equals("java.lang.classfile.MethodTransform"), "binary name java.lang.classfile.MethodTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodTransform is public");
        check(c.isInterface(), "java.lang.classfile.MethodTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodTransform has no superclass");
        check(declares(c, "ofStateful"), "declares ofStateful");
        check(declares(c, "endHandler"), "declares endHandler");
        check(declares(c, "dropping"), "declares dropping");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] src = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("a", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()).withMethodBody("b", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()));
        ClassModel cm = ClassFile.of().parse(src);
        byte[] out = ClassFile.of().transformClass(cm, ClassTransform.transformingMethods(me -> me.methodName().stringValue().equals("a"), MethodTransform.dropping(e -> e instanceof CodeModel)));
        ClassModel cm2 = ClassFile.of().parse(out);
        check(cm2.methods().size() == 2, "two methods retained");
        check(cm2.methods().stream().anyMatch(m -> m.methodName().stringValue().equals("a") && m.code().isEmpty()), "a code dropped");
        check(cm2.methods().stream().anyMatch(m -> m.methodName().stringValue().equals("b") && m.code().isPresent()), "b code retained");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
