/*
 * @test
 * @summary java.lang.classfile.AttributedElement conformance: white-box structure + black-box behavior.
 * @run main AttributedElementTest
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

public class AttributedElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AttributedElement");
        check(c != null, "java.lang.classfile.AttributedElement loads");
        check(c.getName().equals("java.lang.classfile.AttributedElement"), "binary name java.lang.classfile.AttributedElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AttributedElement is public");
        check(c.isInterface(), "java.lang.classfile.AttributedElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AttributedElement has no superclass");
        check(declares(c, "attributes"), "declares attributes");
        check(declares(c, "findAttribute"), "declares findAttribute");
        check(declares(c, "findAttributes"), "declares findAttributes");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(SourceFileAttribute.of("T.java")).with(DeprecatedAttribute.of()));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.findAttribute(Attributes.sourceFile()).isPresent(), "findAttribute sourceFile");
        check(cm.findAttribute(Attributes.sourceFile()).get().sourceFile().stringValue().equals("T.java"), "sourceFile value");
        check(cm.findAttribute(Attributes.deprecated()).isPresent(), "findAttribute deprecated");
        check(cm.findAttribute(Attributes.signature()).isEmpty(), "no signature attr");

    }

    // ---- Part 3: findAttribute(不存在)→empty / findAttributes 缺失→空表 / findAttribute(null)→NPE ----
    static void testFindNegative() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AET"), cb -> cb
                .with(SourceFileAttribute.of("AET.java"))
                .with(DeprecatedAttribute.of())
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(9))));
        ClassModel cm = cf.parse(b);
        // 不存在的 mapper → empty
        check(cm.findAttribute(Attributes.signature()).isEmpty(), "findAttribute(missing) empty");
        check(cm.fields().get(0).findAttribute(Attributes.deprecated()).isEmpty(), "field findAttribute(missing) empty");
        // findAttributes 缺失 → 空列表（非 null）
        List<SignatureAttribute> none = cm.findAttributes(Attributes.signature());
        check(none != null && none.isEmpty(), "findAttributes(missing) empty list");
        // findAttributes 命中 → 全部同类（两个 Deprecated 实测可共存）
        byte[] b2 = cf.build(ClassDesc.of("AET2"), cb -> cb.with(DeprecatedAttribute.of()).with(DeprecatedAttribute.of()));
        List<DeprecatedAttribute> two = cf.parse(b2).findAttributes(Attributes.deprecated());
        check(two.size() == 2, "findAttributes returns all matches: " + two.size());
        // findAttribute(null) → NPE（实测）
        try {
            cm.findAttribute(null);
            check(false, "findAttribute(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "findAttribute(null) NPE");
        }
    }

    // ---- Part 4: attributes() 只读快照语义（add → UnsupportedOperationException）----
    static void testAttributesReadOnly() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AER"), cb -> cb
                .with(SourceFileAttribute.of("AER.java"))
                .with(DeprecatedAttribute.of()));
        ClassModel cm = cf.parse(b);
        List<Attribute<?>> attrs = cm.attributes();
        check(attrs.size() == 2, "attributes list size 2");
        try {
            attrs.add(DeprecatedAttribute.of());
            check(false, "attributes() add must throw UOE");
        } catch (UnsupportedOperationException expected) {
            check(true, "attributes() add UOE (read-only snapshot)");
        }
        try {
            attrs.remove(0);
            check(false, "attributes() remove must throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "attributes() remove UOE");
        }
        // 原模型不受影响
        check(cm.attributes().size() == 2, "attributes unchanged after rejected mutation");
        // MethodModel/FieldModel 的 attributes() 同为只读
        byte[] b2 = cf.build(ClassDesc.of("AER2"), cb -> cb
                .withMethod("m", ConstantDescs.MTD_void, 0, mb -> mb.withCode(cob -> cob.return_()).with(DeprecatedAttribute.of()))
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(DeprecatedAttribute.of())));
        ClassModel cm2 = cf.parse(b2);
        try {
            cm2.methods().get(0).attributes().add(DeprecatedAttribute.of());
            check(false, "method attributes() add must throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "method attributes() UOE");
        }
        try {
            cm2.fields().get(0).attributes().clear();
            check(false, "field attributes() clear must throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "field attributes() UOE");
        }
    }

    // ---- Part 5: Model 与 Code 都实现 AttributedElement（instanceof 断言）+ 混合遍历 ----
    static void testImplementors() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AEI"), cb -> cb
                .with(SourceFileAttribute.of("AEI.java"))
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(1)))
                .withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_STATIC, cob -> {
                    Label l = cob.newLabel();
                    cob.iconst_0().ifne(l);
                    cob.iconst_1().ireturn();
                    cob.labelBinding(l).iconst_2().ireturn();
                }));
        ClassModel cm = cf.parse(b);
        check(cm instanceof AttributedElement, "ClassModel is AttributedElement");
        check(cm.fields().get(0) instanceof AttributedElement, "FieldModel is AttributedElement");
        check(cm.methods().get(0) instanceof AttributedElement, "MethodModel is AttributedElement");
        CodeModel code = cm.methods().get(0).code().get();
        check(code instanceof AttributedElement, "CodeModel is AttributedElement");
        // CodeModel 属性：分支体 → StackMapTable 可查
        check(code.findAttribute(Attributes.stackMapTable()).isPresent(), "code findAttribute StackMapTable");
        check(!code.attributes().isEmpty(), "code attributes non-empty");
        // 多形态统一遍历：4 类被饰元素的属性名字均可读
        Set<String> allNames = new HashSet<>();
        for (AttributedElement ae : List.of(cm, cm.fields().get(0), cm.methods().get(0), code))
            for (Attribute<?> a : ae.attributes()) allNames.add(a.attributeName().stringValue());
        check(allNames.contains("SourceFile"), "traversal saw class attr");
        check(allNames.contains("ConstantValue"), "traversal saw field attr");
        check(allNames.contains("Code"), "traversal saw method attr");
        check(allNames.contains("StackMapTable"), "traversal saw code attr");
        // findAttribute 与 attributes() 自洽：findAttribute 命中者必在 attributes() 列表
        boolean consistent = false;
        for (Attribute<?> a : cm.attributes())
            if (a.attributeName().stringValue().equals("SourceFile")) consistent = true;
        check(consistent && cm.findAttribute(Attributes.sourceFile()).isPresent(), "findAttribute/attributes() consistent");
        // AttributedElement 是 ClassFileElement（层次白盒）
        check(ClassFileElement.class.isAssignableFrom(AttributedElement.class), "AttributedElement is-a ClassFileElement");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFindNegative();
        testAttributesReadOnly();
        testImplementors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
