/*
 * @test
 * @summary java.lang.classfile.attribute.SourceIDAttribute conformance: white-box structure + black-box behavior.
 * @run main SourceIDAttributeTest
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

public class SourceIDAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.SourceIDAttribute");
        check(c != null, "java.lang.classfile.attribute.SourceIDAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.SourceIDAttribute"), "binary name java.lang.classfile.attribute.SourceIDAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.SourceIDAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.SourceIDAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.SourceIDAttribute has no superclass");
        check(declares(c, "sourceId"), "declares sourceId");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        SourceIDAttribute a = SourceIDAttribute.of("sid-1");
        check(a.sourceId().stringValue().equals("sid-1"), "sourceId");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(SourceIDAttribute.of("sid-1")));
        check(ClassFile.of().parse(b).findAttribute(Attributes.sourceId()).get().sourceId().stringValue().equals("sid-1"), "round-trip");

    }


    // ---- Part 3: 工厂 of(Utf8Entry)/of(String) + null + 属性面 ----
    static void testFactoryOverloads() {
        SourceIDAttribute a1 = SourceIDAttribute.of("hash-1234");
        check(a1.sourceId().stringValue().equals("hash-1234"), "of(String)");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("hash-5678");
        SourceIDAttribute a2 = SourceIDAttribute.of(u);
        check(a2.sourceId() == u, "of(Utf8Entry) entry identity");
        check(a1.attributeName().stringValue().equals("SourceID")
                && a1.attributeName().stringValue().equals(Attributes.NAME_SOURCE_ID), "NAME_SOURCE_ID");
        check(a1.attributeMapper() == Attributes.sourceId(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { SourceIDAttribute.of((String) null); check(false, "of(null String) must throw"); }
        catch (NullPointerException e) { check(true, "of(null String) NPE"); }
        try { SourceIDAttribute.of((Utf8Entry) null); check(false, "of(null Utf8) must throw"); }
        catch (NullPointerException e) { check(true, "of(null Utf8) NPE"); }
    }

    // ---- Part 4: 注入回读 + javac 不产（负空间）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynSID"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(SourceIDAttribute.of("id-xyz")));
        ClassModel m = ClassFile.of().parse(b);
        check(m.findAttribute(Attributes.sourceId()).get().sourceId().stringValue().equals("id-xyz"), "inject readback");
        check(m.findAttribute(Attributes.sourceId()).get().attributeMapper() == Attributes.sourceId(), "parsed mapper identity");
        check(ClassFile.of().verify(m).isEmpty(), "verify clean");
        // 双注入（allowMultiple=false）只保留一个
        byte[] b2 = ClassFile.of().build(ClassDesc.of("DynSID2"), cb -> cb
                .with(SourceIDAttribute.of("a")).with(SourceIDAttribute.of("b")));
        int cnt = 0;
        for (Attribute<?> at : ClassFile.of().parse(b2).attributes()) if (at instanceof SourceIDAttribute) cnt++;
        check(cnt == 1, "single SourceID after double inject");
        byte[] self = SourceIDAttributeTest.class.getResourceAsStream("SourceIDAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.sourceId()).isEmpty(), "javac never emits SourceID");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
