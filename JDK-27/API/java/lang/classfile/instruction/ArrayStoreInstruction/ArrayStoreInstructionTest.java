/*
 * @test
 * @summary java.lang.classfile.instruction.ArrayStoreInstruction conformance: white-box structure + black-box behavior.
 * @run main ArrayStoreInstructionTest
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

public class ArrayStoreInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmASI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ArrayStoreInstruction");
        check(c != null, "java.lang.classfile.instruction.ArrayStoreInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ArrayStoreInstruction"), "binary name java.lang.classfile.instruction.ArrayStoreInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ArrayStoreInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ArrayStoreInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ArrayStoreInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ArrayStoreInstruction i = ArrayStoreInstruction.of(Opcode.IASTORE);
        check(i.opcode() == Opcode.IASTORE, "opcode");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");

    }


    // ---- Part 3: 发射-回读矩阵（核心） ----
    static void testEmitReadback() throws Exception {
        // iastore 闭环：数组 → index → value → iastore
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("([I)V"),
                cob -> cob.aload(0).iconst_0().iconst_1().iastore().return_());
        ArrayStoreInstruction asi = null;
        for (CodeElement e : els) if (e instanceof ArrayStoreInstruction x) asi = x;
        check(asi != null, "iastore emitted + read back");
        check(asi.opcode() == Opcode.IASTORE, "iastore opcode");
        check(asi.typeKind() == TypeKind.INT, "iastore typeKind INT");
        check(asi.sizeInBytes() == 1, "iastore sizeInBytes 1");
        check(asi.opcode().kind() == Opcode.Kind.ARRAY_STORE, "IASTORE kind ARRAY_STORE");
        check(asi instanceof Instruction && asi instanceof CodeElement, "Instruction+CodeElement");
        check(!((Object) asi instanceof PseudoInstruction), "not PseudoInstruction");
        // aastore 引用族
        els = emit("aa", MethodTypeDesc.ofDescriptor("([Ljava/lang/String;)V"),
                cob -> cob.aload(0).iconst_0().ldc("v").aastore().return_());
        boolean foundA = false;
        for (CodeElement e : els) if (e instanceof ArrayStoreInstruction x && x.opcode() == Opcode.AASTORE) { foundA = true; check(x.typeKind() == TypeKind.REFERENCE, "AASTORE typeKind REFERENCE"); }
        check(foundA, "aastore emitted + read back");
        // 工厂全 8 opcode 矩阵
        Opcode[] ops = {Opcode.IASTORE, Opcode.LASTORE, Opcode.FASTORE, Opcode.DASTORE,
                        Opcode.AASTORE, Opcode.BASTORE, Opcode.CASTORE, Opcode.SASTORE};
        TypeKind[] tks = {TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE,
                          TypeKind.REFERENCE, TypeKind.BYTE, TypeKind.CHAR, TypeKind.SHORT};
        for (int i = 0; i < ops.length; i++) {
            ArrayStoreInstruction x = ArrayStoreInstruction.of(ops[i]);
            check(x.opcode() == ops[i], ops[i] + " factory opcode");
            check(x.typeKind() == tks[i], ops[i] + " factory typeKind " + tks[i]);
            check(x.sizeInBytes() == 1, ops[i] + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.ARRAY_STORE, ops[i] + " kind ARRAY_STORE");
        }
        // 全 8 形族发射回读
        els = emit("full", MethodTypeDesc.ofDescriptor("([I[J[F[D[Ljava/lang/Object;[B[C[S)V"), cob -> {
            cob.aload(0).iconst_0().iconst_1().iastore();
            cob.aload(1).iconst_0().lconst_0().lastore();
            cob.aload(2).iconst_0().fconst_0().fastore();
            cob.aload(3).iconst_0().dconst_0().dastore();
            cob.aload(4).iconst_0().aconst_null().aastore();
            cob.aload(5).iconst_0().iconst_1().bastore();
            cob.aload(6).iconst_0().iconst_1().castore();
            cob.aload(7).iconst_0().iconst_1().sastore();
            cob.return_();
        });
        Opcode[] seen = new Opcode[8];
        int n = 0;
        for (CodeElement e : els) if (e instanceof ArrayStoreInstruction x) { seen[n % 8] = x.opcode(); n++; }
        check(n == 8, "8 array stores emitted, got " + n);
        for (int i = 0; i < 8; i++) check(seen[i] == ops[i], "family order[" + i + "]=" + seen[i]);
    }

    // ---- Part 4: 负例矩阵 ----
    static void testNegative() {
        try { ArrayStoreInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        try { ArrayStoreInstruction.of(Opcode.NOP); check(false, "of(NOP) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "of(NOP) IAE message non-null"); }
        try { ArrayStoreInstruction.of(Opcode.IALOAD); check(false, "of(IALOAD) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IALOAD) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
