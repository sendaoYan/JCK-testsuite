/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction conformance: white-box structure + black-box behavior.
 * @run main IntrinsicConstantInstructionTest
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

public class IntrinsicConstantInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmICI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ConstantInstruction$IntrinsicConstantInstruction");
        check(c != null, "java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ConstantInstruction$IntrinsicConstantInstruction"), "binary name java.lang.classfile.instruction.ConstantInstruction$IntrinsicConstantInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantInstruction.IntrinsicConstantInstruction i = ConstantInstruction.ofIntrinsic(Opcode.ICONST_2);
        check(i.opcode() == Opcode.ICONST_2, "opcode");
        check(i.constantValue().equals(2), "constantValue 2");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");

    }


    // ---- Part 3: 发射-回读矩阵（核心）：全 12 个 intrinsic 常量指令 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", ConstantDescs.MTD_void, cob -> {
            cob.iconst_m1().pop();
            cob.iconst_0().pop();
            cob.iconst_1().pop();
            cob.iconst_2().pop();
            cob.iconst_3().pop();
            cob.iconst_4().pop();
            cob.iconst_5().pop();
            cob.lconst_0().pop2();
            cob.lconst_1().pop2();
            cob.fconst_0().pop();
            cob.fconst_1().pop();
            cob.fconst_2().pop();
            cob.dconst_0().pop2();
            cob.dconst_1().pop2();
            cob.aconst_null().pop();
            cob.return_();
        });
        Opcode[] expect = {Opcode.ICONST_M1, Opcode.ICONST_0, Opcode.ICONST_1, Opcode.ICONST_2, Opcode.ICONST_3,
                Opcode.ICONST_4, Opcode.ICONST_5, Opcode.LCONST_0, Opcode.LCONST_1, Opcode.FCONST_0,
                Opcode.FCONST_1, Opcode.FCONST_2, Opcode.DCONST_0, Opcode.DCONST_1, Opcode.ACONST_NULL};
        TypeKind[] tks = {TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT,
                TypeKind.INT, TypeKind.INT, TypeKind.LONG, TypeKind.LONG, TypeKind.FLOAT,
                TypeKind.FLOAT, TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.DOUBLE, TypeKind.REFERENCE};
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof ConstantInstruction.IntrinsicConstantInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == 1, x.opcode() + " sizeInBytes 1");
            check(x instanceof ConstantInstruction && !((Object) x instanceof ConstantInstruction.ArgumentConstantInstruction)
               && !((Object) x instanceof ConstantInstruction.LoadConstantInstruction), x.opcode() + " exactly Intrinsic");
        }
        check(got.equals(java.util.List.of(expect)), "15 intrinsic opcodes in order: " + got);
        for (int i = 0; i < expect.length; i++)
            for (CodeElement e : els) if (e instanceof ConstantInstruction.IntrinsicConstantInstruction x && x.opcode() == expect[i])
                check(x.typeKind() == tks[i], expect[i] + " typeKind " + tks[i]);
        check(!els.isEmpty() && els.stream().allMatch(e -> !((Object) e instanceof PseudoInstruction) || true), "parse ok");
    }

    // ---- Part 4: 工厂值矩阵（constantValue/typeKind 精确表） ----
    static void testFactoryMatrix() {
        Object[][] rows = {
                {Opcode.ICONST_M1, Integer.valueOf(-1), TypeKind.INT},
                {Opcode.ICONST_0, Integer.valueOf(0), TypeKind.INT},
                {Opcode.ICONST_1, Integer.valueOf(1), TypeKind.INT},
                {Opcode.ICONST_2, Integer.valueOf(2), TypeKind.INT},
                {Opcode.ICONST_3, Integer.valueOf(3), TypeKind.INT},
                {Opcode.ICONST_4, Integer.valueOf(4), TypeKind.INT},
                {Opcode.ICONST_5, Integer.valueOf(5), TypeKind.INT},
                {Opcode.LCONST_0, Long.valueOf(0L), TypeKind.LONG},
                {Opcode.LCONST_1, Long.valueOf(1L), TypeKind.LONG},
                {Opcode.FCONST_0, Float.valueOf(0.0f), TypeKind.FLOAT},
                {Opcode.FCONST_1, Float.valueOf(1.0f), TypeKind.FLOAT},
                {Opcode.FCONST_2, Float.valueOf(2.0f), TypeKind.FLOAT},
                {Opcode.DCONST_0, Double.valueOf(0.0d), TypeKind.DOUBLE},
                {Opcode.DCONST_1, Double.valueOf(1.0d), TypeKind.DOUBLE},
        };
        for (Object[] r : rows) {
            ConstantInstruction.IntrinsicConstantInstruction i = ConstantInstruction.ofIntrinsic((Opcode) r[0]);
            check(i.opcode() == r[0], r[0] + " opcode");
            check(i.constantValue().equals(r[1]), r[0] + " constantValue " + r[1]);
            check(i.typeKind() == r[2], r[0] + " typeKind " + r[2]);
            check(i.sizeInBytes() == 1, r[0] + " sizeInBytes 1");
        }
        // ACONST_NULL：constantValue 为 nullConstant 引导的 DynamicConstantDesc（非 null 语义）
        ConstantInstruction.IntrinsicConstantInstruction acn = ConstantInstruction.ofIntrinsic(Opcode.ACONST_NULL);
        check(acn.typeKind() == TypeKind.REFERENCE, "ACONST_NULL REFERENCE");
        check(acn.constantValue() != null && acn.constantValue().toString().contains("nullConstant"), "ACONST_NULL cv nullConstant desc");
        check(ConstantInstruction.ofIntrinsic(Opcode.ACONST_NULL) instanceof Instruction, "Instruction");
    }

    // ---- Part 5: 负例 ----
    static void testNegative() {
        try { ConstantInstruction.ofIntrinsic(null); check(false, "ofIntrinsic(null) must throw"); }
        catch (NullPointerException e) { check(true, "ofIntrinsic(null) NPE"); }
        for (Opcode bad : new Opcode[]{Opcode.NOP, Opcode.IADD, Opcode.BIPUSH, Opcode.LDC}) {
            try { ConstantInstruction.ofIntrinsic(bad); check(false, "ofIntrinsic(" + bad + ") must throw"); }
            catch (IllegalArgumentException e) { check(true, "ofIntrinsic(" + bad + ") IAE wrong kind"); }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryMatrix();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
