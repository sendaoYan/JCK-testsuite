/*
 * @test
 * @summary java.lang.classfile.instruction.TypeCheckInstruction conformance: white-box structure + black-box behavior.
 * @run main TypeCheckInstructionTest
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

public class TypeCheckInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmTCI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.TypeCheckInstruction");
        check(c != null, "java.lang.classfile.instruction.TypeCheckInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.TypeCheckInstruction"), "binary name java.lang.classfile.instruction.TypeCheckInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.TypeCheckInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.TypeCheckInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.TypeCheckInstruction has no superclass");
        check(declares(c, "type"), "declares type");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        TypeCheckInstruction i = TypeCheckInstruction.of(Opcode.CHECKCAST, ce);
        check(i.opcode() == Opcode.CHECKCAST, "opcode");
        check(i.type() == ce, "type");
        check(i.type().asInternalName().equals("java/lang/String"), "internal");
        check(TypeCheckInstruction.of(Opcode.INSTANCEOF, ClassDesc.of("java.lang.Number")).type().asSymbol().equals(ClassDesc.of("java.lang.Number")), "INSTANCEOF");

    }


    // ---- Part 3: 发射-回读（核心）：checkcast + instanceof ----
    static void testEmitReadback() throws Exception {
        // checkcast：Object→String
        java.util.List<CodeElement> els = emit("cc", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Ljava/lang/String;"),
                cob -> cob.aload(0).checkcast(ClassDesc.of("java.lang.String")).areturn());
        TypeCheckInstruction cc = null;
        for (CodeElement e : els) if (e instanceof TypeCheckInstruction x && x.opcode() == Opcode.CHECKCAST) cc = x;
        check(cc != null, "checkcast emitted + read back");
        check(cc.opcode() == Opcode.CHECKCAST, "CHECKCAST opcode");
        check(cc.sizeInBytes() == 3, "CHECKCAST sizeInBytes 3");
        check(cc.opcode().kind() == Opcode.Kind.TYPE_CHECK, "kind TYPE_CHECK");
        check(cc.type().asInternalName().equals("java/lang/String"), "type internal");
        check(cc.type().asSymbol().descriptorString().equals("Ljava/lang/String;"), "type symbol");
        check(cc instanceof Instruction && cc instanceof CodeElement, "Instruction+CodeElement");
        // instanceof（CodeBuilder 方法实名 instanceOf，大写 O）
        els = emit("io", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Z"),
                cob -> cob.aload(0).instanceOf(ClassDesc.of("java.lang.String")).ireturn());
        TypeCheckInstruction io = null;
        for (CodeElement e : els) if (e instanceof TypeCheckInstruction x && x.opcode() == Opcode.INSTANCEOF) io = x;
        check(io != null, "instanceOf emitted + read back");
        check(io.sizeInBytes() == 3, "INSTANCEOF sizeInBytes 3");
        check(io.type().asInternalName().equals("java/lang/String"), "INSTANCEOF type");
        // javac 真产物：castHelper 的 (String) 强转 + instanceof Helper
        byte[] self = TypeCheckInstructionTest.class.getResourceAsStream("TypeCheckInstructionTest.class").readAllBytes();
        boolean foundCast = false, foundInstance = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.code().isPresent()) continue;
            boolean isCast = mm.methodName().stringValue().equals("castHelper");
            boolean isInst = mm.methodName().stringValue().equals("instanceofHelper");
            if (!isCast && !isInst) continue;
            for (CodeElement e : mm.code().get().elementList()) if (e instanceof TypeCheckInstruction x) {
                if (isCast && x.opcode() == Opcode.CHECKCAST && x.type().asInternalName().equals("java/lang/String")) foundCast = true;
                if (isInst && x.opcode() == Opcode.INSTANCEOF && x.type().asInternalName().equals("java/lang/String")) foundInstance = true;
            }
        }
        check(foundCast, "javac (String) cast emits CHECKCAST String");
        check(foundInstance, "javac instanceof emits INSTANCEOF String");
        // CodeBuilder 方法实名 instanceOf
        boolean hasInstanceOf = false, hasInstanceof = false;
        for (Method m : CodeBuilder.class.getMethods()) {
            if (m.getName().equals("instanceOf")) hasInstanceOf = true;
            if (m.getName().equals("instanceof_")) hasInstanceof = true;
        }
        check(hasInstanceOf && !hasInstanceof, "CodeBuilder method named instanceOf (not instanceof_)");
    }

    // ---- Part 4: 工厂 2 重载 + 负例 ----
    static void testFactoryNegative() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check(TypeCheckInstruction.of(Opcode.CHECKCAST, ce).type() == ce, "of(op,ClassEntry) identity");
        check(TypeCheckInstruction.of(Opcode.INSTANCEOF, ce).opcode() == Opcode.INSTANCEOF, "of(INSTANCEOF,ce)");
        check(TypeCheckInstruction.of(Opcode.INSTANCEOF, ClassDesc.of("java.lang.Number")).type().asInternalName().equals("java/lang/Number"),
                "of(op,ClassDesc) internal name");
        check(TypeCheckInstruction.of(Opcode.CHECKCAST, ClassDesc.ofDescriptor("[I")).type().asInternalName().equals("[I"),
                "of array class name");
        try { TypeCheckInstruction.of(null, ce); check(false, "of(null,ce) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,ce) NPE"); }
        try { TypeCheckInstruction.of(Opcode.CHECKCAST, (ClassDesc) null); check(false, "of(CHECKCAST,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(CHECKCAST,(ClassDesc)null) NPE"); }
        try { TypeCheckInstruction.of(Opcode.CHECKCAST, (ClassEntry) null); check(false, "of(CHECKCAST,(ClassEntry)null) must throw"); }
        catch (NullPointerException e) { check(true, "of(CHECKCAST,(ClassEntry)null) NPE"); }
        try { TypeCheckInstruction.of(Opcode.NOP, ClassDesc.of("java.lang.String")); check(false, "of(NOP,..) must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("TYPE_CHECK"), "of(NOP,..) IAE mentions TYPE_CHECK: " + e.getMessage());
        }
        try { TypeCheckInstruction.of(Opcode.IADD, ClassDesc.of("java.lang.String")); check(false, "of(IADD,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD,..) IAE wrong kind"); }
    }

    static String castHelper(Object o) { return (String) o; }
    static boolean instanceofHelper(Object o) { return o instanceof String; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
