/*
 * @test
 * @summary java.lang.classfile.instruction.NewReferenceArrayInstruction conformance: white-box structure + black-box behavior.
 * @run main NewReferenceArrayInstructionTest
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

public class NewReferenceArrayInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNRA" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.NewReferenceArrayInstruction");
        check(c != null, "java.lang.classfile.instruction.NewReferenceArrayInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.NewReferenceArrayInstruction"), "binary name java.lang.classfile.instruction.NewReferenceArrayInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.NewReferenceArrayInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.NewReferenceArrayInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.NewReferenceArrayInstruction has no superclass");
        check(declares(c, "componentType"), "declares componentType");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        NewReferenceArrayInstruction i = NewReferenceArrayInstruction.of(ce);
        check(i.opcode() == Opcode.ANEWARRAY, "opcode");
        check(i.componentType() == ce, "componentType");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()[Ljava/lang/String;"),
                cob -> cob.iconst_2().anewarray(ClassDesc.of("java.lang.String")).areturn());
        NewReferenceArrayInstruction i = null;
        for (CodeElement e : els) if (e instanceof NewReferenceArrayInstruction x) i = x;
        check(i != null, "anewarray emitted + read back");
        check(i.opcode() == Opcode.ANEWARRAY, "ANEWARRAY opcode");
        check(i.sizeInBytes() == 3, "ANEWARRAY sizeInBytes 3");
        check(i.opcode().kind() == Opcode.Kind.NEW_REF_ARRAY, "kind NEW_REF_ARRAY");
        check(i.componentType().asInternalName().equals("java/lang/String"), "componentType internal");
        check(i.componentType().asSymbol().descriptorString().equals("Ljava/lang/String;"), "componentType symbol");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        // 多维引用数组（component 本身是数组类）
        els = emit("arr2", MethodTypeDesc.ofDescriptor("()[[Ljava/lang/String;"),
                cob -> cob.iconst_2().anewarray(ClassDesc.ofDescriptor("[Ljava/lang/String;")).areturn());
        for (CodeElement e : els) if (e instanceof NewReferenceArrayInstruction x)
            check(x.componentType().asInternalName().equals("[Ljava/lang/String;"), "array component type");
        // ClassEntry 重载
        els = emit("ce", MethodTypeDesc.ofDescriptor("()[Ljava/lang/Integer;"), cob -> {
            ClassEntry ce = cob.constantPool().classEntry(ClassDesc.of("java.lang.Integer"));
            cob.iconst_1().anewarray(ce).areturn();
        });
        boolean found = false;
        for (CodeElement e : els) if (e instanceof NewReferenceArrayInstruction x && x.componentType().asInternalName().equals("java/lang/Integer")) found = true;
        check(found, "anewarray(ClassEntry) readback");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check(NewReferenceArrayInstruction.of(ce).componentType() == ce, "of identity");
        check(NewReferenceArrayInstruction.of(ce).opcode() == Opcode.ANEWARRAY, "of opcode");
        check(NewReferenceArrayInstruction.of(ce).sizeInBytes() == 3, "of size 3");
        try { NewReferenceArrayInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
