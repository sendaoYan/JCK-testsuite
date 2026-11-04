/*
 * @test
 * @summary java.lang.classfile.MethodBuilder conformance: white-box structure + black-box behavior.
 * @run main MethodBuilderTest
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

public class MethodBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodBuilder");
        check(c != null, "java.lang.classfile.MethodBuilder loads");
        check(c.getName().equals("java.lang.classfile.MethodBuilder"), "binary name java.lang.classfile.MethodBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodBuilder is public");
        check(c.isInterface(), "java.lang.classfile.MethodBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodBuilder has no superclass");
        check(declares(c, "withFlags"), "declares withFlags");
        check(declares(c, "withCode"), "declares withCode");
        check(declares(c, "transformCode"), "declares transformCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethod("m", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, mb -> mb.withCode(cob -> cob.iload(0).iconst_1().iadd().ireturn()).withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC)));
        ClassModel cm = ClassFile.of().parse(b);
        MethodModel m = cm.methods().get(0);
        check(m.methodName().stringValue().equals("m"), "name");
        check(m.methodTypeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int)), "type symbol");
        check(m.flags().has(AccessFlag.PUBLIC) && m.flags().has(AccessFlag.STATIC), "flags");
        check(m.code().isPresent(), "code present");
        check(m.code().get().elementList().size() >= 3, "code instructions");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
