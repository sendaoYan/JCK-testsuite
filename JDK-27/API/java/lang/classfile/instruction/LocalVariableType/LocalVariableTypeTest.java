/*
 * @test
 * @summary java.lang.classfile.instruction.LocalVariableType conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTypeTest
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

public class LocalVariableTypeTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLVY" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LocalVariableType");
        check(c != null, "java.lang.classfile.instruction.LocalVariableType loads");
        check(c.getName().equals("java.lang.classfile.instruction.LocalVariableType"), "binary name java.lang.classfile.instruction.LocalVariableType");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LocalVariableType is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LocalVariableType is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LocalVariableType has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "name"), "declares name");
        check(declares(c, "signature"), "declares signature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariableType(0, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e).labelBinding(s).labelBinding(e).return_();
        }));
        CodeModel code = ClassFile.of().parse(b).methods().get(0).code().get();
        boolean found = false;
        for (CodeElement ce : code) { if (ce instanceof LocalVariableType lvt && lvt.slot() == 0 && lvt.name().stringValue().equals("x")) found = true; }
        check(found, "LocalVariableType present");

    }


    // ---- Part 3: 发射-回读（核心）：泛型签名伪指令 + 属性 ----
    static void testEmitReadback() throws Exception {
        Signature sig = Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;");
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLVY"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).nop().labelBinding(e).return_();
            cob.localVariableType(1, "list", sig, s, e);
            cob.localVariableType(2, "map", Signature.parseFrom("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"), s, e);
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        java.util.List<LocalVariableType> lvs = new ArrayList<>();
        for (CodeElement el : ca.elementList()) if (el instanceof LocalVariableType x) lvs.add(x);
        check(lvs.size() == 2, "2 LocalVariableType pseudos, got " + lvs.size());
        check(lvs.get(0).slot() == 1 && lvs.get(0).name().stringValue().equals("list"), "lvt0 slot/name");
        check(lvs.get(0).signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "lvt0 signature utf8");
        check(lvs.get(0).signatureSymbol() instanceof Signature.ClassTypeSig, "lvt0 signatureSymbol ClassTypeSig");
        check(lvs.get(0).signatureSymbol().toString().contains("List"), "lvt0 signatureSymbol List");
        check(lvs.get(1).signature().stringValue().equals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"), "lvt1 signature utf8");
        for (LocalVariableType lv : lvs) {
            check(lv.startScope() != null && lv.endScope() != null, lv.name().stringValue() + " scopes non-null");
            check(lv instanceof PseudoInstruction, lv.name().stringValue() + " PseudoInstruction");
            check(!((Object) lv instanceof Instruction), lv.name().stringValue() + " not Instruction");
        }
        // 与 LocalVariable（非泛型 LVT）互斥：不同伪指令类型
        int plainLv = 0;
        for (CodeElement el : ca.elementList()) if (el instanceof LocalVariable) plainLv++;
        check(plainLv == 0, "localVariableType does not produce LocalVariable pseudo");
        // 属性面
        check(ca.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "LVTT attribute present");
        check(ca.findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().size() == 2, "LVTT attr 2 entries");
        check(ca.findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().get(0).signature().stringValue()
                .equals("Ljava/util/List<Ljava/lang/String;>;"), "LVTT attr entry0 signature");
        // Utf8Entry 重载
        java.util.List<CodeElement> els = emit("utf", ConstantDescs.MTD_void, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).return_();
            cob.labelBinding(e);
            cob.localVariableType(1, cob.constantPool().utf8Entry("u"), cob.constantPool().utf8Entry("Ljava/util/List<Ljava/lang/String;>;"), s, e);
        });
        int n = 0;
        for (CodeElement el : els) if (el instanceof LocalVariableType x && x.name().stringValue().equals("u")) n++;
        check(n == 1, "Utf8Entry overload emits LocalVariableType u");
        // DROP_DEBUG 剥伪指令保属性
        ClassModel dropped = ClassFile.of().withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b);
        int after = 0;
        for (CodeElement el : dropped.methods().get(0).code().get().elementList()) if (el instanceof LocalVariableType) after++;
        check(after == 0, "DROP_DEBUG strips LocalVariableType pseudo");
        check(dropped.methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).isPresent(), "option keeps LVTT attr");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] ls = new Label[2];
        ClassFile.of().build(ClassDesc.of("EmLVY2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            ls[0] = cob.newLabel();
            ls[1] = cob.newLabel();
            LocalVariableType lv = LocalVariableType.of(3, "g", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), ls[0], ls[1]);
            check(lv.slot() == 3 && lv.name().stringValue().equals("g"), "of(slot,name,Signature,..) accessors");
            check(lv.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "of signature utf8");
            LocalVariableType lv2 = LocalVariableType.of(3, cob.constantPool().utf8Entry("g2"), cob.constantPool().utf8Entry("Ljava/lang/Object;"), ls[0], ls[1]);
            check(lv2.name().stringValue().equals("g2"), "of Utf8 overloads");
            cob.return_();
        }));
        try { LocalVariableType.of(1, "y", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), null, null); check(false, "of null labels must throw"); }
        catch (NullPointerException e) { check(true, "of(...,null,null) NPE"); }
        try { LocalVariableType.of(-1, "y", Signature.parseFrom("Ljava/lang/Object;"), ls[0], ls[1]); check(false, "of(-1,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(-1,...) IAE negative slot"); }
        try { LocalVariableType.of(1, (String) null, Signature.parseFrom("Ljava/lang/Object;"), ls[0], ls[1]); check(false, "of(1,null,..) must throw"); }
        catch (NullPointerException e) { check(true, "of(1,null name,..) NPE"); }
        try { LocalVariableType.of(1, "y", (Signature) null, ls[0], ls[1]); check(false, "of(1,y,null sig,..) must throw"); }
        catch (NullPointerException e) { check(true, "of(1,y,null sig,..) NPE"); }
        // 基本类型签名不触发工厂校验（写入 classfile 时才可能失败）——记录实测行为
        LocalVariableType base = LocalVariableType.of(1, "y", Signature.parseFrom("I"), ls[0], ls[1]);
        check(base.signature().stringValue().equals("I"), "base-type signature accepted by factory, stringValue I");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
