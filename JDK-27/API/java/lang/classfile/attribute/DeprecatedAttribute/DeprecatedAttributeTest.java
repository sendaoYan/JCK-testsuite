/*
 * @test
 * @summary java.lang.classfile.attribute.DeprecatedAttribute conformance: white-box structure + black-box behavior.
 * @run main DeprecatedAttributeTest
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

public class DeprecatedAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.DeprecatedAttribute");
        check(c != null, "java.lang.classfile.attribute.DeprecatedAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.DeprecatedAttribute"), "binary name java.lang.classfile.attribute.DeprecatedAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.DeprecatedAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.DeprecatedAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.DeprecatedAttribute has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        DeprecatedAttribute a = DeprecatedAttribute.of();
        check(a.attributeName().stringValue().equals("Deprecated"), "attributeName");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(DeprecatedAttribute.of()));
        check(ClassFile.of().parse(b).findAttribute(Attributes.deprecated()).isPresent(), "round-trip");

    }

    // ---- Part 3: 元数据 + allowMultiple 语义 ----
    static void testMetadata() {
        DeprecatedAttribute a = DeprecatedAttribute.of();
        check(a.attributeName().stringValue().equals(Attributes.NAME_DEPRECATED), "attributeName==NAME_DEPRECATED");
        check(a.attributeName().stringValue().equals("Deprecated"), "attributeName literal");
        check(a.attributeMapper() == Attributes.deprecated(), "attributeMapper==Attributes.deprecated()");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.STATELESS, "stability STATELESS");
        check(a.attributeMapper().allowMultiple(), "Deprecated allowMultiple true");
        check(Attributes.deprecated().name().equals("Deprecated"), "mapper.name()");
        // 无参工厂每次可用（无状态）
        check(DeprecatedAttribute.of() != null && DeprecatedAttribute.of() != null, "of() repeatable");
    }

    // ---- Part 4: 三级注入（类/方法/字段）+ 重复注入 ----
    static void testInjection() {
        ClassFile cf = ClassFile.of();
        // 类级
        byte[] b1 = cf.build(ClassDesc.of("DEPT1"), cb -> cb.with(DeprecatedAttribute.of()));
        check(cf.parse(b1).findAttribute(Attributes.deprecated()).isPresent(), "class-level injection");
        // 方法级 + 字段级
        byte[] b2 = cf.build(ClassDesc.of("DEPT2"), cb -> cb
                .withField("f", ConstantDescs.CD_int, fb -> fb.with(DeprecatedAttribute.of()))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> {
                    mb.with(DeprecatedAttribute.of());
                    mb.withCode(c -> c.return_());
                }));
        ClassModel cm = cf.parse(b2);
        check(cm.fields().get(0).findAttribute(Attributes.deprecated()).isPresent(), "field-level injection");
        check(cm.methods().get(0).findAttribute(Attributes.deprecated()).isPresent(), "method-level injection");
        check(cm.findAttribute(Attributes.deprecated()).isEmpty(), "not on class when only members annotated");
        // DeprecatedAttribute 同时是 ClassElement/MethodElement/FieldElement（多重通道）
        boolean asFieldElem = false, asMethodElem = false, asClassElem = false;
        for (FieldElement e : cm.fields().get(0).elementList()) if (e instanceof DeprecatedAttribute) asFieldElem = true;
        for (MethodElement e : cm.methods().get(0).elementList()) if (e instanceof DeprecatedAttribute) asMethodElem = true;
        for (ClassElement e : cf.parse(b1).elementList()) if (e instanceof DeprecatedAttribute) asClassElem = true;
        check(asFieldElem, "visible as FieldElement");
        check(asMethodElem, "visible as MethodElement");
        check(asClassElem, "visible as ClassElement");
        // allowMultiple=true：同类两次注入 → 两条都保留
        byte[] b3 = cf.build(ClassDesc.of("DEPT3"), cb -> cb.with(DeprecatedAttribute.of()).with(DeprecatedAttribute.of()));
        long n = cf.parse(b3).attributes().stream().filter(at -> at.attributeName().equalsString("Deprecated")).count();
        check(n == 2, "allowMultiple keeps both instances: " + n);
        check(cf.verify(b3).isEmpty(), "double Deprecated still verifies");
    }

    // ---- Part 5: javac 真类产物 ----
    @Deprecated
    static void depMethod() {}
    @Deprecated
    static int depField = 1;
    @Deprecated
    static class DepNested {}
    static void notDep() {}

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = DeprecatedAttributeTest.class.getResourceAsStream("DeprecatedAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        check(cm.findAttribute(Attributes.deprecated()).isEmpty(), "outer class itself not @Deprecated");
        boolean mFound = false, fFound = false, nd = true;
        for (MethodModel mm : cm.methods()) {
            boolean d = mm.findAttribute(Attributes.deprecated()).isPresent();
            if (mm.methodName().stringValue().equals("depMethod")) mFound = d;
            if (mm.methodName().stringValue().equals("notDep")) nd = !d;
        }
        for (FieldModel fm : cm.fields())
            if (fm.fieldName().stringValue().equals("depField")) fFound = fm.findAttribute(Attributes.deprecated()).isPresent();
        check(mFound, "real @Deprecated method carries attribute");
        check(fFound, "real @Deprecated field carries attribute");
        check(nd, "unannotated method free of attribute");
        // 嵌套 @Deprecated 类自身带属性
        byte[] nested = DeprecatedAttributeTest.class.getResourceAsStream("DeprecatedAttributeTest$DepNested.class").readAllBytes();
        check(ClassFile.of().parse(nested).findAttribute(Attributes.deprecated()).isPresent(), "nested @Deprecated class carries attribute");
        check(cf.verify(cm).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMetadata();
        testInjection();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
