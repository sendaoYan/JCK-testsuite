/*
 * @test
 * @summary java.lang.classfile.instruction.SwitchCase conformance: white-box structure + black-box behavior.
 * @run main SwitchCaseTest
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

public class SwitchCaseTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmSWC" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.SwitchCase");
        check(c != null, "java.lang.classfile.instruction.SwitchCase loads");
        check(c.getName().equals("java.lang.classfile.instruction.SwitchCase"), "binary name java.lang.classfile.instruction.SwitchCase");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.SwitchCase is public");
        check(c.isInterface(), "java.lang.classfile.instruction.SwitchCase is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.SwitchCase has no superclass");
        check(declares(c, "caseValue"), "declares caseValue");
        check(declares(c, "target"), "declares target");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label l = cob.newLabel();
            sc[0] = SwitchCase.of(7, l);
            cob.labelBinding(l).return_();
        }));
        SwitchCase c = sc[0];
        check(c != null, "of created");
        check(c.caseValue() == 7, "caseValue");
        check(c.target() != null, "target");

    }


    // ---- Part 3: 边界值 + equals/hashCode 契约 ----
    static void testValuesAndEquality() throws Exception {
        final Label[] l = new Label[1];
        final SwitchCase[] out = new SwitchCase[4];
        ClassFile.of().build(ClassDesc.of("EmSWC"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            out[0] = SwitchCase.of(1, l[0]);
            out[1] = SwitchCase.of(Integer.MIN_VALUE, l[0]);
            out[2] = SwitchCase.of(Integer.MAX_VALUE, l[0]);
            out[3] = SwitchCase.of(1, l[0]);  // 与 out[0] 同值同 target
            cob.return_();
        }));
        check(out[0].caseValue() == 1, "caseValue 1");
        check(out[0].target() == l[0], "target identity ==");
        check(out[1].caseValue() == Integer.MIN_VALUE, "MIN_VALUE key");
        check(out[2].caseValue() == Integer.MAX_VALUE, "MAX_VALUE key");
        check(out[1].target() == l[0] && out[2].target() == l[0], "boundary targets identity");
        check(out[0].equals(out[3]), "equals: same value+target");
        check(out[0].hashCode() == out[3].hashCode(), "hashCode consistent with equals");
        check(!out[0].equals(out[1]), "different values not equal");
        check(!out[0].equals(null), "not equal to null");
        check(!out[0].equals("x"), "not equal to unrelated type");
        // SwitchCase 不是 CodeElement/Instruction（纯值对象）
        check(!((Object) out[0] instanceof CodeElement), "SwitchCase not CodeElement");
        check(!((Object) out[0] instanceof Instruction), "SwitchCase not Instruction");
    }

    // ---- Part 4: 在真实 switch 中的使用 + 负例 ----
    static void testInSwitchAndNegative() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmSWC2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label d = cob.newLabel(), c1 = cob.newLabel(), c2 = cob.newLabel();
            cob.iload(0).lookupswitch(d, java.util.List.of(SwitchCase.of(1, c1), SwitchCase.of(2, c2)));
            cob.labelBinding(c1).loadConstant(10).ireturn();
            cob.labelBinding(c2).loadConstant(20).ireturn();
            cob.labelBinding(d).iconst_m1().ireturn();
        }));
        boolean found1 = false, found2 = false;
        for (CodeElement e : ClassFile.of().parse(b).methods().get(0).code().get().elementList())
            if (e instanceof LookupSwitchInstruction x)
                for (SwitchCase sc : x.cases()) {
                    if (sc.caseValue() == 1) found1 = true;
                    if (sc.caseValue() == 2) found2 = true;
                }
        check(found1 && found2, "SwitchCase values visible in built lookupswitch");
        try { SwitchCase.of(1, null); check(false, "of(1,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(1,null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValuesAndEquality();
        testInSwitchAndNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
