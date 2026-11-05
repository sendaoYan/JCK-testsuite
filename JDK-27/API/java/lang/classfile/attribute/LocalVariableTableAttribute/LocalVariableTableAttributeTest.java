/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableTableAttribute conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTableAttributeTest
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

public class LocalVariableTableAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableTableAttribute");
        check(c != null, "java.lang.classfile.attribute.LocalVariableTableAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableTableAttribute"), "binary name java.lang.classfile.attribute.LocalVariableTableAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableTableAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableTableAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LocalVariableTableAttribute has no superclass");
        check(declares(c, "localVariables"), "declares localVariables");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariable(0, "x", ConstantDescs.CD_int, s, e).labelBinding(s).labelBinding(e).return_();
        }));
        LocalVariableTableAttribute a = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).get();
        check(a.localVariables().size() == 1, "size");
        check(a.localVariables().get(0).name().stringValue().equals("x"), "name");
        check(a.localVariables().get(0).slot() == 0, "slot");
        check(a.localVariables().get(0).typeSymbol().equals(ConstantDescs.CD_int), "typeSymbol");

    }

    // ---- Part 3: 工厂（unbound，用已解析条目）+ null/u2 负例 + 元数据 ----
    static void testFactoryMatrix() {
        // LocalVariableInfo 无公开工厂：先经发射取得真实条目，再构造新属性
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVTT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "lv", ConstantDescs.CD_int, s, e);
                    c.nop();
                    c.return_();
                }));
        LocalVariableTableAttribute parsed = cf.parse(b).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTable()).get();
        check(parsed.localVariables().size() == 1, "parsed 1 entry");
        LocalVariableTableAttribute rebuilt = LocalVariableTableAttribute.of(parsed.localVariables());
        check(rebuilt.localVariables().size() == 1, "of(List) size");
        check(rebuilt.localVariables().get(0) == parsed.localVariables().get(0), "of(List) keeps instances");
        LocalVariableInfo v = rebuilt.localVariables().get(0);
        check(v.slot() == 1 && v.name().stringValue().equals("lv") && v.type().stringValue().equals("I"), "rebuilt accessors");
        check(v.startPc() == 0 && v.length() == 2, "rebuilt pc/len (2-byte body)");
        check(v.typeSymbol().descriptorString().equals("I"), "rebuilt typeSymbol");
        // null 负例
        try { LocalVariableTableAttribute.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        // u2 尺寸边界（共享条目 nCopies）
        check(LocalVariableTableAttribute.of(Collections.nCopies(65535, v)).localVariables().size() == 65535, "65535 ok");
        try {
            LocalVariableTableAttribute.of(Collections.nCopies(66000, v));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "66000 IAE msg");
        }
        // 元数据：引用 code 位置 -> LABELS，可多条
        check(rebuilt.attributeName().stringValue().equals(Attributes.NAME_LOCAL_VARIABLE_TABLE), "attributeName==NAME_LOCAL_VARIABLE_TABLE");
        check(rebuilt.attributeName().stringValue().equals("LocalVariableTable"), "attributeName literal");
        check(rebuilt.attributeMapper() == Attributes.localVariableTable(), "attributeMapper==Attributes.localVariableTable()");
        check(rebuilt.attributeMapper().stability() == AttributeMapper.AttributeStability.LABELS, "stability LABELS");
        check(rebuilt.attributeMapper().allowMultiple(), "LVT allowMultiple true");
    }

    // ---- Part 4: 发射 → re-parse 回读 + 选项交叉 ----
    static void testEmissionAndOptions() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVTT2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)I"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "arg", ClassDesc.of("java.lang.String"), s, e);
                    c.aload(1);
                    c.invokevirtual(ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.ofDescriptor("()I"));
                    c.ireturn();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var lvt = code.findAttribute(Attributes.localVariableTable());
        check(lvt.isPresent(), "LVT present");
        check(lvt.get().localVariables().size() == 1, "1 var");
        LocalVariableInfo v = lvt.get().localVariables().get(0);
        check(v.name().stringValue().equals("arg"), "name arg");
        check(v.type().stringValue().equals("Ljava/lang/String;"), "ref type descriptor");
        check(v.slot() == 1 && v.startPc() == 0 && v.length() == v.length(), "slot/pc");
        check(v.length() == ((CodeAttribute) code).codeLength(), "length covers whole body");
        // parse 侧 DROP_DEBUG：伪指令剥离、属性保留
        CodeModel dropped = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b).methods().get(0).code().get();
        int pseudos = 0;
        for (CodeElement el : dropped.elementList()) if (el instanceof java.lang.classfile.instruction.LocalVariable) pseudos++;
        check(pseudos == 0, "DROP_DEBUG strips pseudos");
        check(dropped.findAttribute(Attributes.localVariableTable()).isPresent(), "attribute kept under DROP_DEBUG");
        check(dropped.findAttribute(Attributes.localVariableTable()).get().localVariables().size() == 1, "entries intact");
        // build 侧 DROP_DEBUG：属性不生成
        byte[] b2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).build(ClassDesc.of("LVTT3"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "lv", ConstantDescs.CD_int, s, e).return_();
                }));
        check(cf.parse(b2).methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).isEmpty(), "build DROP_DEBUG omits LVT");
        // 无发射则无 LVT
        byte[] b3 = cf.build(ClassDesc.of("LVTT4"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> c.return_()));
        check(cf.parse(b3).methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).isEmpty(), "no emission -> no LVT");
    }

    // ---- Part 5: javac 默认（无 -g）不产 LVT ----
    static void testRealClass() throws Exception {
        byte[] self = LocalVariableTableAttributeTest.class.getResourceAsStream("LocalVariableTableAttributeTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.code().isPresent()) any |= mm.code().get().findAttribute(Attributes.localVariableTable()).isPresent();
        check(!any, "javac without -g emits no LocalVariableTable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testEmissionAndOptions();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
