/*
 * @test
 * @summary java.lang.classfile.instruction.IncrementInstruction conformance: white-box structure + black-box behavior.
 * @run main IncrementInstructionTest
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

public class IncrementInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmINC" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.IncrementInstruction");
        check(c != null, "java.lang.classfile.instruction.IncrementInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.IncrementInstruction"), "binary name java.lang.classfile.instruction.IncrementInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.IncrementInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.IncrementInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.IncrementInstruction has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        IncrementInstruction i = IncrementInstruction.of(2, 5);
        check(i.opcode() == Opcode.IINC, "opcode");
        check(i.slot() == 2, "slot");
        check(i.constant() == 5, "constant");
        check(IncrementInstruction.of(Opcode.IINC_W, 3, -1).opcode() == Opcode.IINC_W, "of(opcode)");

    }


    // ---- Part 3: 发射-回读（核心）：普通 + wide 自动 + 负 delta ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()V"), cob ->
                cob.iinc(1, 1).iinc(256, 32767).iinc(2, -1).return_());
        java.util.List<IncrementInstruction> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof IncrementInstruction x) got.add(x);
        check(got.size() == 3, "3 iinc emitted, got " + got.size());
        check(got.get(0).opcode() == Opcode.IINC, "iinc(1,1) IINC");
        check(got.get(0).slot() == 1 && got.get(0).constant() == 1, "iinc(1,1) slot/constant");
        check(got.get(0).sizeInBytes() == 3, "IINC sizeInBytes 3");
        check(got.get(0).opcode().kind() == Opcode.Kind.INCREMENT, "kind INCREMENT");
        check(got.get(1).opcode() == Opcode.IINC_W, "iinc(256,32767) auto IINC_W");
        check(got.get(1).slot() == 256 && got.get(1).constant() == 32767, "IINC_W slot 256 / const 32767");
        check(got.get(1).sizeInBytes() == 6, "IINC_W sizeInBytes 6");
        check(got.get(2).opcode() == Opcode.IINC, "iinc(2,-1) stays IINC");
        check(got.get(2).constant() == -1, "negative delta -1");
        check(got.get(2).sizeInBytes() == 3, "negative delta still size 3");
        for (IncrementInstruction x : got) {
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
            check(!((Object) x instanceof PseudoInstruction), x.opcode() + " not PseudoInstruction");
        }
        // 访问器实名是 constant()（非 delta）
        check(IncrementInstruction.class.getMethods().length > 0, "reflective surface");
        boolean hasConstant = false, hasDelta = false;
        for (Method m : IncrementInstruction.class.getMethods()) {
            if (m.getName().equals("constant")) hasConstant = true;
            if (m.getName().equals("delta")) hasDelta = true;
        }
        check(hasConstant, "accessor named constant()");
        check(!hasDelta, "no delta() accessor");
        // javac 真产物：iincHelper 的 x++ 产生 IINC
        byte[] self = IncrementInstructionTest.class.getResourceAsStream("IncrementInstructionTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.methodName().stringValue().equals("iincHelper") && mm.code().isPresent())
                for (CodeElement e : mm.code().get().elementList())
                    if (e instanceof IncrementInstruction x && x.opcode() == Opcode.IINC && x.constant() == 1) found = true;
        check(found, "javac x++ emits IINC const 1");
    }

    // ---- Part 4: 工厂 + 负例边界 ----
    static void testFactoryNegative() {
        check(IncrementInstruction.of(0, 0).opcode() == Opcode.IINC, "of(0,0) IINC");
        check(IncrementInstruction.of(255, 127).opcode() == Opcode.IINC, "of(255,127) IINC slot+const boundary");
        check(IncrementInstruction.of(1, 128).opcode() == Opcode.IINC_W, "of(1,128) const>127 forces IINC_W");
        check(IncrementInstruction.of(1, -129).opcode() == Opcode.IINC_W, "of(1,-129) const<-128 forces IINC_W");
        check(IncrementInstruction.of(1, -128).opcode() == Opcode.IINC, "of(1,-128) IINC const boundary");
        check(IncrementInstruction.of(256, 1).opcode() == Opcode.IINC_W, "of(256,1) slot forces IINC_W");
        check(IncrementInstruction.of(1, 32767).opcode() == Opcode.IINC_W, "of(1,32767) IINC_W const boundary");
        check(IncrementInstruction.of(1, -32768).opcode() == Opcode.IINC_W, "of(1,-32768) IINC_W const boundary");
        check(IncrementInstruction.of(Opcode.IINC_W, 1, 1).opcode() == Opcode.IINC_W, "of(IINC_W,1,1) IINC_W");
        check(IncrementInstruction.of(Opcode.IINC_W, 1, 1).sizeInBytes() == 6, "of(IINC_W,..) size 6");
        try { IncrementInstruction.of(-1, 1); check(false, "of(-1,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(-1,1) IAE"); }
        try { IncrementInstruction.of(0, 32768); check(false, "of(0,32768) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(0,32768) IAE const out of short"); }
        try { IncrementInstruction.of(0, -32769); check(false, "of(0,-32769) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(0,-32769) IAE"); }
        try { IncrementInstruction.of(Opcode.IINC, 300, 1); check(false, "of(IINC,300,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IINC,300,1) IAE slot needs wide"); }
        try { IncrementInstruction.of(Opcode.NOP, 1, 1); check(false, "of(NOP,1,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP,1,1) IAE wrong kind"); }
        try { IncrementInstruction.of((Opcode) null, 1, 1); check(false, "of((Opcode)null,..) must throw"); }
        catch (NullPointerException e) { check(true, "of((Opcode)null,..) NPE"); }
    }

    static int iincHelper(int x) { return x++; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
