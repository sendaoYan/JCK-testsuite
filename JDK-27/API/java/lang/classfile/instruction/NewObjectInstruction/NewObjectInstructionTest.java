/*
 * @test
 * @summary java.lang.classfile.instruction.NewObjectInstruction conformance: white-box structure + black-box behavior.
 * @run main NewObjectInstructionTest
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

public class NewObjectInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNEW" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.NewObjectInstruction");
        check(c != null, "java.lang.classfile.instruction.NewObjectInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.NewObjectInstruction"), "binary name java.lang.classfile.instruction.NewObjectInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.NewObjectInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.NewObjectInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.NewObjectInstruction has no superclass");
        check(declares(c, "className"), "declares className");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.StringBuilder"));
        NewObjectInstruction i = NewObjectInstruction.of(ce);
        check(i.opcode() == Opcode.NEW, "opcode");
        check(i.className() == ce, "className");
        check(i.className().asInternalName().equals("java/lang/StringBuilder"), "internal name");

    }


    // ---- Part 3: 发射-回读（核心）：完整 new 序列 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), cob ->
                cob.new_(ClassDesc.of("java.lang.Object")).dup()
                   .invokespecial(ClassDesc.of("java.lang.Object"), "<init>", MethodTypeDesc.ofDescriptor("()V")).areturn());
        NewObjectInstruction i = null;
        for (CodeElement e : els) if (e instanceof NewObjectInstruction x) i = x;
        check(i != null, "new_ emitted + read back");
        check(i.opcode() == Opcode.NEW, "NEW opcode");
        check(i.sizeInBytes() == 3, "NEW sizeInBytes 3");
        check(i.opcode().kind() == Opcode.Kind.NEW_OBJECT, "kind NEW_OBJECT");
        check(i.className().asInternalName().equals("java/lang/Object"), "className internal");
        check(i.className().asSymbol().descriptorString().equals("Ljava/lang/Object;"), "className symbol");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        check(!((Object) i instanceof PseudoInstruction), "not PseudoInstruction");
        // StringBuilder 完整构造序列
        els = emit("sb", MethodTypeDesc.ofDescriptor("()Ljava/lang/StringBuilder;"), cob ->
                cob.new_(ClassDesc.of("java.lang.StringBuilder")).dup()
                   .invokespecial(ClassDesc.of("java.lang.StringBuilder"), "<init>", MethodTypeDesc.ofDescriptor("()V")).areturn());
        for (CodeElement e : els) if (e instanceof NewObjectInstruction x)
            check(x.className().asInternalName().equals("java/lang/StringBuilder"), "StringBuilder className");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.StringBuilder"));
        check(NewObjectInstruction.of(ce).className() == ce, "of identity");
        check(NewObjectInstruction.of(ce).opcode() == Opcode.NEW, "of opcode NEW");
        check(NewObjectInstruction.of(ce).sizeInBytes() == 3, "of size 3");
        try { NewObjectInstruction.of((ClassEntry) null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        // 数组类名被工厂接受与否（记录行为）
        ClassEntry arr = cpb.classEntry(ClassDesc.ofDescriptor("[I"));
        try {
            NewObjectInstruction n = NewObjectInstruction.of(arr);
            check(n.className().asInternalName().equals("[I"), "array class accepted, recorded");
        } catch (Exception ex) {
            check(true, "array class rejected: " + ex.getClass().getSimpleName());
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
