/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableTypeTableAttribute conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTypeTableAttributeTest
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

public class LocalVariableTypeTableAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableTypeTableAttribute");
        check(c != null, "java.lang.classfile.attribute.LocalVariableTypeTableAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableTypeTableAttribute"), "binary name java.lang.classfile.attribute.LocalVariableTypeTableAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableTypeTableAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableTypeTableAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LocalVariableTypeTableAttribute has no superclass");
        check(declares(c, "localVariableTypes"), "declares localVariableTypes");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariableType(0, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e).labelBinding(s).labelBinding(e).return_();
        }));
        LocalVariableTypeTableAttribute a = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).get();
        check(a.localVariableTypes().size() == 1, "size");
        check(a.localVariableTypes().get(0).name().stringValue().equals("x"), "name");

    }

    // ---- Part 3: 工厂（unbound，用已解析条目）+ null/u2 负例 + 元数据 ----
    static void testFactoryMatrix() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVTTA1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        LocalVariableTypeTableAttribute parsed = cf.parse(b).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTypeTable()).get();
        check(parsed.localVariableTypes().size() == 1, "parsed 1 entry");
        LocalVariableTypeTableAttribute rebuilt = LocalVariableTypeTableAttribute.of(parsed.localVariableTypes());
        check(rebuilt.localVariableTypes().size() == 1, "of(List) size");
        check(rebuilt.localVariableTypes().get(0) == parsed.localVariableTypes().get(0), "of(List) keeps instances");
        LocalVariableTypeInfo t = rebuilt.localVariableTypes().get(0);
        check(t.slot() == 1 && t.name().stringValue().equals("x"), "rebuilt slot/name");
        check(t.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "rebuilt signature");
        check(t.startPc() == 0 && t.length() == 3, "rebuilt pc/len covers body");
        // null 负例
        try { LocalVariableTypeTableAttribute.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        // u2 尺寸边界
        check(LocalVariableTypeTableAttribute.of(Collections.nCopies(65535, t)).localVariableTypes().size() == 65535, "65535 ok");
        try {
            LocalVariableTypeTableAttribute.of(Collections.nCopies(66000, t));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "66000 IAE msg");
        }
        // 元数据
        check(rebuilt.attributeName().stringValue().equals(Attributes.NAME_LOCAL_VARIABLE_TYPE_TABLE), "attributeName==NAME_LOCAL_VARIABLE_TYPE_TABLE");
        check(rebuilt.attributeName().stringValue().equals("LocalVariableTypeTable"), "attributeName literal");
        check(rebuilt.attributeMapper() == Attributes.localVariableTypeTable(), "attributeMapper==Attributes.localVariableTypeTable()");
        check(rebuilt.attributeMapper().stability() == AttributeMapper.AttributeStability.LABELS, "stability LABELS");
        check(rebuilt.attributeMapper().allowMultiple(), "LVTT allowMultiple true");
    }

    // ---- Part 4: 发射 → re-parse 回读 + 选项交叉 ----
    static void testEmissionAndOptions() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVTTA2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "a", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e);
                    c.localVariableType(2, "b", Signature.parseFrom("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var lvtt = code.findAttribute(Attributes.localVariableTypeTable());
        check(lvtt.isPresent(), "LVTT present");
        List<LocalVariableTypeInfo> ts = lvtt.get().localVariableTypes();
        check(ts.size() == 2, "2 entries");
        check(ts.get(0).name().stringValue().equals("a") && ts.get(0).slot() == 1, "entry a");
        check(ts.get(1).name().stringValue().equals("b") && ts.get(1).slot() == 2, "entry b");
        check(ts.stream().allMatch(x -> x.signature().stringValue().startsWith("Ljava/util/")), "generic signatures");
        check(ts.stream().allMatch(x -> x.startPc() == 0 && x.length() == 3), "both cover whole body");
        // parse DROP_DEBUG 保留属性
        CodeModel dropped = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b).methods().get(0).code().get();
        check(dropped.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "attribute kept under DROP_DEBUG");
        check(dropped.findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().size() == 2, "entries intact");
        // build DROP_DEBUG 不生成
        byte[] b2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).build(ClassDesc.of("LVTTA3"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        check(cf.parse(b2).methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).isEmpty(), "build DROP_DEBUG omits LVTT");
    }

    // ---- Part 5: javac 默认不产 LVTT ----
    static void testRealClass() throws Exception {
        byte[] self = LocalVariableTypeTableAttributeTest.class.getResourceAsStream("LocalVariableTypeTableAttributeTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.code().isPresent()) any |= mm.code().get().findAttribute(Attributes.localVariableTypeTable()).isPresent();
        check(!any, "javac without -g emits no LocalVariableTypeTable");
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
