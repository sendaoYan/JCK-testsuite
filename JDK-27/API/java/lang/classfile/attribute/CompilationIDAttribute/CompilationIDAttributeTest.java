/*
 * @test
 * @summary java.lang.classfile.attribute.CompilationIDAttribute conformance: white-box structure + black-box behavior.
 * @run main CompilationIDAttributeTest
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

public class CompilationIDAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.CompilationIDAttribute");
        check(c != null, "java.lang.classfile.attribute.CompilationIDAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.CompilationIDAttribute"), "binary name java.lang.classfile.attribute.CompilationIDAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.CompilationIDAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.CompilationIDAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.CompilationIDAttribute has no superclass");
        check(declares(c, "compilationId"), "declares compilationId");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        CompilationIDAttribute a = CompilationIDAttribute.of("cid-9");
        check(a.compilationId().stringValue().equals("cid-9"), "compilationId");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(CompilationIDAttribute.of("cid-9")));
        check(ClassFile.of().parse(b).findAttribute(Attributes.compilationId()).get().compilationId().stringValue().equals("cid-9"), "round-trip");

    }

    // ---- Part 3: 双工厂 + null 负例 + 元数据 ----
    static void testFactoryMatrix() {
        CompilationIDAttribute a1 = CompilationIDAttribute.of("ID-42");
        check(a1.compilationId().stringValue().equals("ID-42"), "of(String) readback");
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        Utf8Entry u = cp.utf8Entry("ID-43");
        CompilationIDAttribute a2 = CompilationIDAttribute.of(u);
        check(a2.compilationId().stringValue().equals("ID-43"), "of(Utf8Entry) readback");
        check(a2.compilationId() == u, "of(Utf8Entry) keeps entry identity");
        try { CompilationIDAttribute.of((String) null); check(false, "of((String)null) must NPE"); }
        catch (NullPointerException e) { check(true, "of((String)null) NPE"); }
        try { CompilationIDAttribute.of((Utf8Entry) null); check(false, "of((Utf8Entry)null) must NPE"); }
        catch (NullPointerException e) { check(true, "of((Utf8Entry)null) NPE"); }
        check(a1.attributeName().stringValue().equals(Attributes.NAME_COMPILATION_ID), "attributeName==NAME_COMPILATION_ID");
        check(a1.attributeName().stringValue().equals("CompilationID"), "attributeName literal");
        check(a1.attributeMapper() == Attributes.compilationId(), "attributeMapper==Attributes.compilationId()");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build 注入 → re-parse 回读（allowMultiple=false → 后写覆盖）----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CIDT1"), cb -> cb.with(CompilationIDAttribute.of("HW-2026")));
        ClassModel cm = cf.parse(b);
        var found = cm.findAttribute(Attributes.compilationId());
        check(found.isPresent(), "injected CompilationID present");
        check(found.get().compilationId().stringValue().equals("HW-2026"), "roundtrip value");
        check(found.get().attributeName().stringValue().equals("CompilationID"), "bound attributeName");
        // 元素通道：ClassElement，可在 elementList 中看到
        boolean inElements = false;
        for (ClassElement e : cm.elementList()) if (e instanceof CompilationIDAttribute) inElements = true;
        check(inElements, "CompilationIDAttribute visible as ClassElement");
        // 同名属性二次注入：allowMultiple=false → 仅保留最后写入
        byte[] b2 = cf.build(ClassDesc.of("CIDT2"), cb -> cb.with(CompilationIDAttribute.of("A")).with(CompilationIDAttribute.of("B")));
        int n = 0; String val = null;
        for (Attribute<?> at : cf.parse(b2).attributes())
            if (at instanceof CompilationIDAttribute c) { n++; val = c.compilationId().stringValue(); }
        check(n == 1, "double inject collapses to 1 attribute: " + n);
        check("B".equals(val), "last write wins (B)");
        // Utf8Entry 版注入
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        byte[] b3 = cf.build(ClassDesc.of("CIDT3"), cb -> cb.with(CompilationIDAttribute.of(cp.utf8Entry("E-ID"))));
        check(cf.parse(b3).findAttribute(Attributes.compilationId()).get().compilationId().stringValue().equals("E-ID"), "Utf8Entry inject roundtrip");
    }

    // ---- Part 5: javac 不产 CompilationID（HotSpot 内部属性）----
    static void testRealClass() throws Exception {
        byte[] self = CompilationIDAttributeTest.class
                .getResourceAsStream("CompilationIDAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.compilationId()).isEmpty(), "javac emits no CompilationID");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
