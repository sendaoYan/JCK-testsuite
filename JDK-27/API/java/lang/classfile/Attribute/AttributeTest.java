/*
 * @test
 * @summary java.lang.classfile.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
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

public class AttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Attribute");
        check(c != null, "java.lang.classfile.Attribute loads");
        check(c.getName().equals("java.lang.classfile.Attribute"), "binary name java.lang.classfile.Attribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Attribute is public");
        check(c.isInterface(), "java.lang.classfile.Attribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Attribute has no superclass");
        check(declares(c, "attributeName"), "declares attributeName");
        check(declares(c, "attributeMapper"), "declares attributeMapper");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(SourceFileAttribute.of("T.java")));
        ClassModel cm = ClassFile.of().parse(b);
        java.util.List<Attribute<?>> attrs = cm.attributes();
        check(attrs.size() >= 1, "class has attributes");
        Attribute<?> sf = attrs.stream().filter(a -> a.attributeName().stringValue().equals("SourceFile")).findFirst().get();
        check(sf.attributeName().stringValue().equals("SourceFile"), "attributeName");
        check(sf.attributeMapper() == Attributes.sourceFile(), "attributeMapper == Attributes.sourceFile()");

    }

    // ---- Part 3: attributeName() 规范名全等表（对照 Attributes.NAME_* 常量逐字比对）----
    static void testCanonicalNames() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AT"), cb -> cb
                .with(SourceFileAttribute.of("AT.java"))
                .with(DeprecatedAttribute.of())
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(11)))
                .withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_STATIC, cob -> {
                    Label l = cob.newLabel();
                    cob.iconst_0().ifne(l);   // 分支 → 产生 StackMapTable
                    cob.iconst_1().ireturn();
                    cob.labelBinding(l).iconst_2().ireturn();
                }));
        ClassModel cm = cf.parse(b);
        // 类级属性：SourceFile / Deprecated
        Attribute<?> sf = cm.findAttribute(Attributes.sourceFile()).get();
        Attribute<?> dep = cm.findAttribute(Attributes.deprecated()).get();
        check(sf.attributeName().stringValue().equals("SourceFile"), "SourceFile attributeName literal");
        check(sf.attributeName().stringValue().equals(Attributes.NAME_SOURCE_FILE), "SourceFile == NAME_SOURCE_FILE");
        check(dep.attributeName().stringValue().equals("Deprecated"), "Deprecated attributeName literal");
        check(dep.attributeName().stringValue().equals(Attributes.NAME_DEPRECATED), "Deprecated == NAME_DEPRECATED");
        // 方法级属性：Code（含 StackMapTable 于 CodeModel）
        MethodModel mm = cm.methods().get(0);
        Attribute<?> codeAttr = mm.attributes().stream().filter(a -> a.attributeName().stringValue().equals("Code")).findFirst().get();
        check(codeAttr.attributeName().stringValue().equals(Attributes.NAME_CODE), "Code == NAME_CODE");
        check(codeAttr instanceof CodeAttribute, "method attr is CodeAttribute");
        // 字段级属性：ConstantValue
        Attribute<?> cv = cm.fields().get(0).findAttribute(Attributes.constantValue()).get();
        check(cv.attributeName().stringValue().equals("ConstantValue"), "ConstantValue attributeName literal");
        check(cv.attributeName().stringValue().equals(Attributes.NAME_CONSTANT_VALUE), "ConstantValue == NAME_CONSTANT_VALUE");
        // 代码体属性：StackMapTable
        CodeModel code = mm.code().get();
        Attribute<?> smt = code.findAttribute(Attributes.stackMapTable()).get();
        check(smt.attributeName().stringValue().equals(Attributes.NAME_STACK_MAP_TABLE), "StackMapTable == NAME_STACK_MAP_TABLE");
        check(smt.attributeName().stringValue().equals("StackMapTable"), "StackMapTable attributeName literal");
        // attributeName() 返回 Utf8Entry（池条目），可 equalsString
        check(sf.attributeName() instanceof Utf8Entry, "attributeName is Utf8Entry");
        check(sf.attributeName().equalsString("SourceFile"), "attributeName equalsString");
        check(sf.attributeName().index() > 0, "attributeName has pool index");
    }

    // ---- Part 4: attributeMapper() 注册表身份 + attributes() 混合类型遍历 ----
    static void testMapperIdentityAndTraversal() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AT2"), cb -> cb
                .with(SourceFileAttribute.of("AT2.java"))
                .with(DeprecatedAttribute.of())
                .with(SyntheticAttribute.of())
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(5))));
        ClassModel cm = cf.parse(b);
        // attributeMapper() == Attributes.xxx() 同一 mapper（注册表身份）
        check(cm.findAttribute(Attributes.sourceFile()).get().attributeMapper() == Attributes.sourceFile(), "mapper identity sourceFile");
        check(cm.findAttribute(Attributes.deprecated()).get().attributeMapper() == Attributes.deprecated(), "mapper identity deprecated");
        check(cm.findAttribute(Attributes.synthetic()).get().attributeMapper() == Attributes.synthetic(), "mapper identity synthetic");
        check(cm.fields().get(0).findAttribute(Attributes.constantValue()).get().attributeMapper() == Attributes.constantValue(), "mapper identity constantValue");
        // attributes() 混合类型遍历：三类属性都在同一列表、名字可逐一取回
        Set<String> names = new HashSet<>();
        for (Attribute<?> a : cm.attributes()) names.add(a.attributeName().stringValue());
        check(names.contains("SourceFile") && names.contains("Deprecated") && names.contains("Synthetic"), "mixed-type traversal sees all: " + names);
        check(cm.attributes().size() == 3, "attributes list exact size: " + cm.attributes().size());
        // Attribute 是 ClassFileElement（白盒层次）
        check(ClassFileElement.class.isAssignableFrom(Attribute.class), "Attribute is-a ClassFileElement");
        // 每个属性的 mapper name 与 attributeName 一致（自洽不变量）
        for (Attribute<?> a : cm.attributes())
            check(a.attributeMapper().name().equals(a.attributeName().stringValue()), "mapper name matches attributeName for " + a.attributeName().stringValue());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCanonicalNames();
        testMapperIdentityAndTraversal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
