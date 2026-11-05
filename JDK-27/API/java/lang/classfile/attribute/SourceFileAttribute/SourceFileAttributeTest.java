/*
 * @test
 * @summary java.lang.classfile.attribute.SourceFileAttribute conformance: white-box structure + black-box behavior.
 * @run main SourceFileAttributeTest
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

public class SourceFileAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.SourceFileAttribute");
        check(c != null, "java.lang.classfile.attribute.SourceFileAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.SourceFileAttribute"), "binary name java.lang.classfile.attribute.SourceFileAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.SourceFileAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.SourceFileAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.SourceFileAttribute has no superclass");
        check(declares(c, "sourceFile"), "declares sourceFile");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        SourceFileAttribute a = SourceFileAttribute.of("T.java");
        check(a.sourceFile().stringValue().equals("T.java"), "sourceFile");
        check(a.attributeName().stringValue().equals("SourceFile"), "attributeName");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(SourceFileAttribute.of("T.java")));
        check(ClassFile.of().parse(b).findAttribute(Attributes.sourceFile()).get().sourceFile().stringValue().equals("T.java"), "round-trip");

    }


    // ---- Part 3: 工厂 of(String)/of(Utf8Entry) + null + 属性面 ----
    static void testFactoryOverloads() {
        SourceFileAttribute a1 = SourceFileAttribute.of("X.java");
        check(a1.sourceFile().stringValue().equals("X.java"), "of(String)");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("Y.java");
        SourceFileAttribute a2 = SourceFileAttribute.of(u);
        check(a2.sourceFile() == u, "of(Utf8Entry) entry identity");
        check(a1.attributeName().stringValue().equals("SourceFile")
                && a1.attributeName().stringValue().equals(Attributes.NAME_SOURCE_FILE), "NAME_SOURCE_FILE");
        check(a1.attributeMapper() == Attributes.sourceFile(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { SourceFileAttribute.of((String) null); check(false, "of(null String) must throw"); }
        catch (NullPointerException e) { check(true, "of(null String) NPE"); }
        try { SourceFileAttribute.of((Utf8Entry) null); check(false, "of(null Utf8) must throw"); }
        catch (NullPointerException e) { check(true, "of(null Utf8) NPE"); }
    }

    // ---- Part 4: javac 真产物（源文件名 == <Simple>Test.java）----
    static void testRealProduct() throws Exception {
        byte[] self = SourceFileAttributeTest.class.getResourceAsStream("SourceFileAttributeTest.class").readAllBytes();
        var at = ClassFile.of().parse(self).findAttribute(Attributes.sourceFile()).get();
        check(at.sourceFile().stringValue().equals("SourceFileAttributeTest.java"), "javac sourceFile exact");
        check(at.attributeMapper() == Attributes.sourceFile(), "parsed mapper identity");
        byte[] nested = SourceFileAttributeTest.class.getResourceAsStream("SourceFileAttributeTest$CA.class").readAllBytes();
        check(ClassFile.of().parse(nested).findAttribute(Attributes.sourceFile()).get().sourceFile().stringValue().equals("SourceFileAttributeTest.java"),
                "nested class shares same source file");
    }

    // ---- Part 5: 注入回读 + 单实例语义 ----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynSFA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(SourceFileAttribute.of("Other.java")));
        check(ClassFile.of().parse(b).findAttribute(Attributes.sourceFile()).get().sourceFile().stringValue().equals("Other.java"),
                "inject overrides default name");
        // 默认 build 无 SourceFile（未显式注入时不自动生成）
        byte[] b2 = ClassFile.of().build(ClassDesc.of("DynSFA2"), cb -> cb.withSuperclass(ConstantDescs.CD_Object));
        check(ClassFile.of().parse(b2).findAttribute(Attributes.sourceFile()).isEmpty(), "fresh build no SourceFile");
        // 双注入只保留一个
        byte[] b3 = ClassFile.of().build(ClassDesc.of("DynSFA3"), cb -> cb
                .with(SourceFileAttribute.of("A.java")).with(SourceFileAttribute.of("B.java")));
        int cnt = 0;
        for (Attribute<?> at : ClassFile.of().parse(b3).attributes()) if (at instanceof SourceFileAttribute) cnt++;
        check(cnt == 1, "single SourceFile after double inject");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testRealProduct();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
