/*
 * @test
 * @summary java.lang.classfile.instruction.LabelTarget conformance: white-box structure + black-box behavior.
 * @run main LabelTargetTest
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

public class LabelTargetTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLBT" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LabelTarget");
        check(c != null, "java.lang.classfile.instruction.LabelTarget loads");
        check(c.getName().equals("java.lang.classfile.instruction.LabelTarget"), "binary name java.lang.classfile.instruction.LabelTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LabelTarget is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LabelTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LabelTarget has no superclass");
        check(declares(c, "label"), "declares label");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), 0, cob -> { Label l = cob.newLabel(); cob.iconst_1().ifeq(l).iconst_2().ireturn().labelBinding(l).iconst_3().ireturn(); }));
        CodeModel code = ClassFile.of().parse(b).methods().get(0).code().get();
        boolean found = false;
        for (CodeElement ce : code) { if (ce instanceof LabelTarget lt && lt.label() != null) found = true; }
        check(found, "LabelTarget present with label");

    }


    // ---- Part 3: 发射-回读（核心）：分支目标标签 vs 无目标标签 ----
    static void testEmitReadback() throws Exception {
        // 分支目标 → 解析模型含 LabelTarget，labelToBci 可定位
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLBT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label l = cob.newLabel();
            cob.iconst_0().iload(0).if_icmpge(l).iconst_1().ireturn().labelBinding(l).iconst_m1().ireturn();
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        LabelTarget lt = null;
        for (CodeElement e : ca.elementList()) if (e instanceof LabelTarget x) lt = x;
        check(lt != null, "LabelTarget present for branch target label");
        check(lt.label() != null, "label() non-null");
        check(ca.labelToBci(lt.label()) == 7, "labelToBci == 7 (after iconst_1+ireturn)");
        check(lt instanceof PseudoInstruction, "PseudoInstruction");
        check(!((Object) lt instanceof Instruction), "not Instruction");
        check(lt instanceof CodeElement, "CodeElement");
        // 多标签：每个分支目标一个 LabelTarget
        java.util.List<CodeElement> els = emit("multi", MethodTypeDesc.ofDescriptor("(I)I"), cob -> {
            Label a = cob.newLabel(), b2 = cob.newLabel();
            cob.iload(0).ifeq(a).iload(0).ifne(b2).iconst_0().ireturn();
            cob.labelBinding(a).iconst_1().ireturn();
            cob.labelBinding(b2).iconst_2().ireturn();
        });
        int n = 0;
        for (CodeElement e : els) if (e instanceof LabelTarget) n++;
        check(n == 2, "2 branch-target LabelTargets, got " + n);
        // 非目标标签：javap 语义下不产生 LabelTarget（解析期丢弃）
        els = emit("dead", MethodTypeDesc.ofDescriptor("()V"), cob -> {
            Label unused = cob.newLabel();
            cob.labelBinding(unused).return_();
        });
        n = 0;
        for (CodeElement e : els) if (e instanceof LabelTarget) n++;
        check(n == 0, "non-targeted bound label yields no LabelTarget in parse, got " + n);
        // for-each CodeModel 迭代与 elementList 一致
        int iterN = 0;
        CodeModel cm = ClassFile.of().parse(b).methods().get(0).code().get();
        for (CodeElement e : cm) iterN++;
        check(iterN == cm.elementList().size(), "CodeModel iterable == elementList size");
        // LabelTarget 无静态工厂（唯一访问器 label()）
        boolean hasOf = false;
        for (Method m : LabelTarget.class.getDeclaredMethods()) if (m.getName().equals("of")) hasOf = true;
        check(!hasOf, "LabelTarget has no of() factory (emission-only)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
