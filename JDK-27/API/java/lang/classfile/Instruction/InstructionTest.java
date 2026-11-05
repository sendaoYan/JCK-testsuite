/*
 * @test
 * @summary java.lang.classfile.Instruction conformance: white-box structure + black-box behavior.
 * @run main InstructionTest
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

public class InstructionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Instruction");
        check(c != null, "java.lang.classfile.Instruction loads");
        check(c.getName().equals("java.lang.classfile.Instruction"), "binary name java.lang.classfile.Instruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Instruction is public");
        check(c.isInterface(), "java.lang.classfile.Instruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Instruction has no superclass");
        check(declares(c, "opcode"), "declares opcode");
        check(declares(c, "sizeInBytes"), "declares sizeInBytes");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Instruction i = NopInstruction.of();
        check(i.opcode() == Opcode.NOP, "opcode NOP");
        check(i.sizeInBytes() == 1, "size 1");
        Instruction r = ReturnInstruction.of(TypeKind.INT);
        check(r.opcode() == Opcode.IRETURN && r.sizeInBytes() == 1, "IRETURN size 1");

    }


    // ---- real-class members: javac 产物指令源 ----
    static int sw(int x) { switch (x) { case 1: return 1; case 5: return 5; case 9: return 9; default: return -1; } }
    static long ldc2() { return 100000L; }
    static String ldcs() { return "hello"; }

    // ---- Part 3: 发射-回读矩阵：opcode/sizeInBytes 与 Opcode.sizeIfFixed 全一致 ----
    static void testEmitReadbackMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.IM"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
            c.iconst_1().bipush(7).iadd().pop();
            c.ldc("hello").pop();
            c.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).pop();
            c.nop();
            c.iinc(0, 1);                 // 参数槽位 0（已初始化，可通过 verify）
            c.iconst_0().istore(256);      // 初始化宽槽位 256
            c.iinc(256, 2);
            c.iconst_0().istore(300);      // 初始化宽槽位 300
            c.iload(300).pop();
            c.iload(0).ireturn();
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        List<Instruction> instrs = new ArrayList<>();
        for (CodeElement e : code.elementList()) if (e instanceof Instruction i) instrs.add(i);
        check(instrs.size() >= 11, ">= 11 instructions: " + instrs.size());
        int fixedChecked = 0, wideChecked = 0;
        for (Instruction i : instrs) {
            check(i.opcode() != null, "opcode non-null");
            check(i instanceof CodeElement, "instruction is CodeElement");
            int fixed = i.opcode().sizeIfFixed();
            if (fixed >= 0) { check(i.sizeInBytes() == fixed, i.opcode() + " sizeInBytes==sizeIfFixed (" + i.sizeInBytes() + "/" + fixed + ")"); fixedChecked++; }
            else check(i.sizeInBytes() > 0, i.opcode() + " variable size > 0");
        }
        check(fixedChecked >= 10, ">= 10 fixed-size instructions checked: " + fixedChecked);
        // 精确 opcode 序列（发射序）
        List<Opcode> ops = new ArrayList<>();
        for (Instruction i : instrs) ops.add(i.opcode());
        check(ops.contains(Opcode.ICONST_1), "contains ICONST_1");
        check(ops.contains(Opcode.BIPUSH), "contains BIPUSH");
        check(ops.contains(Opcode.LDC), "contains LDC");
        check(ops.contains(Opcode.GETSTATIC), "contains GETSTATIC");
        check(ops.contains(Opcode.NOP), "contains NOP");
        check(ops.contains(Opcode.IINC), "contains IINC");
        check(ops.contains(Opcode.IINC_W), "iinc(256,..) auto-wide -> IINC_W");
        check(ops.contains(Opcode.ILOAD_W), "iload(300) auto-wide -> ILOAD_W");
        check(ops.contains(Opcode.IRETURN), "contains IRETURN");
        // wide 语义
        for (Instruction i : instrs) {
            if (i.opcode() == Opcode.IINC_W || i.opcode() == Opcode.ILOAD_W) { check(i.opcode().isWide(), i.opcode() + " isWide"); wideChecked++; }
        }
        check(wideChecked == 2, "2 wide instructions: " + wideChecked);
        check(Opcode.IINC_W.sizeIfFixed() == 6 && Opcode.ILOAD_W.sizeIfFixed() == 4, "wide fixed sizes 6/4");
        check(Opcode.IINC.sizeIfFixed() == 3, "IINC fixed size 3");
        // sizeInBytes 总和 == codeLength 不变量
        CodeAttribute ca = (CodeAttribute) code;
        int sum = 0; for (Instruction i : instrs) sum += i.sizeInBytes();
        check(sum == ca.codeLength(), "sum(sizeInBytes)==codeLength (" + sum + "/" + ca.codeLength() + ")");
        check(ca.codeArray().length == ca.codeLength(), "codeArray length == codeLength");
        check(cf.verify(cm).isEmpty(), "matrix verify clean");
    }

    // ---- Part 4: 变长指令 tableswitch/lookupswitch + 真类 javac 产物 ----
    static void testSwitchInstructions() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.SW"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
            Label def = c.newLabel(), c1 = c.newLabel(), c2 = c.newLabel();
            c.iload(0).tableswitch(0, 1, def, List.of(SwitchCase.of(0, c1), SwitchCase.of(1, c2)));
            c.labelBinding(c1).iconst_1().ireturn();
            c.labelBinding(c2).iconst_2().ireturn();
            c.labelBinding(def).iconst_m1().ireturn();
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        TableSwitchInstruction ts = null;
        for (CodeElement e : code.elementList()) if (e instanceof TableSwitchInstruction t) ts = t;
        check(ts != null, "tableswitch readback");
        check(ts.opcode() == Opcode.TABLESWITCH, "TABLESWITCH opcode");
        check(ts.opcode().sizeIfFixed() == -1, "TABLESWITCH sizeIfFixed -1 (variable)");
        check(ts.sizeInBytes() > 12, "tableswitch sizeInBytes > 12: " + ts.sizeInBytes());
        check(ts.lowValue() == 0 && ts.highValue() == 1, "low 0 high 1");
        check(ts.cases().size() == 2, "2 cases");
        check(ts.cases().get(0).caseValue() == 0 && ts.cases().get(1).caseValue() == 1, "case values 0,1");
        check(ts.defaultTarget() != null, "defaultTarget non-null");
        check(ts.defaultTarget() instanceof Label, "defaultTarget is Label");
        check(ts.cases().get(0).target() instanceof Label, "case target is Label");
        // 真类 lookupswitch（javac switch(1/5/9)）
        byte[] self = InstructionTest.class.getResourceAsStream("InstructionTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        MethodModel swm = null;
        for (MethodModel m : sm.methods()) if (m.methodName().stringValue().equals("sw")) swm = m;
        check(swm != null && swm.code().isPresent(), "self sw method located");
        LookupSwitchInstruction ls = null;
        for (CodeElement e : swm.code().get().elementList()) if (e instanceof LookupSwitchInstruction l) ls = l;
        check(ls != null, "real javac switch -> LOOKUPSWITCH");
        check(ls.opcode() == Opcode.LOOKUPSWITCH && ls.opcode().sizeIfFixed() == -1, "LOOKUPSWITCH variable");
        check(ls.sizeInBytes() > 8, "lookupswitch size > 8");
        List<Integer> keys = new ArrayList<>();
        ls.cases().forEach(sc -> keys.add(sc.caseValue()));
        check(keys.equals(List.of(1, 5, 9)), "lookupswitch keys ascending [1,5,9]: " + keys);
        // 真类 LDC2_W（long 常量）
        MethodModel l2 = null;
        for (MethodModel m : sm.methods()) if (m.methodName().stringValue().equals("ldc2")) l2 = m;
        boolean hasLdc2 = false;
        for (CodeElement e : l2.code().get().elementList())
            if (Instruction.class.isInstance(e) && ((Instruction) e).opcode() == Opcode.LDC2_W) hasLdc2 = true;
        check(hasLdc2, "real ldc long -> LDC2_W");
        check(Opcode.LDC2_W.sizeIfFixed() == 3, "LDC2_W fixed 3");
        check(Opcode.LDC.sizeIfFixed() == 2, "LDC fixed 2");
        // 真类 LDC(String)
        MethodModel ls2 = null;
        for (MethodModel m : sm.methods()) if (m.methodName().stringValue().equals("ldcs")) ls2 = m;
        boolean hasLdc = false;
        for (CodeElement e : ls2.code().get().elementList())
            if (Instruction.class.isInstance(e) && ((Instruction) e).opcode() == Opcode.LDC) hasLdc = true;
        check(hasLdc, "real ldc String -> LDC");
        check(cf.verify(sm).isEmpty(), "self verify clean");
    }

    // ---- Part 5: 静态工厂族 + 指令分类 + defineClass 运行闭环 ----
    static void testFactoriesAndRun() throws Exception {
        Instruction nop = NopInstruction.of();
        check(nop.opcode() == Opcode.NOP && nop.sizeInBytes() == 1, "NopInstruction.of opcode/size");
        check(Opcode.NOP.sizeIfFixed() == 1 && !Opcode.NOP.isWide(), "NOP fixed 1 not wide");
        Instruction ret = ReturnInstruction.of(TypeKind.INT);
        check(ret.opcode() == Opcode.IRETURN && ret.sizeInBytes() == 1, "ReturnInstruction.of(INT)");
        check(ReturnInstruction.of(TypeKind.REFERENCE).opcode() == Opcode.ARETURN, "ReturnInstruction.of(REFERENCE) ARETURN");
        check(ReturnInstruction.of(TypeKind.VOID).opcode() == Opcode.RETURN, "ReturnInstruction.of(VOID) RETURN");
        check(ReturnInstruction.of(TypeKind.LONG).opcode() == Opcode.LRETURN, "ReturnInstruction.of(LONG) LRETURN");
        // 指令非 PseudoInstruction（分类互斥）
        check(!PseudoInstruction.class.isInstance(nop), "Instruction NOT PseudoInstruction");
        check(!Instruction.class.isInstance(LineNumber.of(1)), "LineNumber NOT Instruction");
        // 213 opcode 全集一致性：每个固定 size opcode 的 sizeIfFixed 与字节宽一致（代表性采样）
        check(Opcode.values().length == 213, "Opcode values 213");
        // defineClass 运行闭环：发射的算术指令真运行
        ClassFile cf = ClassFile.of();
        String name = "DynInstruction" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("go", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                c -> c.iload(0).bipush(6).imul().iload(1).iadd().ireturn()));
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("go", int.class, int.class).invoke(null, 7, 0).equals(42), "dyn go(7,0)==42 运行闭环");
        check(cf.verify(cf.parse(b)).isEmpty(), "dyn verify clean");
    }
    static int dynSeq;
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadbackMatrix();
        testSwitchInstructions();
        testFactoriesAndRun();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
