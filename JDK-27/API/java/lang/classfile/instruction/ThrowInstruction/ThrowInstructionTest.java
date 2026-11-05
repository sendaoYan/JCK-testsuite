/*
 * @test
 * @summary java.lang.classfile.instruction.ThrowInstruction conformance: white-box structure + black-box behavior.
 * @run main ThrowInstructionTest
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

public class ThrowInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmTHR" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ThrowInstruction");
        check(c != null, "java.lang.classfile.instruction.ThrowInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ThrowInstruction"), "binary name java.lang.classfile.instruction.ThrowInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ThrowInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ThrowInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ThrowInstruction has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ThrowInstruction i = ThrowInstruction.of();
        check(i.opcode() == Opcode.ATHROW, "opcode ATHROW");
        check(i.sizeInBytes() == 1, "size 1");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Exception;)V"),
                cob -> cob.aload(0).athrow());
        ThrowInstruction i = null;
        for (CodeElement e : els) if (e instanceof ThrowInstruction x) i = x;
        check(i != null, "athrow emitted + read back");
        check(i.opcode() == Opcode.ATHROW, "ATHROW opcode");
        check(i.sizeInBytes() == 1, "ATHROW sizeInBytes 1");
        check(i.opcode().kind() == Opcode.Kind.THROW_EXCEPTION, "kind THROW_EXCEPTION");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        check(!((Object) i instanceof PseudoInstruction), "not PseudoInstruction");
        // new + dup + invokespecial + athrow 完整抛出序列
        els = emit("full", ConstantDescs.MTD_void, cob -> cob
                .new_(ClassDesc.of("java.lang.IllegalStateException")).dup()
                .invokespecial(ClassDesc.of("java.lang.IllegalStateException"), "<init>", MethodTypeDesc.ofDescriptor("()V"))
                .athrow());
        int n = 0;
        for (CodeElement e : els) if (e instanceof ThrowInstruction) n++;
        check(n == 1, "full throw sequence has 1 ATHROW");
        // javac 真产物：throwHelper 中的 athrow
        byte[] self = ThrowInstructionTest.class.getResourceAsStream("ThrowInstructionTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.methodName().stringValue().equals("throwHelper") && mm.code().isPresent())
                for (CodeElement e : mm.code().get().elementList())
                    if (e instanceof ThrowInstruction x && x.opcode() == Opcode.ATHROW) found = true;
        check(found, "javac throw emits ATHROW");
        // CodeBuilder 方法实名是 athrow()
        boolean hasAthrow = false;
        for (Method m : CodeBuilder.class.getMethods()) if (m.getName().equals("athrow")) hasAthrow = true;
        check(hasAthrow, "CodeBuilder.athrow() exists");
    }

    // ---- Part 4: 工厂 ----
    static void testFactory() {
        ThrowInstruction i = ThrowInstruction.of();
        check(i != null, "of() non-null");
        check(i.opcode() == Opcode.ATHROW, "of() opcode");
        check(i.sizeInBytes() == 1, "of() size 1");
        check(Opcode.ATHROW.bytecode() == 191, "ATHROW bytecode 191");
        check(ThrowInstruction.of() instanceof ThrowInstruction, "of() type");
    }

    static void throwHelper() { throw new IllegalStateException("boom"); }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
