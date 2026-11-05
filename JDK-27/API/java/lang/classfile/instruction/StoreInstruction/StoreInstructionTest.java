/*
 * @test
 * @summary java.lang.classfile.instruction.StoreInstruction conformance: white-box structure + black-box behavior.
 * @run main StoreInstructionTest
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

public class StoreInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmSTI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.StoreInstruction");
        check(c != null, "java.lang.classfile.instruction.StoreInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.StoreInstruction"), "binary name java.lang.classfile.instruction.StoreInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.StoreInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.StoreInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.StoreInstruction has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        StoreInstruction i = StoreInstruction.of(TypeKind.INT, 5);
        check(i.opcode() == Opcode.ISTORE, "opcode ISTORE");
        check(i.slot() == 5, "slot 5");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");

    }


    // ---- Part 3: 发射-回读（核心）：_0/_1/_2/_3 快捷 + wide 自动 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("(IJFDLjava/lang/Object;)V"), cob -> {
            cob.iconst_1().istore(0);
            cob.iconst_1().istore(1);
            cob.iconst_1().istore(2);
            cob.iconst_1().istore(3);
            cob.lconst_0().lstore(1);
            cob.fconst_0().fstore(1);
            cob.dconst_0().dstore(1);
            cob.aconst_null().astore(4);
            cob.return_();
        });
        Opcode[] expect = {Opcode.ISTORE_0, Opcode.ISTORE_1, Opcode.ISTORE_2, Opcode.ISTORE_3, Opcode.LSTORE_1,
                Opcode.FSTORE_1, Opcode.DSTORE_1, Opcode.ASTORE};
        TypeKind[] tks = {TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.LONG,
                TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.REFERENCE};
        int[] slots = {0, 1, 2, 3, 1, 1, 1, 4};
        java.util.List<Opcode> got = new ArrayList<>();
        int idx = 0;
        for (CodeElement e : els) if (e instanceof StoreInstruction x) {
            got.add(x.opcode());
            check(x.opcode().kind() == Opcode.Kind.STORE, x.opcode() + " kind STORE");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
            idx++;
        }
        check(got.equals(java.util.List.of(expect)), "8 stores in order: " + got);
        idx = 0;
        for (CodeElement e : els) if (e instanceof StoreInstruction x) {
            check(x.typeKind() == tks[idx], expect[idx] + " typeKind " + tks[idx]);
            check(x.slot() == slots[idx], expect[idx] + " slot " + slots[idx]);
            check(x.sizeInBytes() == (x.opcode() == Opcode.ASTORE ? 2 : 1), expect[idx] + " sizeInBytes");
            idx++;
        }
        // wide：istore(300) → ISTORE_W size4
        els = emit("wide", MethodTypeDesc.ofDescriptor("()V"), cob -> cob.iconst_1().istore(300).lconst_0().lstore(300).return_());
        java.util.List<Opcode> wideOps = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof StoreInstruction x) {
            wideOps.add(x.opcode());
            check(x.slot() == 300, x.opcode() + " slot 300");
            check(x.sizeInBytes() == 4, x.opcode() + " sizeInBytes 4");
        }
        check(wideOps.equals(java.util.List.of(Opcode.ISTORE_W, Opcode.LSTORE_W)), "wide stores " + wideOps);
        // 通用 storeLocal(TypeKind, slot)
        els = emit("gen", MethodTypeDesc.ofDescriptor("(I)V"), cob -> cob.iload(0).storeLocal(TypeKind.INT, 5).return_());
        boolean found = false;
        for (CodeElement e : els) if (e instanceof StoreInstruction x && x.opcode() == Opcode.ISTORE && x.slot() == 5) found = true;
        check(found, "storeLocal(INT,5) emits ISTORE 5");
    }

    // ---- Part 4: 工厂 typeKind→opcode 全表 + 负例 ----
    static void testFactoryNegative() {
        check(StoreInstruction.of(TypeKind.INT, 0).opcode() == Opcode.ISTORE_0, "of(INT,0) ISTORE_0");
        check(StoreInstruction.of(TypeKind.INT, 5).opcode() == Opcode.ISTORE, "of(INT,5) ISTORE");
        check(StoreInstruction.of(TypeKind.LONG, 5).opcode() == Opcode.LSTORE, "of(LONG,5) LSTORE");
        check(StoreInstruction.of(TypeKind.FLOAT, 5).opcode() == Opcode.FSTORE, "of(FLOAT,5) FSTORE");
        check(StoreInstruction.of(TypeKind.DOUBLE, 5).opcode() == Opcode.DSTORE, "of(DOUBLE,5) DSTORE");
        check(StoreInstruction.of(TypeKind.REFERENCE, 5).opcode() == Opcode.ASTORE, "of(REF,5) ASTORE");
        check(StoreInstruction.of(TypeKind.REFERENCE, 7).slot() == 7, "of(REF,7) slot");
        check(StoreInstruction.of(TypeKind.LONG, 300).opcode() == Opcode.LSTORE_W, "of(LONG,300) LSTORE_W");
        check(StoreInstruction.of(TypeKind.SHORT, 1).opcode() == Opcode.ISTORE_1, "of(SHORT,1) narrows ISTORE_1");
        check(StoreInstruction.of(Opcode.ISTORE, 5).opcode() == Opcode.ISTORE, "of(ISTORE,5)");
        try { StoreInstruction.of(TypeKind.VOID, 0); check(false, "of(VOID,0) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(VOID,0) IAE"); }
        try { StoreInstruction.of((TypeKind) null, 5); check(false, "of(null,5) must throw"); }
        catch (NullPointerException e) { check(true, "of((TypeKind)null,5) NPE"); }
        try { StoreInstruction.of(TypeKind.INT, -1); check(false, "of(INT,-1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(INT,-1) IAE negative slot"); }
        try { StoreInstruction.of(Opcode.ISTORE, 300); check(false, "of(ISTORE,300) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(ISTORE,300) IAE slot needs wide"); }
        try { StoreInstruction.of((Opcode) null, 5); check(false, "of((Opcode)null,5) must throw"); }
        catch (NullPointerException e) { check(true, "of((Opcode)null,5) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
