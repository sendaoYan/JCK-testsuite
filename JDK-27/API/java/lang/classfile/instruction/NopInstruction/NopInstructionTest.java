/*
 * @test
 * @summary java.lang.classfile.instruction.NopInstruction conformance: white-box structure + black-box behavior.
 * @run main NopInstructionTest
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

public class NopInstructionTest {
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

    // 触发 ConstantDescs 完整初始化，规避本 JDK 构建的 MethodTypeDesc.ofDescriptor 静态初始化环
    static final ClassDesc TOUCH = ConstantDescs.CD_Object;

    // 发射-回读辅助：build 唯一类名 + parse 回全部 CodeElement
    static java.util.List<CodeElement> emit(String m, MethodTypeDesc mtd, java.util.function.Consumer<CodeBuilder> body) throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNOP" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
        java.util.List<CodeElement> els = new ArrayList<>();
        for (MethodModel mm : ClassFile.of().parse(b).methods())
            if (mm.code().isPresent()) els.addAll(mm.code().get().elementList());
        return els;
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.NopInstruction");
        check(c != null, "java.lang.classfile.instruction.NopInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.NopInstruction"), "binary name java.lang.classfile.instruction.NopInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.NopInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.NopInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.NopInstruction has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        NopInstruction i = NopInstruction.of();
        check(i.opcode() == Opcode.NOP, "opcode NOP");
        check(i.sizeInBytes() == 1, "size 1");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", ConstantDescs.MTD_void, cob -> cob.nop().nop().nop().return_());
        int n = 0;
        for (CodeElement e : els) if (e instanceof NopInstruction x) {
            n++;
            check(x.opcode() == Opcode.NOP, "NOP opcode");
            check(x.sizeInBytes() == 1, "NOP sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.NOP, "kind NOP");
            check(x instanceof Instruction && x instanceof CodeElement, "Instruction+CodeElement");
            check(!((Object) x instanceof PseudoInstruction), "not PseudoInstruction");
        }
        check(n == 3, "3 nops emitted, got " + n);
        // NOP 指令在 CodeAttribute 中位置正确（bci 0,1,2）
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNOP2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                cob -> cob.nop().nop().return_()));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        check(ca.codeLength() == 3, "codeLength 3 (2 nop + return)");
        check(ca.codeArray().length == 3, "codeArray length 3");
        check((ca.codeArray()[0] & 0xff) == Opcode.NOP.bytecode(), "byte 0 is NOP");
        check((ca.codeArray()[1] & 0xff) == Opcode.NOP.bytecode(), "byte 1 is NOP");
        check((ca.codeArray()[2] & 0xff) == Opcode.RETURN.bytecode(), "byte 2 is RETURN");
    }

    // ---- Part 4: 工厂 ----
    static void testFactory() {
        NopInstruction i = NopInstruction.of();
        check(i.opcode() == Opcode.NOP, "of() NOP");
        check(i.sizeInBytes() == 1, "of() size 1");
        check(NopInstruction.of() instanceof NopInstruction, "of() type");
        check(Opcode.NOP.bytecode() == 0, "NOP bytecode 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
