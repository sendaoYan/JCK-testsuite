/*
 * @test
 * @summary java.lang.classfile.instruction.LocalVariable conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTest
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

public class LocalVariableTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLVT" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LocalVariable");
        check(c != null, "java.lang.classfile.instruction.LocalVariable loads");
        check(c.getName().equals("java.lang.classfile.instruction.LocalVariable"), "binary name java.lang.classfile.instruction.LocalVariable");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LocalVariable is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LocalVariable is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LocalVariable has no superclass");
        check(declares(c, "slot"), "declares slot");
        check(declares(c, "name"), "declares name");
        check(declares(c, "type"), "declares type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariable(0, "x", ConstantDescs.CD_int, s, e).labelBinding(s).labelBinding(e).return_();
        }));
        CodeModel code = ClassFile.of().parse(b).methods().get(0).code().get();
        boolean found = false;
        for (CodeElement ce : code) { if (ce instanceof LocalVariable lv && lv.slot() == 0 && lv.name().stringValue().equals("x")) found = true; }
        check(found, "LocalVariable present");

    }


    // ---- Part 3: 发射-回读（核心）：LVT 伪指令 + 属性双面 ----
    static void testEmitReadback() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLVT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(IJ)V"), ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).iload(0).pop().labelBinding(e).return_();
            cob.localVariable(0, "x", ConstantDescs.CD_int, s, e);
            cob.localVariable(1, "y", ClassDesc.ofDescriptor("J"), s, e);
            cob.localVariable(2, "obj", ConstantDescs.CD_String, s, e);
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        java.util.List<LocalVariable> lvs = new ArrayList<>();
        for (CodeElement el : ca.elementList()) if (el instanceof LocalVariable x) lvs.add(x);
        check(lvs.size() == 3, "3 LocalVariable pseudos, got " + lvs.size());
        check(lvs.get(0).slot() == 0 && lvs.get(0).name().stringValue().equals("x"), "lv0 slot/name");
        check(lvs.get(0).type().stringValue().equals("I"), "lv0 type utf8 I");
        check(lvs.get(0).typeSymbol().descriptorString().equals("I"), "lv0 typeSymbol I");
        check(lvs.get(1).slot() == 1 && lvs.get(1).name().stringValue().equals("y"), "lv1 slot/name");
        check(lvs.get(1).type().stringValue().equals("J"), "lv1 type utf8 J");
        check(lvs.get(2).typeSymbol().descriptorString().equals("Ljava/lang/String;"), "lv2 typeSymbol String");
        for (LocalVariable lv : lvs) {
            check(lv.startScope() != null && lv.endScope() != null, lv.name().stringValue() + " scopes non-null");
            check(ca.labelToBci(lv.startScope()) == 0, lv.name().stringValue() + " startScope bci 0");
            check(ca.labelToBci(lv.endScope()) == 2, lv.name().stringValue() + " endScope bci 2");
            check(lv instanceof PseudoInstruction, lv.name().stringValue() + " PseudoInstruction");
            check(!((Object) lv instanceof Instruction), lv.name().stringValue() + " not Instruction");
        }
        // LVT 属性面（Utf8Entry 重载）
        check(ca.findAttribute(Attributes.localVariableTable()).isPresent(), "LVT attribute present");
        var lnt = ca.findAttribute(Attributes.localVariableTable()).get().localVariables();
        check(lnt.size() == 3, "LVT attr 3 entries");
        check(lnt.get(0).slot() == 0 && lnt.get(0).name().stringValue().equals("x") && lnt.get(0).type().stringValue().equals("I"),
                "LVT attr entry0 triple");
        check(lnt.get(0).startPc() == 0, "LVT attr startPc 0");
        check(lnt.get(0).length() == 2, "LVT attr length 2");
        // Utf8Entry 重载发射
        java.util.List<CodeElement> els = emit("utf", MethodTypeDesc.ofDescriptor("(I)V"), cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).return_();
            cob.labelBinding(e);
            cob.localVariable(0, cob.constantPool().utf8Entry("u"), cob.constantPool().utf8Entry("I"), s, e);
        });
        int n = 0;
        for (CodeElement el : els) if (el instanceof LocalVariable x && x.name().stringValue().equals("u")) n++;
        check(n == 1, "Utf8Entry overload emits LocalVariable u");
        // DROP_DEBUG 剥伪指令保属性
        ClassModel dropped = ClassFile.of().withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b);
        int after = 0;
        for (CodeElement el : dropped.methods().get(0).code().get().elementList()) if (el instanceof LocalVariable) after++;
        check(after == 0, "DROP_DEBUG strips LocalVariable pseudo");
        check(dropped.methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).isPresent(), "option keeps LVT attr");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] ls = new Label[2];
        ClassFile.of().build(ClassDesc.of("EmLVT2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            ls[0] = cob.newLabel();
            ls[1] = cob.newLabel();
            LocalVariable lv = LocalVariable.of(3, "n", ConstantDescs.CD_Object, ls[0], ls[1]);
            check(lv.slot() == 3 && lv.name().stringValue().equals("n"), "of(slot,name,ClassDesc,..) accessors");
            check(lv.type().stringValue().equals("Ljava/lang/Object;"), "of type utf8");
            check(lv.typeSymbol().descriptorString().equals("Ljava/lang/Object;"), "of typeSymbol");
            LocalVariable lv2 = LocalVariable.of(3, cob.constantPool().utf8Entry("n2"), cob.constantPool().utf8Entry("I"), ls[0], ls[1]);
            check(lv2.name().stringValue().equals("n2") && lv2.type().stringValue().equals("I"), "of Utf8 overloads");
            cob.return_();
        }));
        try { LocalVariable.of(1, "x", ConstantDescs.CD_int, null, null); check(false, "of null labels must throw"); }
        catch (NullPointerException e) { check(true, "of(...,null,null) NPE"); }
        try { LocalVariable.of(-1, "x", ConstantDescs.CD_int, ls[0], ls[1]); check(false, "of(-1,..) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(-1,...) IAE negative slot"); }
        try { LocalVariable.of(1, (String) null, ConstantDescs.CD_int, ls[0], ls[1]); check(false, "of(1,null,..) must throw"); }
        catch (NullPointerException e) { check(true, "of(1,null name,..) NPE"); }
        try { LocalVariable.of(1, "x", (ClassDesc) null, ls[0], ls[1]); check(false, "of(1,x,null type,..) must throw"); }
        catch (NullPointerException e) { check(true, "of(1,x,null type,..) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
