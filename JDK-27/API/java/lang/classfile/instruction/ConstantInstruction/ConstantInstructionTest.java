/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction conformance: white-box structure + black-box behavior.
 * @run main ConstantInstructionTest
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

public class ConstantInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmCIN" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ConstantInstruction");
        check(c != null, "java.lang.classfile.instruction.ConstantInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ConstantInstruction"), "binary name java.lang.classfile.instruction.ConstantInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ConstantInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ConstantInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ConstantInstruction has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "ofIntrinsic"), "declares ofIntrinsic");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantInstruction c1 = ConstantInstruction.ofIntrinsic(Opcode.ICONST_1);
        check(c1 instanceof ConstantInstruction.IntrinsicConstantInstruction, "intrinsic type");
        check(c1.opcode() == Opcode.ICONST_1, "opcode");
        ConstantInstruction c2 = ConstantInstruction.ofArgument(Opcode.BIPUSH, 7);
        check(c2 instanceof ConstantInstruction.ArgumentConstantInstruction, "argument type");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ConstantInstruction c3 = ConstantInstruction.ofLoad(Opcode.LDC, cpb.stringEntry("s"));
        check(c3 instanceof ConstantInstruction.LoadConstantInstruction, "load type");

    }


    // ---- Part 3: 三分族层次结构 + 工厂矩阵（核心） ----
    static void testHierarchy() {
        // 三分族互斥且皆 instanceof ConstantInstruction
        ConstantInstruction intr = ConstantInstruction.ofIntrinsic(Opcode.ICONST_1);
        ConstantInstruction arg = ConstantInstruction.ofArgument(Opcode.BIPUSH, 7);
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ConstantInstruction load = ConstantInstruction.ofLoad(Opcode.LDC, cpb.stringEntry("s"));
        check(intr instanceof ConstantInstruction.IntrinsicConstantInstruction, "intrinsic subtype");
        check(arg instanceof ConstantInstruction.ArgumentConstantInstruction, "argument subtype");
        check(load instanceof ConstantInstruction.LoadConstantInstruction, "load subtype");
        check(!(intr instanceof ConstantInstruction.ArgumentConstantInstruction), "intrinsic !Argument");
        check(!(intr instanceof ConstantInstruction.LoadConstantInstruction), "intrinsic !Load");
        check(!(arg instanceof ConstantInstruction.IntrinsicConstantInstruction), "argument !Intrinsic");
        check(!(arg instanceof ConstantInstruction.LoadConstantInstruction), "argument !Load");
        check(!(load instanceof ConstantInstruction.IntrinsicConstantInstruction), "load !Intrinsic");
        check(!(load instanceof ConstantInstruction.ArgumentConstantInstruction), "load !Argument");
        for (ConstantInstruction c : new ConstantInstruction[]{intr, arg, load}) {
            check(c instanceof Instruction && c instanceof CodeElement, "Instruction+CodeElement");
            check(!((Object) c instanceof PseudoInstruction), "not PseudoInstruction");
        }
        check(cpb.stringEntry("s").equalsString("s"), "pool string entry");
        // sealed 层次：父接口 permits 三分族
        Class<?> c = ConstantInstruction.class;
        check(c.isSealed(), "ConstantInstruction sealed");
        Class<?>[] perm = c.getPermittedSubclasses();
        check(perm.length == 3, "3 permitted subclasses");
        java.util.List<String> names = new ArrayList<>();
        for (Class<?> p : perm) names.add(p.getSimpleName());
        check(names.contains("IntrinsicConstantInstruction") && names.contains("ArgumentConstantInstruction") && names.contains("LoadConstantInstruction"),
                "permitted set " + names);
    }

    // ---- Part 4: 工厂值矩阵 ----
    static void testFactoryMatrix() {
        // ofIntrinsic 全 typeKind 表
        check(ConstantInstruction.ofIntrinsic(Opcode.ICONST_2).typeKind() == TypeKind.INT, "ICONST_2 INT");
        check(ConstantInstruction.ofIntrinsic(Opcode.LCONST_1).typeKind() == TypeKind.LONG, "LCONST_1 LONG");
        check(ConstantInstruction.ofIntrinsic(Opcode.FCONST_2).typeKind() == TypeKind.FLOAT, "FCONST_2 FLOAT");
        check(ConstantInstruction.ofIntrinsic(Opcode.DCONST_1).typeKind() == TypeKind.DOUBLE, "DCONST_1 DOUBLE");
        check(ConstantInstruction.ofIntrinsic(Opcode.ACONST_NULL).typeKind() == TypeKind.REFERENCE, "ACONST_NULL REFERENCE");
        // constantValue 统一通道（父接口方法）
        check(ConstantInstruction.ofIntrinsic(Opcode.ICONST_2).constantValue().equals(Integer.valueOf(2)), "ICONST_2 cv 2");
        check(ConstantInstruction.ofIntrinsic(Opcode.LCONST_1).constantValue().equals(Long.valueOf(1L)), "LCONST_1 cv 1L");
        check(ConstantInstruction.ofIntrinsic(Opcode.FCONST_2).constantValue().equals(Float.valueOf(2.0f)), "FCONST_2 cv 2.0f");
        check(ConstantInstruction.ofIntrinsic(Opcode.DCONST_1).constantValue().equals(Double.valueOf(1.0d)), "DCONST_1 cv 1.0d");
        // ofArgument 边界
        check(ConstantInstruction.ofArgument(Opcode.BIPUSH, -128).constantValue().equals(-128), "BIPUSH -128");
        check(ConstantInstruction.ofArgument(Opcode.BIPUSH, 127).constantValue().equals(127), "BIPUSH 127");
        check(ConstantInstruction.ofArgument(Opcode.SIPUSH, -32768).constantValue().equals(-32768), "SIPUSH -32768");
        check(ConstantInstruction.ofArgument(Opcode.SIPUSH, 32767).constantValue().equals(32767), "SIPUSH 32767");
        check(ConstantInstruction.ofArgument(Opcode.SIPUSH, 32767).typeKind() == TypeKind.INT, "SIPUSH INT");
        // ofLoad 各常量类型 typeKind
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        check(ConstantInstruction.ofLoad(Opcode.LDC, cpb.stringEntry("x")).typeKind() == TypeKind.REFERENCE, "LDC String REFERENCE");
        check(ConstantInstruction.ofLoad(Opcode.LDC, cpb.intEntry(42)).typeKind() == TypeKind.INT, "LDC Integer INT");
        check(ConstantInstruction.ofLoad(Opcode.LDC2_W, cpb.longEntry(1L)).typeKind() == TypeKind.LONG, "LDC2_W Long LONG");
        check(ConstantInstruction.ofLoad(Opcode.LDC2_W, cpb.doubleEntry(1.5d)).typeKind() == TypeKind.DOUBLE, "LDC2_W Double DOUBLE");
        check(ConstantInstruction.ofLoad(Opcode.LDC, cpb.floatEntry(1.5f)).typeKind() == TypeKind.FLOAT, "LDC Float FLOAT");
        check(ConstantInstruction.ofLoad(Opcode.LDC, cpb.classEntry(ClassDesc.of("java.lang.String"))).typeKind() == TypeKind.REFERENCE, "LDC Class REFERENCE");
    }

    // ---- Part 5: 发射-回读（三分族各一例） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()J"),
                cob -> cob.iconst_2().i2l().pop2().bipush(7).pop().ldc("z").pop().ldc(100000L).pop2().lconst_1().return_());
        int intrinsic = 0, argument = 0, load = 0, other = 0;
        for (CodeElement e : els) {
            if (e instanceof ConstantInstruction.IntrinsicConstantInstruction) intrinsic++;
            else if (e instanceof ConstantInstruction.ArgumentConstantInstruction) argument++;
            else if (e instanceof ConstantInstruction.LoadConstantInstruction) load++;
            else if (e instanceof ConstantInstruction) other++;
        }
        check(intrinsic == 2, "2 intrinsic (ICONST_2, LCONST_1), got " + intrinsic);
        check(argument == 1, "1 argument (BIPUSH)");
        check(load == 2, "2 load (LDC String, LDC2_W Long)");
        check(other == 0, "no other ConstantInstruction");
        // CONSTANT kind 归属
        for (CodeElement e : els) if (e instanceof ConstantInstruction ci) check(ci.opcode().kind() == Opcode.Kind.CONSTANT, ci.opcode() + " kind CONSTANT");
    }

    // ---- Part 6: 负例矩阵 ----
    static void testNegative() {
        try { ConstantInstruction.ofIntrinsic(null); check(false, "ofIntrinsic(null) must throw"); }
        catch (NullPointerException e) { check(true, "ofIntrinsic(null) NPE"); }
        try { ConstantInstruction.ofIntrinsic(Opcode.NOP); check(false, "ofIntrinsic(NOP) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofIntrinsic(NOP) IAE"); }
        try { ConstantInstruction.ofArgument(null, 1); check(false, "ofArgument(null,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofArgument(null,1) IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.NOP, 1); check(false, "ofArgument(NOP,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofArgument(NOP,1) IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.BIPUSH, 128); check(false, "ofArgument(BIPUSH,128) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofArgument(BIPUSH,128) IAE out of byte range"); }
        try { ConstantInstruction.ofArgument(Opcode.SIPUSH, 32768); check(false, "ofArgument(SIPUSH,32768) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofArgument(SIPUSH,32768) IAE out of short range"); }
        try { ConstantInstruction.ofLoad(null, null); check(false, "ofLoad(null,null) must throw"); }
        catch (NullPointerException e) { check(true, "ofLoad(null,null) NPE"); }
        try { ConstantInstruction.ofLoad(Opcode.NOP, ConstantPoolBuilder.of().stringEntry("x")); check(false, "ofLoad(NOP,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofLoad(NOP,..) IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchy();
        testFactoryMatrix();
        testEmitReadback();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
