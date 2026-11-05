/*
 * @test
 * @summary java.lang.classfile.instruction.InvokeInstruction conformance: white-box structure + black-box behavior.
 * @run main InvokeInstructionTest
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

public class InvokeInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmIVI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.InvokeInstruction");
        check(c != null, "java.lang.classfile.instruction.InvokeInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.InvokeInstruction"), "binary name java.lang.classfile.instruction.InvokeInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.InvokeInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.InvokeInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.InvokeInstruction has no superclass");
        check(declares(c, "method"), "declares method");
        check(declares(c, "isInterface"), "declares isInterface");
        check(declares(c, "count"), "declares count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MethodRefEntry mre = cpb.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.of(ConstantDescs.CD_String));
        InvokeInstruction i = InvokeInstruction.of(Opcode.INVOKEVIRTUAL, mre);
        check(i.opcode() == Opcode.INVOKEVIRTUAL, "opcode");
        check(i.method() == mre, "method");
        check(i.name().stringValue().equals("toString"), "name");
        check(i.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_String)), "typeSymbol");
        check(i.count() == 0, "count 0 for invokevirtual");
        check(!i.isInterface(), "isInterface false");

    }


    // ---- Part 3: 发射-回读（核心）：invokestatic/virtual/interface ----
    static void testEmitReadback() throws Exception {
        // invokestatic java.lang.Math.abs(I)I
        java.util.List<CodeElement> els = emit("ist", MethodTypeDesc.ofDescriptor("(I)I"),
                cob -> cob.iconst_m1().invokestatic(ClassDesc.of("java.lang.Math"), "abs", MethodTypeDesc.ofDescriptor("(I)I")).ireturn());
        InvokeInstruction i = null;
        for (CodeElement e : els) if (e instanceof InvokeInstruction x) i = x;
        check(i != null, "invokestatic emitted + read back");
        check(i.opcode() == Opcode.INVOKESTATIC, "INVOKESTATIC opcode");
        check(i.sizeInBytes() == 3, "INVOKESTATIC sizeInBytes 3");
        check(i.opcode().kind() == Opcode.Kind.INVOKE, "kind INVOKE");
        check(i.owner().asInternalName().equals("java/lang/Math"), "owner");
        check(i.name().stringValue().equals("abs"), "name");
        check(i.type().stringValue().equals("(I)I"), "type utf8");
        check(i.typeSymbol().descriptorString().equals("(I)I"), "typeSymbol");
        check(i.typeSymbol().parameterCount() == 1, "parameterCount 1");
        check(i.method() != null, "method() non-null");
        check(!i.isInterface(), "invokestatic isInterface false");
        check(i.count() == 0, "invokestatic count 0");
        // invokevirtual java.lang.String.length()
        els = emit("ivt", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)I"),
                cob -> cob.aload(0).invokevirtual(ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.ofDescriptor("()I")).ireturn());
        for (CodeElement e : els) if (e instanceof InvokeInstruction x) {
            check(x.opcode() == Opcode.INVOKEVIRTUAL, "INVOKEVIRTUAL opcode");
            check(x.owner().asInternalName().equals("java/lang/String"), "owner String");
            check(x.name().stringValue().equals("length"), "name length");
            check(!x.isInterface(), "virtual isInterface false");
        }
        // invokeinterface CharSequence.length()：count==1（receiver）
        els = emit("ifc", MethodTypeDesc.ofDescriptor("(Ljava/lang/CharSequence;)I"),
                cob -> cob.aload(0).invokeinterface(ClassDesc.of("java.lang.CharSequence"), "length", MethodTypeDesc.ofDescriptor("()I")).ireturn());
        for (CodeElement e : els) if (e instanceof InvokeInstruction x) {
            check(x.opcode() == Opcode.INVOKEINTERFACE, "INVOKEINTERFACE opcode");
            check(x.isInterface(), "interface isInterface true");
            check(x.count() == 1, "count 1 (receiver)");
            check(x.sizeInBytes() == 5, "INVOKEINTERFACE sizeInBytes 5");
        }
        // invokeinterface 带实参：count = 1 + argCount
        els = emit("ifc2", MethodTypeDesc.ofDescriptor("(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)I"),
                cob -> cob.aload(0).aload(1).invokeinterface(ClassDesc.of("java.lang.CharSequence"), "compareTo", MethodTypeDesc.ofDescriptor("(Ljava/lang/CharSequence;)I")).ireturn());
        for (CodeElement e : els) if (e instanceof InvokeInstruction x) {
            check(x.count() == 2, "count 2 (receiver + 1 arg)");
            check(x.method() instanceof InterfaceMethodRefEntry, "method() InterfaceMethodRefEntry");
        }
        check(els.stream().filter(e -> e instanceof InvokeInstruction).count() == 1, "last emission has 1 invoke");
    }

    // ---- Part 4: 工厂 3 重载 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MethodRefEntry mr = cpb.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        InvokeInstruction a = InvokeInstruction.of(Opcode.INVOKEVIRTUAL, mr);
        check(a.opcode() == Opcode.INVOKEVIRTUAL && a.method() == mr, "of(op,MemberRefEntry) identity");
        ClassEntry owner = cpb.classEntry(ClassDesc.of("java.lang.CharSequence"));
        InvokeInstruction b2 = InvokeInstruction.of(Opcode.INVOKEINTERFACE, owner, cpb.utf8Entry("length"), cpb.utf8Entry("()I"), true);
        check(b2.opcode() == Opcode.INVOKEINTERFACE, "of(op,ClassEntry,Utf8,Utf8,bool) opcode");
        check(b2.isInterface() && b2.name().stringValue().equals("length") && b2.type().stringValue().equals("()I"), "5-arg accessors");
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("run", MethodTypeDesc.ofDescriptor("()V"));
        InvokeInstruction c = InvokeInstruction.of(Opcode.INVOKESTATIC, owner, nat, false);
        check(c.name().stringValue().equals("run") && c.type().stringValue().equals("()V"), "of(op,ClassEntry,NameAndType,bool) accessors");
        try { InvokeInstruction.of(null, mr); check(false, "of(null,mr) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,mr) NPE"); }
        try { InvokeInstruction.of(Opcode.INVOKESTATIC, (MemberRefEntry) null); check(false, "of(INVOKESTATIC,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(INVOKESTATIC,null) NPE"); }
        try { InvokeInstruction.of(Opcode.NOP, mr); check(false, "of(NOP,mr) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP,mr) IAE wrong kind"); }
        try { InvokeInstruction.of(Opcode.IADD, mr); check(false, "of(IADD,mr) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD,mr) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
