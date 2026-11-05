/*
 * @test
 * @summary java.lang.classfile.instruction.LoadInstruction conformance: white-box structure + black-box behavior.
 * @run main LoadInstructionTest
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

public class LoadInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLDI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LoadInstruction");
        check(c != null, "java.lang.classfile.instruction.LoadInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.LoadInstruction"), "binary name java.lang.classfile.instruction.LoadInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LoadInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LoadInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LoadInstruction has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        LoadInstruction i = LoadInstruction.of(TypeKind.INT, 5);
        check(i.opcode() == Opcode.ILOAD, "opcode ILOAD");
        check(i.slot() == 5, "slot 5");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(LoadInstruction.of(TypeKind.LONG, 1).typeKind().slotSize() == 2, "LONG slot size 2");

    }


    // ---- Part 3: 发射-回读（核心）：_0/_1 快捷 + wide 自动 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("(IJFDLjava/lang/Object;)V"), cob -> {
            cob.iload(0).pop();
            cob.iload(1).pop();
            cob.iload(2).pop();
            cob.iload(3).pop();
            cob.lload(1).pop2();
            cob.fload(1).pop();
            cob.dload(1).pop2();
            cob.aload(4).pop();
            cob.return_();
        });
        Opcode[] expect = {Opcode.ILOAD_0, Opcode.ILOAD_1, Opcode.ILOAD_2, Opcode.ILOAD_3, Opcode.LLOAD_1,
                Opcode.FLOAD_1, Opcode.DLOAD_1, Opcode.ALOAD};
        TypeKind[] tks = {TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.LONG,
                TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.REFERENCE};
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof LoadInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == (x.opcode() == Opcode.ALOAD ? 2 : 1), x.opcode() + " sizeInBytes");
            check(x.opcode().kind() == Opcode.Kind.LOAD, x.opcode() + " kind LOAD");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
        }
        check(got.equals(java.util.List.of(expect)), "8 loads in order: " + got);
        for (int i = 0; i < expect.length; i++)
            for (CodeElement e : els) if (e instanceof LoadInstruction x && x.opcode() == expect[i])
                check(x.typeKind() == tks[i], expect[i] + " typeKind " + tks[i]);
        // slot 回读
        els = emit("slots", MethodTypeDesc.ofDescriptor("(I)V"), cob -> cob.iload(0).pop().iload(1).pop().iload(4).pop().return_());
        java.util.List<Integer> slots = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof LoadInstruction x) slots.add(x.slot());
        check(slots.equals(java.util.List.of(0, 1, 4)), "slot readback " + slots);
        // wide：iload(300) → ILOAD_W size4
        els = emit("wide", MethodTypeDesc.ofDescriptor("()V"), cob -> cob.iload(300).pop().lload(300).pop2().return_());
        java.util.List<Opcode> wideOps = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof LoadInstruction x) {
            wideOps.add(x.opcode());
            check(x.slot() == 300, x.opcode() + " slot 300");
            check(x.sizeInBytes() == 4, x.opcode() + " sizeInBytes 4");
        }
        check(wideOps.equals(java.util.List.of(Opcode.ILOAD_W, Opcode.LLOAD_W)), "wide loads " + wideOps);
        // 通用 loadLocal(TypeKind, slot)
        els = emit("gen", MethodTypeDesc.ofDescriptor("(I)V"), cob -> cob.loadLocal(TypeKind.INT, 0).pop().return_());
        boolean found = false;
        for (CodeElement e : els) if (e instanceof LoadInstruction x && x.opcode() == Opcode.ILOAD_0) found = true;
        check(found, "loadLocal(INT,0) emits ILOAD_0");
    }

    // ---- Part 4: 工厂 typeKind→opcode 全表 + 负例 ----
    static void testFactoryNegative() {
        check(LoadInstruction.of(TypeKind.INT, 0).opcode() == Opcode.ILOAD_0, "of(INT,0) ILOAD_0");
        check(LoadInstruction.of(TypeKind.INT, 5).opcode() == Opcode.ILOAD, "of(INT,5) ILOAD");
        check(LoadInstruction.of(TypeKind.LONG, 5).opcode() == Opcode.LLOAD, "of(LONG,5) LLOAD");
        check(LoadInstruction.of(TypeKind.FLOAT, 5).opcode() == Opcode.FLOAD, "of(FLOAT,5) FLOAD");
        check(LoadInstruction.of(TypeKind.DOUBLE, 5).opcode() == Opcode.DLOAD, "of(DOUBLE,5) DLOAD");
        check(LoadInstruction.of(TypeKind.REFERENCE, 5).opcode() == Opcode.ALOAD, "of(REF,5) ALOAD");
        check(LoadInstruction.of(TypeKind.INT, 300).opcode() == Opcode.ILOAD_W, "of(INT,300) IINC analog ILOAD_W");
        check(LoadInstruction.of(Opcode.ILOAD, 5).opcode() == Opcode.ILOAD, "of(ILOAD,5)");
        check(LoadInstruction.of(Opcode.ILOAD_W, 300).slot() == 300, "of(ILOAD_W,300) slot");
        check(LoadInstruction.of(TypeKind.SHORT, 1).opcode() == Opcode.ILOAD_1, "of(SHORT,1) narrows to ILOAD_1");
        check(LoadInstruction.of(TypeKind.BYTE, 1).opcode() == Opcode.ILOAD_1, "of(BYTE,1) narrows to ILOAD_1");
        check(LoadInstruction.of(TypeKind.CHAR, 1).opcode() == Opcode.ILOAD_1, "of(CHAR,1) narrows to ILOAD_1");
        check(LoadInstruction.of(TypeKind.BOOLEAN, 1).opcode() == Opcode.ILOAD_1, "of(BOOLEAN,1) narrows to ILOAD_1");
        try { LoadInstruction.of(TypeKind.VOID, 0); check(false, "of(VOID,0) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(VOID,0) IAE"); }
        try { LoadInstruction.of((TypeKind) null, 5); check(false, "of(null,5) must throw"); }
        catch (NullPointerException e) { check(true, "of((TypeKind)null,5) NPE"); }
        try { LoadInstruction.of(TypeKind.INT, -1); check(false, "of(INT,-1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(INT,-1) IAE negative slot"); }
        try { LoadInstruction.of(Opcode.ILOAD, 300); check(false, "of(ILOAD,300) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(ILOAD,300) IAE slot needs wide opcode"); }
        try { LoadInstruction.of((Opcode) null, 5); check(false, "of((Opcode)null,5) must throw"); }
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
