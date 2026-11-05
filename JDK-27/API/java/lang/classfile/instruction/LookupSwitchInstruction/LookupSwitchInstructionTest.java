/*
 * @test
 * @summary java.lang.classfile.instruction.LookupSwitchInstruction conformance: white-box structure + black-box behavior.
 * @run main LookupSwitchInstructionTest
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

public class LookupSwitchInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLSW" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LookupSwitchInstruction");
        check(c != null, "java.lang.classfile.instruction.LookupSwitchInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.LookupSwitchInstruction"), "binary name java.lang.classfile.instruction.LookupSwitchInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LookupSwitchInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LookupSwitchInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LookupSwitchInstruction has no superclass");
        check(declares(c, "defaultTarget"), "declares defaultTarget");
        check(declares(c, "cases"), "declares cases");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label d = cob.newLabel();
            Label c = cob.newLabel();
            ls[0] = LookupSwitchInstruction.of(d, java.util.List.of(SwitchCase.of(100, c), SwitchCase.of(-5, c)));
            cob.labelBinding(d).labelBinding(c).return_();
        }));
        LookupSwitchInstruction i = ls[0];
        check(i != null, "of created");
        check(i.opcode() == Opcode.LOOKUPSWITCH, "opcode");
        check(i.cases().size() == 2, "2 cases");
        check(i.cases().get(0).caseValue() == 100, "case 100");
        check(i.defaultTarget() != null, "defaultTarget");

    }


    // ---- Part 3: 发射-回读（核心）：乱序输入 → 升序输出 + 键/target 精确 ----
    static void testEmitReadback() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLSW"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label d = cob.newLabel(), c1 = cob.newLabel(), c5 = cob.newLabel(), c9 = cob.newLabel();
            // 乱序输入 [9,1,5]
            cob.iload(0).lookupswitch(d, java.util.List.of(SwitchCase.of(9, c9), SwitchCase.of(1, c1), SwitchCase.of(5, c5)));
            cob.labelBinding(c1).iconst_1().ireturn();
            cob.labelBinding(c5).iconst_5().ireturn();
            cob.labelBinding(c9).bipush(9).ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        LookupSwitchInstruction lsi = null;
        for (CodeElement e : ca.elementList()) if (e instanceof LookupSwitchInstruction x) lsi = x;
        check(lsi != null, "lookupswitch emitted + read back");
        check(lsi.opcode() == Opcode.LOOKUPSWITCH, "LOOKUPSWITCH opcode");
        check(lsi.opcode().kind() == Opcode.Kind.LOOKUP_SWITCH, "kind LOOKUP_SWITCH");
        check(lsi.cases().size() == 3, "3 cases");
        java.util.List<Integer> keys = new ArrayList<>();
        for (SwitchCase sc : lsi.cases()) keys.add(sc.caseValue());
        check(keys.equals(java.util.List.of(1, 5, 9)), "keys sorted ascending [1,5,9]: " + keys);
        check(lsi.defaultTarget() != null, "defaultTarget non-null");
        check(ca.labelToBci(lsi.defaultTarget()) > 0, "defaultTarget bci > 0");
        check(lsi.cases().get(0).target() != null && lsi.cases().get(2).target() != null, "case targets non-null");
        check(ca.labelToBci(lsi.cases().get(0).target()) < ca.labelToBci(lsi.cases().get(2).target()), "case1 bci < case9 bci");
        check(lsi instanceof Instruction && lsi instanceof CodeElement, "Instruction+CodeElement");
        check(lsi.sizeInBytes() == 35, "LOOKUPSWITCH sizeInBytes 35 for 3 cases, got " + lsi.sizeInBytes());
        // 负键 + MIN_VALUE 边界
        java.util.List<CodeElement> els = emit("neg", MethodTypeDesc.ofDescriptor("(I)I"), cob -> {
            Label d = cob.newLabel(), a = cob.newLabel(), b2 = cob.newLabel();
            cob.iload(0).lookupswitch(d, java.util.List.of(SwitchCase.of(Integer.MIN_VALUE, a), SwitchCase.of(-1, b2)));
            cob.labelBinding(a).iconst_1().ireturn();
            cob.labelBinding(b2).iconst_2().ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        });
        for (CodeElement e : els) if (e instanceof LookupSwitchInstruction x) {
            check(x.cases().get(0).caseValue() == Integer.MIN_VALUE, "MIN_VALUE key");
            check(x.cases().get(1).caseValue() == -1, "negative key");
            check(x.cases().get(0).caseValue() < x.cases().get(1).caseValue(), "MIN first");
        }
        // javac 真产物：sparse switch [1,5,9]
        byte[] self = LookupSwitchInstructionTest.class.getResourceAsStream("LookupSwitchInstructionTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.methodName().stringValue().equals("sparseHelper") || !mm.code().isPresent()) continue;
            for (CodeElement e : mm.code().get().elementList()) if (e instanceof LookupSwitchInstruction x) {
                found = true;
                java.util.List<Integer> ks = new ArrayList<>();
                for (SwitchCase sc : x.cases()) ks.add(sc.caseValue());
                check(ks.equals(java.util.List.of(1, 5, 9)), "javac sparse keys [1,5,9]: " + ks);
                check(x.defaultTarget() != null, "javac default present");
            }
        }
        check(found, "javac sparse switch emits LOOKUPSWITCH");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] l = new Label[2];
        ClassFile.of().build(ClassDesc.of("EmLSW2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            l[1] = cob.newLabel();
            LookupSwitchInstruction i = LookupSwitchInstruction.of(l[0], java.util.List.of(SwitchCase.of(7, l[1])));
            check(i.opcode() == Opcode.LOOKUPSWITCH, "of opcode");
            check(i.cases().size() == 1 && i.cases().get(0).caseValue() == 7, "of case 7");
            check(i.cases().get(0).target() == l[1], "of target identity");
            check(i.defaultTarget() == l[0], "of default identity");
            LookupSwitchInstruction empty = LookupSwitchInstruction.of(l[0], java.util.List.of());
            check(empty.cases().isEmpty(), "of empty cases legal");
            cob.return_();
        }));
        try { LookupSwitchInstruction.of(null, java.util.List.of()); check(false, "of(null,..) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,empty) NPE"); }
        try { LookupSwitchInstruction.of(null, null); check(false, "of(null,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,null) NPE"); }
    }

    static int sparseHelper(int x) {
        switch (x) { case 1: return 1; case 5: return 5; case 9: return 9; default: return -1; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
