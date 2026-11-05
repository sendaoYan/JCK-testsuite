/*
 * @test
 * @summary java.lang.classfile.instruction.TableSwitchInstruction conformance: white-box structure + black-box behavior.
 * @run main TableSwitchInstructionTest
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

public class TableSwitchInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmTSW" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.TableSwitchInstruction");
        check(c != null, "java.lang.classfile.instruction.TableSwitchInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.TableSwitchInstruction"), "binary name java.lang.classfile.instruction.TableSwitchInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.TableSwitchInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.TableSwitchInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.TableSwitchInstruction has no superclass");
        check(declares(c, "lowValue"), "declares lowValue");
        check(declares(c, "highValue"), "declares highValue");
        check(declares(c, "defaultTarget"), "declares defaultTarget");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label d = cob.newLabel();
            Label c0 = cob.newLabel();
            ts[0] = TableSwitchInstruction.of(0, 1, d, java.util.List.of(SwitchCase.of(0, c0), SwitchCase.of(1, c0)));
            cob.labelBinding(d).labelBinding(c0).return_();
        }));
        TableSwitchInstruction i = ts[0];
        check(i != null, "of created");
        check(i.opcode() == Opcode.TABLESWITCH, "opcode");
        check(i.lowValue() == 0 && i.highValue() == 1, "low/high");
        check(i.cases().size() == 2, "2 cases");
        check(i.defaultTarget() != null, "defaultTarget");

    }


    // ---- Part 3: 发射-回读（核心）：low/high/default/cases 精确 ----
    static void testEmitReadback() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmTSW"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label d = cob.newLabel(), c0 = cob.newLabel(), c1 = cob.newLabel(), c2 = cob.newLabel();
            cob.iload(0).tableswitch(0, 2, d, java.util.List.of(SwitchCase.of(0, c0), SwitchCase.of(1, c1), SwitchCase.of(2, c2)));
            cob.labelBinding(c0).iconst_0().ireturn();
            cob.labelBinding(c1).iconst_1().ireturn();
            cob.labelBinding(c2).iconst_2().ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        TableSwitchInstruction i = null;
        for (CodeElement e : ca.elementList()) if (e instanceof TableSwitchInstruction x) i = x;
        check(i != null, "tableswitch emitted + read back");
        check(i.opcode() == Opcode.TABLESWITCH, "TABLESWITCH opcode");
        check(i.opcode().kind() == Opcode.Kind.TABLE_SWITCH, "kind TABLE_SWITCH");
        check(i.lowValue() == 0, "lowValue 0");
        check(i.highValue() == 2, "highValue 2");
        check(i.cases().size() == 3, "3 cases (high-low+1)");
        java.util.List<Integer> keys = new ArrayList<>();
        for (SwitchCase sc : i.cases()) keys.add(sc.caseValue());
        check(keys.equals(java.util.List.of(0, 1, 2)), "case keys [0,1,2]: " + keys);
        check(i.defaultTarget() != null, "defaultTarget non-null");
        check(ca.labelToBci(i.defaultTarget()) > ca.labelToBci(i.cases().get(0).target()), "default bci > case0 bci");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        check(i.sizeInBytes() == 27, "TABLESWITCH sizeInBytes 27 for 3 cases, got " + i.sizeInBytes());
        // 负 low 起点
        java.util.List<CodeElement> els = emit("neg", MethodTypeDesc.ofDescriptor("(I)I"), cob -> {
            Label d = cob.newLabel(), cm = cob.newLabel();
            cob.iload(0).tableswitch(-1, 0, d, java.util.List.of(SwitchCase.of(-1, cm), SwitchCase.of(0, cm)));
            cob.labelBinding(cm).iconst_1().ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        });
        for (CodeElement e : els) if (e instanceof TableSwitchInstruction x) {
            check(x.lowValue() == -1 && x.highValue() == 0, "negative low");
            check(x.cases().get(0).caseValue() == -1, "negative case key");
        }
        // 自动推导重载 tableswitch(Label, List<SwitchCase>)
        els = emit("auto", MethodTypeDesc.ofDescriptor("(I)I"), cob -> {
            Label d = cob.newLabel(), c1 = cob.newLabel(), c2 = cob.newLabel();
            cob.iload(0).tableswitch(d, java.util.List.of(SwitchCase.of(5, c1), SwitchCase.of(6, c2)));
            cob.labelBinding(c1).iconst_1().ireturn();
            cob.labelBinding(c2).iconst_2().ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        });
        for (CodeElement e : els) if (e instanceof TableSwitchInstruction x)
            check(x.lowValue() == 5 && x.highValue() == 6, "auto-derived low/high 5-6");
        // javac 真产物：dense switch 0..2
        byte[] self = TableSwitchInstructionTest.class.getResourceAsStream("TableSwitchInstructionTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.methodName().stringValue().equals("denseHelper") || !mm.code().isPresent()) continue;
            for (CodeElement e : mm.code().get().elementList()) if (e instanceof TableSwitchInstruction x) {
                found = true;
                check(x.lowValue() == 0 && x.highValue() == 2 && x.cases().size() == 3, "javac dense low=0 high=2 n=3");
            }
        }
        check(found, "javac dense switch emits TABLESWITCH");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] l = new Label[2];
        ClassFile.of().build(ClassDesc.of("EmTSW2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            l[1] = cob.newLabel();
            TableSwitchInstruction i = TableSwitchInstruction.of(0, 1, l[0], java.util.List.of(SwitchCase.of(0, l[1]), SwitchCase.of(1, l[1])));
            check(i.opcode() == Opcode.TABLESWITCH, "of opcode");
            check(i.lowValue() == 0 && i.highValue() == 1, "of low/high");
            check(i.cases().size() == 2, "of 2 cases");
            check(i.defaultTarget() == l[0], "of default identity");
            check(i.cases().get(0).target() == l[1], "of case target identity");
            cob.return_();
        }));
        try { TableSwitchInstruction.of(0, 1, null, null); check(false, "of(0,1,null,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(0,1,null,null) NPE"); }
        try { TableSwitchInstruction.of(2, 0, null, null); check(false, "of(2,0,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of low>high IAE"); }
    }

    static int denseHelper(int x) {
        switch (x) { case 0: return 10; case 1: return 11; case 2: return 12; default: return -1; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
