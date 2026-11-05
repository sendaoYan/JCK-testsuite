/*
 * @test
 * @summary java.lang.classfile.attribute.NestHostAttribute conformance: white-box structure + black-box behavior.
 * @run main NestHostAttributeTest
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

public class NestHostAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.NestHostAttribute");
        check(c != null, "java.lang.classfile.attribute.NestHostAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.NestHostAttribute"), "binary name java.lang.classfile.attribute.NestHostAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.NestHostAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.NestHostAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.NestHostAttribute has no superclass");
        check(declares(c, "nestHost"), "declares nestHost");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        NestHostAttribute a = NestHostAttribute.of(ClassDesc.of("pkg.Outer"));
        check(a.nestHost().asInternalName().equals("pkg/Outer"), "nestHost");

    }


    // ---- real-product members ----
    static class NHInner { static class NHDeeper {} }

    // ---- Part 3: javac 真产物（嵌套类 NestHost == 顶层宿主内名）----
    static void testRealNestedProduct() throws Exception {
        byte[] inner = NestHostAttributeTest.class.getResourceAsStream("NestHostAttributeTest$NHInner.class").readAllBytes();
        var nh = ClassFile.of().parse(inner).findAttribute(Attributes.nestHost()).get();
        check(nh.nestHost().asInternalName().equals("NestHostAttributeTest"), "member nestHost == top-level internal name");
        check(nh.nestHost().asSymbol().descriptorString().equals("LNestHostAttributeTest;"), "nestHost asSymbol");
        check(nh.attributeMapper() == Attributes.nestHost(), "parsed mapper identity");
        check(nh.attributeName().stringValue().equals("NestHost"), "parsed attributeName");
        byte[] deeper = NestHostAttributeTest.class.getResourceAsStream("NestHostAttributeTest$NHInner$NHDeeper.class").readAllBytes();
        check(ClassFile.of().parse(deeper).findAttribute(Attributes.nestHost()).get().nestHost().asInternalName().equals("NestHostAttributeTest"),
                "deep member nestHost still top-level host");
        byte[] self = NestHostAttributeTest.class.getResourceAsStream("NestHostAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.nestHost()).isEmpty(), "top-level class has no NestHost");
    }

    // ---- Part 4: 工厂 of(ClassDesc)/of(ClassEntry) + null 负例 + 属性面 ----
    static void testFactoryOverloads() {
        NestHostAttribute a = NestHostAttribute.of(ClassDesc.of("SomeHost"));
        check(a.nestHost().asInternalName().equals("SomeHost"), "of(ClassDesc)");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("EntryHost"));
        check(NestHostAttribute.of(ce).nestHost() == ce, "of(ClassEntry) entry identity");
        check(a.attributeName().stringValue().equals("NestHost")
                && a.attributeName().stringValue().equals(Attributes.NAME_NEST_HOST), "NAME_NEST_HOST");
        check(a.attributeMapper() == Attributes.nestHost(), "mapper identity");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a.attributeMapper().allowMultiple(), "allowMultiple false");
        try { NestHostAttribute.of((ClassDesc) null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null ClassDesc) NPE"); }
        try { NestHostAttribute.of((ClassEntry) null); check(false, "of(null entry) must throw"); }
        catch (NullPointerException e) { check(true, "of(null ClassEntry) NPE"); }
    }

    // ---- Part 5: 注入顶层类（非规范产物，build 不做语义校验——实测记录）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynNHA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(NestHostAttribute.of(ClassDesc.of("FakeHost"))));
        ClassModel m = ClassFile.of().parse(b);
        check(m.findAttribute(Attributes.nestHost()).get().nestHost().asInternalName().equals("FakeHost"), "inject top-level readback");
        check(m.findAttribute(Attributes.nestHost()).get().attributeMapper() == Attributes.nestHost(), "parsed mapper identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealNestedProduct();
        testFactoryOverloads();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
