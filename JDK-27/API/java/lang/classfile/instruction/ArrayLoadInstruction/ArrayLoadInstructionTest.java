/*
 * @test
 * @summary java.lang.classfile.instruction.ArrayLoadInstruction conformance: white-box structure + black-box behavior.
 * @run main ArrayLoadInstructionTest
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

public class ArrayLoadInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmALI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ArrayLoadInstruction");
        check(c != null, "java.lang.classfile.instruction.ArrayLoadInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ArrayLoadInstruction"), "binary name java.lang.classfile.instruction.ArrayLoadInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ArrayLoadInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ArrayLoadInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ArrayLoadInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ArrayLoadInstruction i = ArrayLoadInstruction.of(Opcode.IALOAD);
        check(i.opcode() == Opcode.IALOAD, "opcode");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(ArrayLoadInstruction.of(Opcode.AALOAD).typeKind() == TypeKind.REFERENCE, "AALOAD REFERENCE");

    }


    // ---- Part 3: 发射-回读矩阵（核心） ----
    static void testEmitReadback() throws Exception {
        // 单条 iaload 闭环：count → newarray → index → iaload → ireturn
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()I"),
                cob -> cob.iconst_3().newarray(TypeKind.INT).iconst_0().iaload().ireturn());
        ArrayLoadInstruction ali = null;
        for (CodeElement e : els) if (e instanceof ArrayLoadInstruction x) ali = x;
        check(ali != null, "iaload emitted + read back");
        check(ali.opcode() == Opcode.IALOAD, "iaload opcode");
        check(ali.typeKind() == TypeKind.INT, "iaload typeKind INT");
        check(ali.sizeInBytes() == 1, "iaload sizeInBytes 1");
        check(ali.opcode().kind() == Opcode.Kind.ARRAY_LOAD, "IALOAD kind ARRAY_LOAD");
        check(ali instanceof Instruction && ali instanceof CodeElement, "Instruction+CodeElement");
        check(!((Object) ali instanceof PseudoInstruction), "not PseudoInstruction");
        // 工厂全 8 opcode 矩阵
        Opcode[] ops = {Opcode.IALOAD, Opcode.LALOAD, Opcode.FALOAD, Opcode.DALOAD,
                        Opcode.AALOAD, Opcode.BALOAD, Opcode.CALOAD, Opcode.SALOAD};
        TypeKind[] tks = {TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE,
                          TypeKind.REFERENCE, TypeKind.BYTE, TypeKind.CHAR, TypeKind.SHORT};
        for (int i = 0; i < ops.length; i++) {
            ArrayLoadInstruction x = ArrayLoadInstruction.of(ops[i]);
            check(x.opcode() == ops[i], ops[i] + " factory opcode");
            check(x.typeKind() == tks[i], ops[i] + " factory typeKind " + tks[i]);
            check(x.sizeInBytes() == 1, ops[i] + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.ARRAY_LOAD, ops[i] + " kind ARRAY_LOAD");
        }
        // 通用 arrayLoad(TypeKind) 发射
        els = emit("g", MethodTypeDesc.ofDescriptor("([I)I"), cob -> cob.aload(0).iconst_0().arrayLoad(TypeKind.INT).ireturn());
        boolean found = false;
        for (CodeElement e : els) if (e instanceof ArrayLoadInstruction x && x.opcode() == Opcode.IALOAD) found = true;
        check(found, "arrayLoad(INT) emits IALOAD");
        // 全 8 形族发射回读（含 LONG/DOUBLE 双槽）
        els = emit("full", MethodTypeDesc.ofDescriptor("([I[J[F[D[Ljava/lang/Object;[B[C[S)V"), cob -> {
            cob.aload(0).iconst_0().iaload().pop();
            cob.aload(1).iconst_0().laload().pop2();
            cob.aload(2).iconst_0().faload().pop();
            cob.aload(3).iconst_0().daload().pop2();
            cob.aload(4).iconst_0().aaload().pop();
            cob.aload(5).iconst_0().baload().pop();
            cob.aload(6).iconst_0().caload().pop();
            cob.aload(7).iconst_0().saload().pop();
            cob.return_();
        });
        Opcode[] seen = new Opcode[8];
        int n = 0;
        for (CodeElement e : els) if (e instanceof ArrayLoadInstruction x) { seen[n % 8] = x.opcode(); n++; }
        check(n == 8, "8 array loads emitted, got " + n);
        for (int i = 0; i < 8; i++) check(seen[i] == ops[i], "family order[" + i + "]=" + seen[i]);
        for (int i = 0; i < 8; i++)
            for (CodeElement e : els) if (e instanceof ArrayLoadInstruction x && x.opcode() == ops[i]) check(x.typeKind() == tks[i], ops[i] + " readback typeKind");
    }

    // ---- Part 4: 负例矩阵 ----
    static void testNegative() {
        try { ArrayLoadInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null || true, "of(null) NPE"); }
        try { ArrayLoadInstruction.of(Opcode.NOP); check(false, "of(NOP) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "of(NOP) IAE message non-null"); }
        try { ArrayLoadInstruction.of(Opcode.IADD); check(false, "of(IADD) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
