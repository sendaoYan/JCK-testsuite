/*
 * @test
 * @summary java.lang.classfile.instruction.FieldInstruction conformance: white-box structure + black-box behavior.
 * @run main FieldInstructionTest
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

public class FieldInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmFLI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.FieldInstruction");
        check(c != null, "java.lang.classfile.instruction.FieldInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.FieldInstruction"), "binary name java.lang.classfile.instruction.FieldInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.FieldInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.FieldInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.FieldInstruction has no superclass");
        check(declares(c, "field"), "declares field");
        check(declares(c, "owner"), "declares owner");
        check(declares(c, "name"), "declares name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FieldRefEntry fre = cpb.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        FieldInstruction i = FieldInstruction.of(Opcode.GETSTATIC, fre);
        check(i.opcode() == Opcode.GETSTATIC, "opcode");
        check(i.field() == fre, "field");
        check(i.owner().asInternalName().equals("java/lang/System"), "owner");
        check(i.name().stringValue().equals("out"), "name");
        check(i.typeSymbol().equals(ClassDesc.of("java.io.PrintStream")), "typeSymbol");

    }


    // ---- Part 3: 发射-回读（核心）：四 opcode + 自建类字段双通道 ----
    static void testEmitReadback() throws Exception {
        // getstatic java.lang.System.out
        java.util.List<CodeElement> els = emit("gs", MethodTypeDesc.ofDescriptor("()Ljava/io/PrintStream;"),
                cob -> cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).areturn());
        FieldInstruction i = null;
        for (CodeElement e : els) if (e instanceof FieldInstruction x) i = x;
        check(i != null, "getstatic emitted + read back");
        check(i.opcode() == Opcode.GETSTATIC, "GETSTATIC opcode");
        check(i.sizeInBytes() == 3, "GETSTATIC sizeInBytes 3");
        check(i.opcode().kind() == Opcode.Kind.FIELD_ACCESS, "kind FIELD_ACCESS");
        check(i.owner().asInternalName().equals("java/lang/System"), "owner");
        check(i.name().stringValue().equals("out"), "name");
        check(i.type().stringValue().equals("Ljava/io/PrintStream;"), "type utf8");
        check(i.typeSymbol().descriptorString().equals("Ljava/io/PrintStream;"), "typeSymbol");
        check(i.field().owner().asInternalName().equals("java/lang/System"), "field().owner");
        check(i.field().name().stringValue().equals("out"), "field().name");
        check(i.field().type().stringValue().equals("Ljava/io/PrintStream;"), "field().type");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        // 自建类：getfield/putfield（实例方法 aload(0)）+ putstatic
        byte[] b = ClassFile.of().build(ClassDesc.of("EmFLIF"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .withField("f", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC)
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC,
                        cob -> cob.aload(0).getfield(ClassDesc.of("EmFLIF"), "f", ConstantDescs.CD_int).ireturn())
                .withMethodBody("set", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_PUBLIC,
                        cob -> cob.aload(0).iload(1).putfield(ClassDesc.of("EmFLIF"), "f", ConstantDescs.CD_int).return_())
                .withMethodBody("sv", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iload(0).putstatic(ClassDesc.of("EmFLIF"), "f", ConstantDescs.CD_int).return_()));
        int get = 0, put = 0, puts = 0;
        for (MethodModel mm : ClassFile.of().parse(b).methods())
            for (CodeElement e : mm.code().get().elementList()) if (e instanceof FieldInstruction x) {
                check(x.owner().asInternalName().equals("EmFLIF"), x.opcode() + " owner self");
                check(x.name().stringValue().equals("f"), x.opcode() + " name f");
                check(x.type().stringValue().equals("I") && x.typeSymbol().descriptorString().equals("I"), x.opcode() + " type I");
                check(x.sizeInBytes() == 3, x.opcode() + " size 3");
                if (x.opcode() == Opcode.GETFIELD) get++;
                if (x.opcode() == Opcode.PUTFIELD) put++;
                if (x.opcode() == Opcode.PUTSTATIC) puts++;
            }
        check(get == 1 && put == 1 && puts == 1, "getfield/putfield/putstatic all read back: " + get + "/" + put + "/" + puts);
        // 通用 fieldAccess(Opcode,...) 通道
        els = emit("fa", MethodTypeDesc.ofDescriptor("()Ljava/io/PrintStream;"),
                cob -> cob.fieldAccess(Opcode.GETSTATIC, ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).areturn());
        boolean foundFa = false;
        for (CodeElement e : els) if (e instanceof FieldInstruction x && x.opcode() == Opcode.GETSTATIC) foundFa = true;
        check(foundFa, "fieldAccess(GETSTATIC,...) emits GETSTATIC");
    }

    // ---- Part 4: 工厂 3 重载 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FieldRefEntry fr = cpb.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        FieldInstruction a = FieldInstruction.of(Opcode.GETSTATIC, fr);
        check(a.opcode() == Opcode.GETSTATIC && a.field() == fr, "of(op,FieldRefEntry) identity");
        check(a.owner().asInternalName().equals("java/lang/System"), "of(op,FieldRefEntry) owner");
        ClassEntry owner = cpb.classEntry(ClassDesc.of("T"));
        FieldInstruction b2 = FieldInstruction.of(Opcode.PUTFIELD, owner, cpb.utf8Entry("f"), cpb.utf8Entry("I"));
        check(b2.opcode() == Opcode.PUTFIELD, "of(op,ClassEntry,Utf8,Utf8) opcode");
        check(b2.owner() == owner && b2.name().stringValue().equals("f") && b2.type().stringValue().equals("I"), "of 4-arg accessors");
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("g", MethodTypeDesc.ofDescriptor("()I"));
        FieldInstruction c = FieldInstruction.of(Opcode.GETFIELD, owner, nat);
        check(c.name().stringValue().equals("g") && c.type().stringValue().equals("()I"), "of(op,ClassEntry,NameAndType) accessors");
        try { FieldInstruction.of(null, fr); check(false, "of(null,fr) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,fr) NPE"); }
        try { FieldInstruction.of(Opcode.GETSTATIC, (FieldRefEntry) null); check(false, "of(GETSTATIC,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(GETSTATIC,null) NPE"); }
        try { FieldInstruction.of(Opcode.NOP, fr); check(false, "of(NOP,fr) must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("FIELD_ACCESS"), "of(NOP,fr) IAE mentions FIELD_ACCESS: " + e.getMessage());
        }
        try { FieldInstruction.of(Opcode.IADD, fr); check(false, "of(IADD,fr) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD,fr) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
