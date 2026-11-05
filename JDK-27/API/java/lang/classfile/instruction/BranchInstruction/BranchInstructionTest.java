/*
 * @test
 * @summary java.lang.classfile.instruction.BranchInstruction conformance: white-box structure + black-box behavior.
 * @run main BranchInstructionTest
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

public class BranchInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmBRI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.BranchInstruction");
        check(c != null, "java.lang.classfile.instruction.BranchInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.BranchInstruction"), "binary name java.lang.classfile.instruction.BranchInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.BranchInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.BranchInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.BranchInstruction has no superclass");
        check(declares(c, "target"), "declares target");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label l = cob.newLabel();
            br[0] = BranchInstruction.of(Opcode.GOTO, l);
            cob.labelBinding(l).return_();
        }));
        BranchInstruction bi = br[0];
        check(bi != null, "of created");
        check(bi.opcode() == Opcode.GOTO, "opcode");
        check(bi.target() != null, "target non-null");

    }


    // ---- Part 3: 发射-回读矩阵（核心） ----
    static void testEmitReadback() throws Exception {
        // if_icmpge：两 int 入栈 → 条件分支 → 双出口
        byte[] b = ClassFile.of().build(ClassDesc.of("EmBRI"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label l = cob.newLabel();
            cob.iconst_0().iload(0).if_icmpge(l).iconst_1().ireturn().labelBinding(l).iconst_m1().ireturn();
        }));
        MethodModel mm = ClassFile.of().parse(b).methods().get(0);
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        BranchInstruction cond = null;
        for (CodeElement e : ca.elementList()) if (e instanceof BranchInstruction x && x.opcode() == Opcode.IF_ICMPGE) cond = x;
        check(cond != null, "if_icmpge emitted + read back");
        check(cond.opcode() == Opcode.IF_ICMPGE, "opcode IF_ICMPGE");
        check(cond.sizeInBytes() == 3, "conditional sizeInBytes 3");
        check(cond.opcode().kind() == Opcode.Kind.BRANCH, "IF_ICMPGE kind BRANCH");
        check(cond.target() != null, "target non-null");
        check(ca.labelToBci(cond.target()) == 7, "target bci 7 (skip ICONST_1+IRETURN)");
        check(cond instanceof Instruction && cond instanceof CodeElement, "Instruction+CodeElement");
        // goto / goto_w
        java.util.List<CodeElement> els = emit("g", ConstantDescs.MTD_void, cob -> {
            Label l = cob.newLabel();
            cob.goto_(l).labelBinding(l).return_();
        });
        BranchInstruction gt = null;
        for (CodeElement e : els) if (e instanceof BranchInstruction x) gt = x;
        check(gt != null && gt.opcode() == Opcode.GOTO && gt.sizeInBytes() == 3, "goto_ emits GOTO size 3");
        els = emit("gw", ConstantDescs.MTD_void, cob -> {
            Label l = cob.newLabel();
            cob.goto_w(l).labelBinding(l).return_();
        });
        for (CodeElement e : els) if (e instanceof BranchInstruction x) check(x.opcode() == Opcode.GOTO_W && x.sizeInBytes() == 5, "goto_w emits GOTO_W size 5");
        // 一元条件 + 引用条件族
        els = emit("u", MethodTypeDesc.ofDescriptor("(ILjava/lang/Object;Ljava/lang/Object;)I"), cob -> {
            Label a = cob.newLabel();
            cob.iload(0).ifeq(a);
            cob.iload(0).ifne(a);
            cob.aload(1).ifnull(a);
            cob.aload(1).ifnonnull(a);
            cob.aload(1).aload(2).if_acmpeq(a);
            cob.aload(1).aload(2).if_acmpne(a);
            cob.iconst_0().ireturn();
            cob.labelBinding(a).iconst_1().ireturn();
        });
        java.util.List<Opcode> brs = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof BranchInstruction x) brs.add(x.opcode());
        check(brs.equals(java.util.List.of(Opcode.IFEQ, Opcode.IFNE, Opcode.IFNULL, Opcode.IFNONNULL, Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE)),
                "unary/ref branch family " + brs);
        // 多分支同目标
        els = emit("same", MethodTypeDesc.ofDescriptor("(I)I"), cob -> {
            Label shared = cob.newLabel(), out = cob.newLabel();
            cob.iload(0).ifeq(shared);
            cob.iload(0).ifne(shared);
            cob.labelBinding(out).iconst_0().ireturn();
            cob.labelBinding(shared).iconst_0().ireturn();
        });
        int cnt = 0;
        Label firstT = null;
        CodeAttribute ca2;
        java.util.List<Label> tg = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof BranchInstruction x) { cnt++; tg.add(x.target()); }
        check(cnt == 2, "two branches emitted");
        check(tg.get(0) == tg.get(1), "both targets same label object");
        // int 比较全族
        els = emit("cmp", MethodTypeDesc.ofDescriptor("(II)I"), cob -> {
            Label l1 = cob.newLabel();
            cob.iload(0).iload(1).if_icmplt(l1);
            cob.iload(0).iload(1).if_icmpge(l1);
            cob.iload(0).iload(1).if_icmpgt(l1);
            cob.iload(0).iload(1).if_icmple(l1);
            cob.iload(0).iload(1).if_icmpeq(l1);
            cob.iload(0).iload(1).if_icmpne(l1);
            cob.iconst_0().ireturn();
            cob.labelBinding(l1).iconst_1().ireturn();
        });
        java.util.List<Opcode> cmps = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof BranchInstruction x) cmps.add(x.opcode());
        check(cmps.equals(java.util.List.of(Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE)),
                "int compare family " + cmps);
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] l = new Label[1];
        ClassFile.of().build(ClassDesc.of("EmBRI2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            BranchInstruction bi = BranchInstruction.of(Opcode.GOTO, l[0]);
            check(bi.opcode() == Opcode.GOTO, "of(GOTO,l) opcode");
            check(bi.target() == l[0], "of target identity ==");
            check(bi.sizeInBytes() == 3, "of(GOTO,l) size 3");
            check(BranchInstruction.of(Opcode.GOTO_W, l[0]).opcode() == Opcode.GOTO_W, "of(GOTO_W,l)");
            check(BranchInstruction.of(Opcode.IFNE, l[0]).opcode() == Opcode.IFNE, "of(IFNE,l)");
            cob.return_();
        }));
        try { BranchInstruction.of(null, l[0]); check(false, "of(null,l) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,l) NPE"); }
        try { BranchInstruction.of(Opcode.GOTO, null); check(false, "of(GOTO,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(GOTO,null) NPE"); }
        try { BranchInstruction.of(Opcode.NOP, l[0]); check(false, "of(NOP,l) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP,l) IAE wrong kind"); }
        try { BranchInstruction.of(Opcode.IADD, l[0]); check(false, "of(IADD,l) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD,l) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
