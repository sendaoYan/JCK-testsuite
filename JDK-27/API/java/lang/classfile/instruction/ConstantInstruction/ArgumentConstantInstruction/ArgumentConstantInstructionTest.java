/*
 * @test
 * @summary java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction conformance: white-box structure + black-box behavior.
 * @run main ArgumentConstantInstructionTest
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

public class ArgumentConstantInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmACI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ConstantInstruction$ArgumentConstantInstruction");
        check(c != null, "java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ConstantInstruction$ArgumentConstantInstruction"), "binary name java.lang.classfile.instruction.ConstantInstruction$ArgumentConstantInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantInstruction.ArgumentConstantInstruction i = ConstantInstruction.ofArgument(Opcode.SIPUSH, 300);
        check(i.opcode() == Opcode.SIPUSH, "opcode");
        check(i.constantValue().equals(300), "constantValue 300");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");

    }


    // ---- Part 3: 发射-回读矩阵（核心） ----
    static void testEmitReadback() throws Exception {
        // bipush(7) → BIPUSH size2 operand 7
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()I"), cob -> cob.bipush(7).ireturn());
        ConstantInstruction.ArgumentConstantInstruction i = null;
        for (CodeElement e : els) if (e instanceof ConstantInstruction.ArgumentConstantInstruction x) i = x;
        check(i != null, "bipush emitted + read back");
        check(i.opcode() == Opcode.BIPUSH, "BIPUSH opcode");
        check(i.sizeInBytes() == 2, "BIPUSH sizeInBytes 2");
        check(i.constantValue().equals(Integer.valueOf(7)), "BIPUSH operand 7");
        check(i.typeKind() == TypeKind.INT, "BIPUSH typeKind INT");
        check(i instanceof ConstantInstruction, "instanceof ConstantInstruction");
        check(!((Object) i instanceof ConstantInstruction.IntrinsicConstantInstruction)
           && !((Object) i instanceof ConstantInstruction.LoadConstantInstruction), "exactly Argument");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        check(i.opcode().kind() == Opcode.Kind.CONSTANT, "kind CONSTANT");
        // sipush(1000) → SIPUSH size3
        els = emit("s", MethodTypeDesc.ofDescriptor("()I"), cob -> cob.sipush(1000).ireturn());
        for (CodeElement e : els) if (e instanceof ConstantInstruction.ArgumentConstantInstruction x) {
            check(x.opcode() == Opcode.SIPUSH, "SIPUSH opcode");
            check(x.sizeInBytes() == 3, "SIPUSH sizeInBytes 3");
            check(x.constantValue().equals(Integer.valueOf(1000)), "SIPUSH operand 1000");
            check(x.typeKind() == TypeKind.INT, "SIPUSH typeKind INT");
        }
        // 负值边界发射回读
        els = emit("neg", MethodTypeDesc.ofDescriptor("()I"), cob -> cob.bipush(-128).pop().bipush(127).pop().sipush(-32768).pop().sipush(32767).ireturn());
        java.util.List<Integer> vals = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof ConstantInstruction.ArgumentConstantInstruction x) vals.add(x.constantValue());
        check(vals.equals(java.util.List.of(-128, 127, -32768, 32767)), "boundary values " + vals);
        // javac 真产物：iconst 装不下时自动用 BIPUSH/SIPUSH
        byte[] self = ArgumentConstantInstructionTest.class.getResourceAsStream("ArgumentConstantInstructionTest.class").readAllBytes();
        boolean foundB = false, foundS = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.code().isPresent()) continue;
            for (CodeElement e : mm.code().get().elementList())
                if (e instanceof ConstantInstruction.ArgumentConstantInstruction x) {
                    if (x.opcode() == Opcode.BIPUSH && x.constantValue().equals(Integer.valueOf(100))) foundB = true;
                    if (x.opcode() == Opcode.SIPUSH && x.constantValue().equals(Integer.valueOf(1000))) foundS = true;
                }
        }
        check(foundB, "javac uses BIPUSH for literal 100 (helper bipush100)");
        check(foundS, "javac uses SIPUSH for literal 1000 (helper sipush1000)");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        ConstantInstruction.ArgumentConstantInstruction b = ConstantInstruction.ofArgument(Opcode.BIPUSH, 100);
        check(b.opcode() == Opcode.BIPUSH && b.constantValue().equals(Integer.valueOf(100)) && b.typeKind() == TypeKind.INT, "of(BIPUSH,100)");
        check(ConstantInstruction.ofArgument(Opcode.SIPUSH, 32767).sizeInBytes() == 3, "of(SIPUSH,32767) size 3");
        check(ConstantInstruction.ofArgument(Opcode.BIPUSH, -128).sizeInBytes() == 2, "of(BIPUSH,-128) size 2");
        try { ConstantInstruction.ofArgument(Opcode.BIPUSH, 128); check(false, "BIPUSH 128 must throw"); }
        catch (IllegalArgumentException e) { check(true, "BIPUSH 128 IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.BIPUSH, -129); check(false, "BIPUSH -129 must throw"); }
        catch (IllegalArgumentException e) { check(true, "BIPUSH -129 IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.SIPUSH, 32768); check(false, "SIPUSH 32768 must throw"); }
        catch (IllegalArgumentException e) { check(true, "SIPUSH 32768 IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.SIPUSH, -32769); check(false, "SIPUSH -32769 must throw"); }
        catch (IllegalArgumentException e) { check(true, "SIPUSH -32769 IAE"); }
        try { ConstantInstruction.ofArgument(null, 1); check(false, "of(null,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(null,1) IAE"); }
        try { ConstantInstruction.ofArgument(Opcode.ICONST_1, 1); check(false, "of(ICONST_1,1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(ICONST_1,1) IAE wrong kind"); }
    }

    static int bipush100() { return 100; }
    static int sipush1000() { return 1000; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
