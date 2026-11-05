/*
 * @test
 * @summary java.lang.classfile.instruction.MonitorInstruction conformance: white-box structure + black-box behavior.
 * @run main MonitorInstructionTest
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

public class MonitorInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmMON" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.MonitorInstruction");
        check(c != null, "java.lang.classfile.instruction.MonitorInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.MonitorInstruction"), "binary name java.lang.classfile.instruction.MonitorInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.MonitorInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.MonitorInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.MonitorInstruction has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        MonitorInstruction i = MonitorInstruction.of(Opcode.MONITORENTER);
        check(i.opcode() == Opcode.MONITORENTER, "opcode MONITORENTER");
        check(MonitorInstruction.of(Opcode.MONITOREXIT).opcode() == Opcode.MONITOREXIT, "MONITOREXIT");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", ConstantDescs.MTD_void, cob ->
                cob.ldc("x").monitorenter().ldc("x").monitorexit().return_());
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof MonitorInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == 1, x.opcode() + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.MONITOR, x.opcode() + " kind MONITOR");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
            check(!((Object) x instanceof PseudoInstruction), x.opcode() + " not PseudoInstruction");
        }
        check(got.equals(java.util.List.of(Opcode.MONITORENTER, Opcode.MONITOREXIT)), "enter+exit in order: " + got);
        // getstatic 前置变体
        els = emit("gs", ConstantDescs.MTD_void, cob ->
                cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).dup().monitorenter().monitorexit().return_());
        int n = 0;
        for (CodeElement e : els) if (e instanceof MonitorInstruction) n++;
        check(n == 2, "getstatic+dup+enter+exit");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        check(MonitorInstruction.of(Opcode.MONITORENTER).opcode() == Opcode.MONITORENTER, "of(MONITORENTER)");
        check(MonitorInstruction.of(Opcode.MONITOREXIT).opcode() == Opcode.MONITOREXIT, "of(MONITOREXIT)");
        check(MonitorInstruction.of(Opcode.MONITORENTER).sizeInBytes() == 1, "size 1");
        try { MonitorInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        for (Opcode bad : new Opcode[]{Opcode.NOP, Opcode.IADD, Opcode.ATHROW}) {
            try { MonitorInstruction.of(bad); check(false, "of(" + bad + ") must throw"); }
            catch (IllegalArgumentException e) { check(true, "of(" + bad + ") IAE wrong kind"); }
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
