/*
 * @test
 * @summary java.lang.classfile.instruction.StackInstruction conformance: white-box structure + black-box behavior.
 * @run main StackInstructionTest
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

public class StackInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmSTK" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.StackInstruction");
        check(c != null, "java.lang.classfile.instruction.StackInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.StackInstruction"), "binary name java.lang.classfile.instruction.StackInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.StackInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.StackInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.StackInstruction has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        StackInstruction i = StackInstruction.of(Opcode.POP);
        check(i.opcode() == Opcode.POP, "opcode");
        check(StackInstruction.of(Opcode.DUP).opcode() == Opcode.DUP, "DUP");
        check(StackInstruction.of(Opcode.SWAP).opcode() == Opcode.SWAP, "SWAP");

    }


    // ---- Part 3: 发射-回读（核心）：9 个栈操作 opcode 全表 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", ConstantDescs.MTD_void, cob -> {
            cob.iconst_1().iconst_2().swap().pop2();          // SWAP
            cob.iconst_1().iconst_2().dup().pop2();           // DUP
            cob.lconst_0().dup2().pop2();                     // DUP2
            cob.aconst_null().iconst_1().dup_x1().pop().pop();// DUP_X1
            cob.aconst_null().lconst_0().dup2_x1().pop2().pop(); // DUP2_X1
            cob.iconst_1().iconst_2().iconst_3().dup_x2().pop().pop().pop();   // DUP_X2
            cob.aconst_null().lconst_0().lconst_1().dup2_x2().pop2().pop2().pop(); // DUP2_X2
            cob.iconst_1().pop();                             // POP
            cob.lconst_0().pop2();                            // POP2
            cob.return_();
        });
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof StackInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == 1, x.opcode() + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.STACK, x.opcode() + " kind STACK");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
            check(!((Object) x instanceof PseudoInstruction), x.opcode() + " not PseudoInstruction");
        }
        // 9 个栈 opcode 全部出现（清理用的 pop/pop2 也计入 got，故断言全集 + 首次出现顺序）
        java.util.Set<Opcode> distinct = new java.util.LinkedHashSet<>(got);
        check(distinct.equals(new java.util.LinkedHashSet<>(java.util.List.of(
                Opcode.SWAP, Opcode.POP2, Opcode.DUP, Opcode.DUP2, Opcode.DUP_X1,
                Opcode.POP, Opcode.DUP2_X1, Opcode.DUP_X2, Opcode.DUP2_X2))),
                "9 stack ops all present, first-appearance order: " + distinct);
        check(got.size() == 22, "22 stack instructions total (9 targets + 13 cleanup pops): " + got.size());
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        Opcode[] all = {Opcode.DUP, Opcode.DUP_X1, Opcode.DUP_X2, Opcode.DUP2, Opcode.DUP2_X1, Opcode.DUP2_X2,
                Opcode.POP, Opcode.POP2, Opcode.SWAP};
        for (Opcode op : all) check(StackInstruction.of(op).opcode() == op, "of(" + op + ")");
        try { StackInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        for (Opcode bad : new Opcode[]{Opcode.NOP, Opcode.IADD, Opcode.RETURN}) {
            try { StackInstruction.of(bad); check(false, "of(" + bad + ") must throw"); }
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
