/*
 * @test
 * @summary java.lang.classfile.CodeBuilder conformance: white-box structure + black-box behavior.
 * @run main CodeBuilderTest
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

public class CodeBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder"), "binary name java.lang.classfile.CodeBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeBuilder has no superclass");
        check(declares(c, "newLabel"), "declares newLabel");
        check(declares(c, "startLabel"), "declares startLabel");
        check(declares(c, "endLabel"), "declares endLabel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            cob.iconst_0().istore(1).iload(1).iconst_1().iadd().istore(1).iload(1).ireturn();
        }));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        int n = 0;
        for (CodeElement ce : code) { if (ce instanceof Instruction i && i.opcode() == Opcode.ISTORE_1) n++; }
        check(n == 2, "istore emitted");
        check(code.elementList().stream().anyMatch(e -> e instanceof Instruction i && i.opcode() == Opcode.IRETURN), "ireturn");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
