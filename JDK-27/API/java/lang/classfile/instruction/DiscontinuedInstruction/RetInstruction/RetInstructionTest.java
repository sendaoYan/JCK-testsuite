/*
 * @test
 * @summary java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction conformance: white-box structure + black-box behavior.
 * @run main RetInstructionTest
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

public class RetInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmRTI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction");
        check(c != null, "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction"), "binary name java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        DiscontinuedInstruction.RetInstruction i = DiscontinuedInstruction.RetInstruction.of(Opcode.RET, 1);
        check(i.opcode() == Opcode.RET, "opcode");
        check(i.slot() == 1, "slot");

    }


    // ---- Part 3: v50 发射-回读（核心） ----
    static void testEmitReadbackV50() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmRTI"), cb -> cb
                .with(ClassFileVersion.of(50, 0)).withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC, cob -> {
                    Label sub = cob.newLabel(), cont = cob.newLabel();
                    cob.with(DiscontinuedInstruction.JsrInstruction.of(sub));
                    cob.labelBinding(cont).iconst_1().ireturn();
                    cob.labelBinding(sub).astore(1);
                    cob.with(DiscontinuedInstruction.RetInstruction.of(1));
                }));
        DiscontinuedInstruction.RetInstruction ret = null;
        for (CodeElement e : ClassFile.of().parse(b).methods().get(0).code().get().elementList())
            if (e instanceof DiscontinuedInstruction.RetInstruction x) ret = x;
        check(ret != null, "ret emitted at v50 + read back");
        check(ret.opcode() == Opcode.RET, "RET opcode");
        check(ret.sizeInBytes() == 2, "RET sizeInBytes 2");
        check(ret.slot() == 1, "RET slot 1 roundtrip");
        check(ret instanceof DiscontinuedInstruction, "instanceof DiscontinuedInstruction");
        check(ret instanceof Instruction && ret instanceof CodeElement, "Instruction+CodeElement");
        // RET_W（slot >= 256）
        byte[] b2 = ClassFile.of().build(ClassDesc.of("EmRTI2"), cb -> cb
                .with(ClassFileVersion.of(50, 0)).withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC, cob -> {
                    Label sub = cob.newLabel(), cont = cob.newLabel();
                    cob.with(DiscontinuedInstruction.JsrInstruction.of(sub));
                    cob.labelBinding(cont).iconst_1().ireturn();
                    cob.labelBinding(sub).astore(300);
                    cob.with(DiscontinuedInstruction.RetInstruction.of(300));
                }));
        for (CodeElement e : ClassFile.of().parse(b2).methods().get(0).code().get().elementList())
            if (e instanceof DiscontinuedInstruction.RetInstruction x) {
                check(x.opcode() == Opcode.RET_W, "slot 300 auto RET_W");
                check(x.slot() == 300, "RET_W slot 300");
                check(x.sizeInBytes() == 4, "RET_W sizeInBytes 4");
            }
        // v71 拒绝
        try {
            ClassFile.of().build(ClassDesc.of("EmRTI3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                    cob -> cob.with(DiscontinuedInstruction.RetInstruction.of(1))));
            check(false, "ret at v71 must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("ret"), "ret v71 IAE mentions ret");
        }
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        DiscontinuedInstruction.RetInstruction i = DiscontinuedInstruction.RetInstruction.of(3);
        check(i.opcode() == Opcode.RET, "of(3) RET");
        check(i.slot() == 3, "of(3) slot 3");
        check(i.sizeInBytes() == 2, "of(3) size 2");
        check(DiscontinuedInstruction.RetInstruction.of(300).opcode() == Opcode.RET_W, "of(300) RET_W");
        check(DiscontinuedInstruction.RetInstruction.of(300).slot() == 300, "of(300) slot");
        check(DiscontinuedInstruction.RetInstruction.of(0).opcode() == Opcode.RET, "of(0) RET boundary");
        check(DiscontinuedInstruction.RetInstruction.of(255).opcode() == Opcode.RET, "of(255) RET boundary");
        check(DiscontinuedInstruction.RetInstruction.of(256).opcode() == Opcode.RET_W, "of(256) RET_W boundary");
        check(DiscontinuedInstruction.RetInstruction.of(Opcode.RET_W, 7).opcode() == Opcode.RET_W, "of(RET_W,7) RET_W");
        try { DiscontinuedInstruction.RetInstruction.of(-1); check(false, "of(-1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(-1) IAE negative slot"); }
        try { DiscontinuedInstruction.RetInstruction.of((Opcode) null, 1); check(false, "of(null,1) must throw"); }
        catch (NullPointerException e) { check(true, "of((Opcode)null,1) NPE"); }
        try { DiscontinuedInstruction.RetInstruction.of(Opcode.NOP, 1); check(false, "of(NOP,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP,1) IAE wrong kind"); }
        try { DiscontinuedInstruction.RetInstruction.of(Opcode.JSR, 1); check(false, "of(JSR,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(JSR,1) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadbackV50();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
