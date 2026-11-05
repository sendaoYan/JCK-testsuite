/*
 * @test
 * @summary java.lang.classfile.Label conformance: white-box structure + black-box behavior.
 * @run main LabelTest
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

public class LabelTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Label");
        check(c != null, "java.lang.classfile.Label loads");
        check(c.getName().equals("java.lang.classfile.Label"), "binary name java.lang.classfile.Label");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Label is public");
        check(c.isInterface(), "java.lang.classfile.Label is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Label has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), 0, cob -> { Label l = cob.newLabel(); cob.iconst_1().ifeq(l).iconst_2().ireturn().labelBinding(l).iconst_3().ireturn(); }));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        int labelCount = 0;
        for (CodeElement ce : code) { if (ce instanceof LabelTarget lt && lt.label() != null) labelCount++; }
        check(labelCount == 1, "label appears as LabelTarget");

    }


    // ---- Part 3: newLabel 唯一性 + labelBinding 返回接收器 + Label 双重身份（实测 LabelImpl 同为实现 LabelTarget） ----
    static void testLabelIdentity() throws Exception {
        ClassFile cf = ClassFile.of();
        final Label[] l1 = new Label[1], l2 = new Label[1];
        final boolean[] sameBuilder = new boolean[1];
        cf.build(ClassDesc.of("pkg.LI"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> {
            l1[0] = c.newLabel();
            l2[0] = c.newLabel();
            sameBuilder[0] = (c.labelBinding(l1[0]) == c);
            c.return_();
        }));
        check(l1[0] != null && l2[0] != null, "newLabel returns labels");
        check(l1[0] != l2[0], "two newLabel() instances distinct (!=)");
        check(l1[0].equals(l1[0]), "label reflexive equals");
        check(!l1[0].equals(l2[0]), "distinct labels not equals");
        check(sameBuilder[0], "labelBinding returns the same CodeBuilder (fluent receiver)");
        // Label 是纯标记接口：无自身方法
        check(Label.class.getDeclaredMethods().length == 0, "Label declares no methods (marker)");
        // 实测：newLabel() 的实现对象同时是 LabelTarget（label 对象兼任自己的 LabelTarget）
        check(l1[0] instanceof LabelTarget, "newLabel() instance IS LabelTarget (impl-observable)");
        check(l1[0] instanceof CodeElement, "newLabel() instance IS CodeElement (via LabelTarget)");
        check(!Instruction.class.isInstance(l1[0]), "label NOT Instruction");
    }

    // ---- Part 4: 绑定语义：branch.target() 与 LabelTarget.label() 同一对象 + labelToBci ----
    static void testBindingReadback() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.LB"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label l = cob.newLabel();
            cob.iload(0).ifeq(l);
            cob.iconst_1().ireturn();
            cob.labelBinding(l).iconst_m1().ireturn();
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        CodeAttribute ca = (CodeAttribute) code;
        BranchInstruction br = null;
        Label target = null;
        int ltCount = 0;
        Label ltLabel = null;
        for (CodeElement e : code.elementList()) {
            if (e instanceof BranchInstruction bi) { br = bi; target = bi.target(); }
            if (e instanceof LabelTarget lt) { ltCount++; ltLabel = lt.label(); }
        }
        check(br != null && br.opcode() == Opcode.IFEQ, "IFEQ readback");
        check(br.sizeInBytes() == 3, "IFEQ sizeInBytes 3");
        check(br.target() != null, "branch target non-null");
        check(ltCount == 1, "exactly 1 LabelTarget: " + ltCount);
        // 关键身份契约：branch.target() 与目标处 LabelTarget.label() 是同一对象（同一模型内）
        check(target == ltLabel, "branch.target() == LabelTarget.label() (identity within model)");
        int bci = ca.labelToBci(target);
        check(bci >= 0 && bci < ca.codeLength(), "labelToBci in range [0,codeLength): " + bci + "/" + ca.codeLength());
        check(bci == 6, "target bci 6 (iload1+ifeq3+iconst1+ireturn1)");
        check(ca.labelToBci(ltLabel) == bci, "labelToBci consistency branch vs LabelTarget");
        check(cf.verify(cm).isEmpty(), "label build verify clean");
        // goto_/switch 目标同契约
        byte[] g = cf.build(ClassDesc.of("pkg.LB2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label end = cob.newLabel();
            cob.goto_(end);
            cob.iconst_1().ireturn();
            cob.labelBinding(end).iconst_0().ireturn();
        }));
        CodeModel gcode = cf.parse(g).methods().get(0).code().get();
        boolean gotoFound = false;
        for (CodeElement e : gcode.elementList()) if (Instruction.class.isInstance(e) && ((Instruction) e).opcode() == Opcode.GOTO) gotoFound = true;
        check(gotoFound, "GOTO emitted");
        check(cf.verify(cf.parse(g)).isEmpty(), "goto verify clean");
    }

    // ---- Part 5: 未绑定/死标签行为（实测精确异常） ----
    static void testUnboundAndDeadLabels() {
        ClassFile cf = ClassFile.of();
        // 未绑定：发射跳转但从不 labelBinding → IAE（即使 DROP_DEAD_LABELS，实测）
        for (ClassFile.DeadLabelsOption opt : new ClassFile.DeadLabelsOption[]{ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS, ClassFile.DeadLabelsOption.DROP_DEAD_LABELS}) {
            try {
                cf.withOptions(opt).build(ClassDesc.of("pkg.UB"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                        cob -> { Label l = cob.newLabel(); cob.iload(0).ifeq(l).iconst_1().ireturn(); }));
                check(false, "unbound label must throw under " + opt);
            } catch (IllegalArgumentException e) {
                String msg = e.getMessage();
                check(msg.startsWith("Detected branch target out of bytecode range"), opt + " unbound IAE msg first line ok");
            }
        }
        // 死标签：绑定但从未被引用 → build 成功，回读时 LabelTarget 被剔除（实测）
        byte[] db = cf.build(ClassDesc.of("pkg.DL"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label dead = cob.newLabel();
            cob.iload(0).iconst_1().iadd().ireturn();
            cob.labelBinding(dead);
        }));
        ClassModel dm = cf.parse(db);
        CodeModel dcode = dm.methods().get(0).code().get();
        int lt = 0;
        for (CodeElement e : dcode.elementList()) if (e instanceof LabelTarget) lt++;
        check(lt == 0, "dead (unreferenced) label's LabelTarget dropped on re-parse: " + lt);
        check(cf.verify(dm).isEmpty(), "dead label verify clean");
        // 同一 bci 绑定两个被引用的标签 → 回读只剩一个 LabelTarget（合并，实测）
        byte[] tb = cf.build(ClassDesc.of("pkg.CL"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label a = cob.newLabel(), bb = cob.newLabel();
            cob.iload(0).ifeq(a).iconst_1().ifeq(bb).iconst_2().ireturn();
            cob.labelBinding(a);
            cob.labelBinding(bb);
            cob.iconst_3().ireturn();
        }));
        CodeModel tcode = cf.parse(tb).methods().get(0).code().get();
        CodeAttribute tca = (CodeAttribute) tcode;
        int sameBciLt = 0;
        for (CodeElement e : tcode.elementList()) if (e instanceof LabelTarget) sameBciLt++;
        check(sameBciLt == 1, "two labels bound at same bci coalesce to 1 LabelTarget: " + sameBciLt);
        check(tca.labelToBci(((LabelTarget) tcode.elementList().stream().filter(e -> e instanceof LabelTarget).findFirst().get()).label()) >= 0, "coalesced labelToBci valid");
        check(cf.verify(cf.parse(tb)).isEmpty(), "coalesced verify clean");
    }

    // ---- Part 6: defineClass 运行闭环（分支方法真运行两路） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynLabel" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("sign", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            Label neg = cob.newLabel();
            cob.iload(0).iflt(neg);
            cob.iconst_1().ireturn();
            cob.labelBinding(neg).iconst_m1().ireturn();
        }));
        ClassModel rm = cf.parse(b);
        CodeAttribute ca = (CodeAttribute) rm.methods().get(0).code().get();
        Label tl = null;
        for (CodeElement e : rm.methods().get(0).code().get().elementList()) if (e instanceof BranchInstruction bi) tl = bi.target();
        check(ca.labelToBci(tl) > 0, "dyn branch target bci > 0");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("sign", int.class).invoke(null, 3).equals(1), "dyn sign(3)==1 正路");
        check(dc.getMethod("sign", int.class).invoke(null, -3).equals(-1), "dyn sign(-3)==-1 分支路");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLabelIdentity();
        testBindingReadback();
        testUnboundAndDeadLabels();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
