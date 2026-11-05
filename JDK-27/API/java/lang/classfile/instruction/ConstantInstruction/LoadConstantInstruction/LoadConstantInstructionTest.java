/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction conformance: white-box structure + black-box behavior.
 * @run main LoadConstantInstructionTest
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

public class LoadConstantInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLCI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ConstantInstruction$LoadConstantInstruction");
        check(c != null, "java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ConstantInstruction$LoadConstantInstruction"), "binary name java.lang.classfile.instruction.ConstantInstruction$LoadConstantInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction has no superclass");
        check(declares(c, "constantEntry"), "declares constantEntry");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ConstantInstruction.LoadConstantInstruction i = ConstantInstruction.ofLoad(Opcode.LDC, cpb.stringEntry("x"));
        check(i.opcode() == Opcode.LDC, "opcode");
        check(i.constantEntry().constantValue().equals("x"), "constantEntry constantValue");
        check(i.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");
        check(i.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");

    }


    // ---- Part 3: 发射-回读矩阵（核心）：ldc 类型全表 ----
    static void testEmitReadback() throws Exception {
        // String → LDC size2
        java.util.List<CodeElement> els = emit("s", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"), cob -> cob.ldc("hello").areturn());
        ConstantInstruction.LoadConstantInstruction i = null;
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) i = x;
        check(i != null, "ldc(String) emitted + read back");
        check(i.opcode() == Opcode.LDC, "LDC opcode");
        check(i.sizeInBytes() == 2, "LDC sizeInBytes 2");
        check(i.constantValue().equals("hello"), "constantValue hello");
        check(i.constantEntry() instanceof StringEntry, "constantEntry StringEntry");
        check(((StringEntry) i.constantEntry()).stringValue().equals("hello"), "entry stringValue");
        check(i.typeKind() == TypeKind.REFERENCE, "String REFERENCE");
        // int → LDC / IntegerEntry
        els = emit("i", MethodTypeDesc.ofDescriptor("()I"), cob -> cob.ldc(42).ireturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) {
            check(x.opcode() == Opcode.LDC, "ldc(int) LDC");
            check(x.constantValue().equals(Integer.valueOf(42)), "ldc int cv 42");
            check(x.constantEntry() instanceof IntegerEntry, "IntegerEntry");
            check(x.typeKind() == TypeKind.INT, "int INT");
        }
        // long → LDC2_W size3
        els = emit("l", MethodTypeDesc.ofDescriptor("()J"), cob -> cob.ldc(100000L).lreturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) {
            check(x.opcode() == Opcode.LDC2_W, "ldc(long) LDC2_W");
            check(x.sizeInBytes() == 3, "LDC2_W sizeInBytes 3");
            check(x.constantValue().equals(Long.valueOf(100000L)), "long cv 100000");
            check(x.constantEntry() instanceof LongEntry, "LongEntry");
            check(x.typeKind() == TypeKind.LONG, "long LONG");
        }
        // double → LDC2_W / DoubleEntry
        els = emit("d", MethodTypeDesc.ofDescriptor("()D"), cob -> cob.ldc(1.5d).dreturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) {
            check(x.opcode() == Opcode.LDC2_W, "ldc(double) LDC2_W");
            check(x.constantValue().equals(Double.valueOf(1.5d)), "double cv 1.5");
            check(x.constantEntry() instanceof DoubleEntry, "DoubleEntry");
            check(x.typeKind() == TypeKind.DOUBLE, "double DOUBLE");
        }
        // float → LDC / FloatEntry
        els = emit("f", MethodTypeDesc.ofDescriptor("()F"), cob -> cob.ldc(1.5f).freturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) {
            check(x.opcode() == Opcode.LDC, "ldc(float) LDC");
            check(x.constantValue().equals(Float.valueOf(1.5f)), "float cv 1.5");
            check(x.constantEntry() instanceof FloatEntry, "FloatEntry");
            check(x.typeKind() == TypeKind.FLOAT, "float FLOAT");
        }
        // Class → LDC / ClassEntry
        els = emit("c", MethodTypeDesc.ofDescriptor("()Ljava/lang/Class;"), cob -> cob.ldc(ClassDesc.of("java.lang.String")).areturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.LoadConstantInstruction x) {
            check(x.opcode() == Opcode.LDC, "ldc(Class) LDC");
            check(x.constantEntry() instanceof ClassEntry, "ClassEntry");
            check(((ClassEntry) x.constantEntry()).asInternalName().equals("java/lang/String"), "class internal name");
            check(x.typeKind() == TypeKind.REFERENCE, "class REFERENCE");
        }
        // 分类
        check(els.stream().noneMatch(e -> (Object) e instanceof PseudoInstruction && e instanceof Instruction), "classification sane");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        StringEntry se = cpb.stringEntry("x");
        ConstantInstruction.LoadConstantInstruction i = ConstantInstruction.ofLoad(Opcode.LDC, se);
        check(i.opcode() == Opcode.LDC, "ofLoad LDC opcode");
        check(i.constantEntry() == se, "constantEntry identity from factory");
        check(i.typeKind() == TypeKind.REFERENCE, "String REFERENCE");
        check(ConstantInstruction.ofLoad(Opcode.LDC_W, cpb.stringEntry("w")).opcode() == Opcode.LDC_W, "ofLoad LDC_W");
        check(ConstantInstruction.ofLoad(Opcode.LDC_W, cpb.stringEntry("w")).sizeInBytes() == 3, "LDC_W size 3");
        check(ConstantInstruction.ofLoad(Opcode.LDC2_W, cpb.longEntry(1L)).typeKind() == TypeKind.LONG, "ofLoad LDC2_W LONG");
        try { ConstantInstruction.ofLoad(null, se); check(false, "ofLoad(null,..) must throw"); }
        catch (NullPointerException e) { check(true, "ofLoad(null,..) NPE"); }
        try { ConstantInstruction.ofLoad(Opcode.LDC, null); check(false, "ofLoad(LDC,null) must throw"); }
        catch (NullPointerException e) { check(true, "ofLoad(LDC,null) NPE"); }
        try { ConstantInstruction.ofLoad(Opcode.NOP, se); check(false, "ofLoad(NOP,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "ofLoad(NOP,..) IAE wrong kind"); }
        // loadConstant 泛化通道（CodeBuilder.loadConstant 自动选 LDC/LDC2_W）
        try {
            byte[] b = ClassFile.of().build(ClassDesc.of("EmLCI2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_STATIC,
                    cob -> cob.loadConstant(100000L).lreturn()));
            boolean wide = false;
            for (CodeElement e : ClassFile.of().parse(b).methods().get(0).code().get().elementList())
                if (e instanceof ConstantInstruction.LoadConstantInstruction x && x.opcode() == Opcode.LDC2_W) wide = true;
            check(wide, "loadConstant(long) auto-selects LDC2_W");
            boolean small = false;
            byte[] b2 = ClassFile.of().build(ClassDesc.of("EmLCI3"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC,
                    cob -> cob.loadConstant(100000).ireturn()));
            for (CodeElement e : ClassFile.of().parse(b2).methods().get(0).code().get().elementList())
                if (e instanceof ConstantInstruction.LoadConstantInstruction x && x.opcode() == Opcode.LDC && x.constantValue().equals(Integer.valueOf(100000))) small = true;
            check(small, "loadConstant(int beyond short) uses LDC");
        } catch (Exception ex) {
            check(false, "loadConstant channel: " + ex);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
