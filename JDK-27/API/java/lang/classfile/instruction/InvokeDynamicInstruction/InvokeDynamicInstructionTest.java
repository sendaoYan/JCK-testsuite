/*
 * @test
 * @summary java.lang.classfile.instruction.InvokeDynamicInstruction conformance: white-box structure + black-box behavior.
 * @run main InvokeDynamicInstructionTest
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

public class InvokeDynamicInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmIDI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.InvokeDynamicInstruction");
        check(c != null, "java.lang.classfile.instruction.InvokeDynamicInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.InvokeDynamicInstruction"), "binary name java.lang.classfile.instruction.InvokeDynamicInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.InvokeDynamicInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.InvokeDynamicInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.InvokeDynamicInstruction has no superclass");
        check(declares(c, "invokedynamic"), "declares invokedynamic");
        check(declares(c, "name"), "declares name");
        check(declares(c, "type"), "declares type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        InvokeDynamicInstruction i = InvokeDynamicInstruction.of(cpb.invokeDynamicEntry(bsm, nat));
        check(i.opcode() == Opcode.INVOKEDYNAMIC, "opcode");
        check(i.name().stringValue().equals("m"), "name");
        check(i.type().stringValue().equals("()V"), "type");
        check(i.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_void)), "typeSymbol");
        check(i.bootstrapMethod() != null, "bootstrapMethod");

    }


    // ---- Part 3: 发射-回读（核心）：invokedynamic(DynamicCallSiteDesc) ----
    static void testEmitReadback() throws Exception {
        DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("java.lang.invoke.LambdaMetafactory"), "metafactory",
                MethodTypeDesc.ofDescriptor("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
        DynamicCallSiteDesc dcsd = DynamicCallSiteDesc.of(bsm, "x",
                MethodTypeDesc.ofDescriptor("()Ljava/lang/Runnable;"),
                MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"),
                MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("EmIDIImpl"), "run", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;")),
                MethodTypeDesc.ofDescriptor("()Ljava/lang/Runnable;"));
        java.util.List<CodeElement> els = emit("ind", MethodTypeDesc.ofDescriptor("()Ljava/lang/Runnable;"),
                cob -> cob.invokedynamic(dcsd).areturn());
        InvokeDynamicInstruction i = null;
        for (CodeElement e : els) if (e instanceof InvokeDynamicInstruction x) i = x;
        check(i != null, "invokedynamic emitted + read back");
        check(i.opcode() == Opcode.INVOKEDYNAMIC, "INVOKEDYNAMIC opcode");
        check(i.opcode().kind() == Opcode.Kind.INVOKE_DYNAMIC, "kind INVOKE_DYNAMIC");
        check(i.name().stringValue().equals("x"), "name x");
        check(i.type().stringValue().equals("()Ljava/lang/Runnable;"), "type utf8");
        check(i.typeSymbol().descriptorString().equals("()Ljava/lang/Runnable;"), "typeSymbol");
        check(i.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/invoke/LambdaMetafactory;"), "bsm owner");
        check(i.bootstrapMethod().methodName().equals("metafactory"), "bsm methodName");
        check(i.bootstrapArgs().size() == 3, "bsm 3 static args");
        check(i.invokedynamic() != null, "invokedynamic entry non-null");
        check(i.invokedynamic().name().stringValue().equals("x"), "entry name matches");
        check(i.invokedynamic().type().stringValue().equals("()Ljava/lang/Runnable;"), "entry type matches");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        check(i.sizeInBytes() == 5, "INVOKEDYNAMIC sizeInBytes 5");
    }

    // ---- Part 4: 真 lambda 解析（javac 产物） + 负例 ----
    static Runnable r = () -> {};
    static java.util.function.IntSupplier s = () -> 7;

    static void testRealLambdaAndNegative() throws Exception {
        byte[] self = InvokeDynamicInstructionTest.class.getResourceAsStream("InvokeDynamicInstructionTest.class").readAllBytes();
        int indy = 0;
        boolean lambdaBsm = false, nameApply = false, args3 = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.code().isPresent()) continue;
            for (CodeElement e : mm.code().get().elementList())
                if (e instanceof InvokeDynamicInstruction x) {
                    indy++;
                    if (x.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/invoke/LambdaMetafactory;")
                            && x.bootstrapMethod().methodName().equals("metafactory")) lambdaBsm = true;
                    if (x.name().stringValue().equals("apply") || x.name().stringValue().equals("getAsInt")
                            || x.name().stringValue().equals("run") || x.name().stringValue().equals("get")) nameApply = true;
                    if (x.bootstrapArgs().size() == 3) args3 = true;
                }
        }
        check(indy >= 2, "javac lambda fields produce >=2 INVOKEDYNAMIC, got " + indy);
        check(lambdaBsm, "LambdaMetafactory::metafactory bsm");
        check(nameApply, "lambda name is functional method name");
        check(args3, "bsm 3 args (samMethodType/implMethod/instantiatedMethodType)");
        try { InvokeDynamicInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testRealLambdaAndNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
