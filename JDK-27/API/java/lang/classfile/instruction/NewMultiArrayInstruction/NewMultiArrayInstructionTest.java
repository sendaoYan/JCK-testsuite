/*
 * @test
 * @summary java.lang.classfile.instruction.NewMultiArrayInstruction conformance: white-box structure + black-box behavior.
 * @run main NewMultiArrayInstructionTest
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

public class NewMultiArrayInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNMA" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.NewMultiArrayInstruction");
        check(c != null, "java.lang.classfile.instruction.NewMultiArrayInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.NewMultiArrayInstruction"), "binary name java.lang.classfile.instruction.NewMultiArrayInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.NewMultiArrayInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.NewMultiArrayInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.NewMultiArrayInstruction has no superclass");
        check(declares(c, "arrayType"), "declares arrayType");
        check(declares(c, "dimensions"), "declares dimensions");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.ofDescriptor("[[I"));
        NewMultiArrayInstruction i = NewMultiArrayInstruction.of(ce, 2);
        check(i.opcode() == Opcode.MULTIANEWARRAY, "opcode");
        check(i.arrayType() == ce, "arrayType");
        check(i.dimensions() == 2, "dimensions");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", ConstantDescs.MTD_void, cob ->
                cob.iconst_2().iconst_2().multianewarray(ClassDesc.ofDescriptor("[[I"), 2).pop().return_());
        NewMultiArrayInstruction i = null;
        for (CodeElement e : els) if (e instanceof NewMultiArrayInstruction x) i = x;
        check(i != null, "multianewarray emitted + read back");
        check(i.opcode() == Opcode.MULTIANEWARRAY, "MULTIANEWARRAY opcode");
        check(i.sizeInBytes() == 4, "MULTIANEWARRAY sizeInBytes 4");
        check(i.opcode().kind() == Opcode.Kind.NEW_MULTI_ARRAY, "kind NEW_MULTI_ARRAY");
        check(i.dimensions() == 2, "dimensions 2");
        check(i.arrayType().asInternalName().equals("[[I"), "arrayType [[I");
        check(i.arrayType().asSymbol().descriptorString().equals("[[I"), "arrayType symbol [[I");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        // 3 维
        els = emit("d3", ConstantDescs.MTD_void, cob ->
                cob.iconst_1().iconst_2().iconst_3().multianewarray(ClassDesc.ofDescriptor("[[[Ljava/lang/String;"), 3).pop().return_());
        for (CodeElement e : els) if (e instanceof NewMultiArrayInstruction x) {
            check(x.dimensions() == 3, "dimensions 3");
            check(x.arrayType().asInternalName().equals("[[[Ljava/lang/String;"), "3-dim ref array type");
        }
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.ofDescriptor("[[I"));
        check(NewMultiArrayInstruction.of(ce, 2).arrayType() == ce, "of identity");
        check(NewMultiArrayInstruction.of(ce, 1).dimensions() == 1, "of dims 1");
        check(NewMultiArrayInstruction.of(ce, 2).sizeInBytes() == 4, "of size 4");
        try { NewMultiArrayInstruction.of(null, 2); check(false, "of(null,2) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,2) NPE"); }
        try { NewMultiArrayInstruction.of(ce, 0); check(false, "of(ce,0) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(ce,0) IAE dimensions < 1"); }
        try { NewMultiArrayInstruction.of(ce, -1); check(false, "of(ce,-1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(ce,-1) IAE"); }
        // dims > rank 不经工厂校验（字节码层面合法范围 1-255）
        check(NewMultiArrayInstruction.of(ce, 3).dimensions() == 3, "dims>rank accepted by factory (recorded behavior)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
