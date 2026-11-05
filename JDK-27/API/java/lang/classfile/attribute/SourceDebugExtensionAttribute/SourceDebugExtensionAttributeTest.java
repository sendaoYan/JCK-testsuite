/*
 * @test
 * @summary java.lang.classfile.attribute.SourceDebugExtensionAttribute conformance: white-box structure + black-box behavior.
 * @run main SourceDebugExtensionAttributeTest
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

public class SourceDebugExtensionAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.SourceDebugExtensionAttribute");
        check(c != null, "java.lang.classfile.attribute.SourceDebugExtensionAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.SourceDebugExtensionAttribute"), "binary name java.lang.classfile.attribute.SourceDebugExtensionAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.SourceDebugExtensionAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.SourceDebugExtensionAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.SourceDebugExtensionAttribute has no superclass");
        check(declares(c, "contents"), "declares contents");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] data = new byte[]{0x10, 0x20, 0x30};
        SourceDebugExtensionAttribute a = SourceDebugExtensionAttribute.of(data);
        check(a.contents().length == 3 && a.contents()[1] == 0x20, "contents");

    }


    // ---- Part 3: of(byte[]) 值域 + null 负例 ----
    static void testFactoryOverloads() {
        byte[] data = {1, 2, 3, -128, 0, 127, (byte) 0xFF};
        SourceDebugExtensionAttribute a = SourceDebugExtensionAttribute.of(data);
        check(a.contents().length == 7, "contents length");
        check(Arrays.equals(a.contents(), data), "contents value equality");
        check(SourceDebugExtensionAttribute.of(new byte[0]).contents().length == 0, "empty contents");
        check(a.attributeName().stringValue().equals("SourceDebugExtension")
                && a.attributeName().stringValue().equals(Attributes.NAME_SOURCE_DEBUG_EXTENSION), "NAME_SOURCE_DEBUG_EXTENSION");
        check(a.attributeMapper() == Attributes.sourceDebugExtension(), "mapper identity");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.STATELESS, "stability STATELESS");
        check(!a.attributeMapper().allowMultiple(), "allowMultiple false");
        try { SourceDebugExtensionAttribute.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    // ---- Part 4: 注入闭环（javac 从不产生该属性——注入是唯一通道）----
    static void testInjectRoundTrip() throws Exception {
        byte[] data = {83, 68, 69, 0, 10, 11, 12};
        byte[] b = ClassFile.of().build(ClassDesc.of("DynSDE"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(SourceDebugExtensionAttribute.of(data)));
        ClassModel m = ClassFile.of().parse(b);
        var at = m.findAttribute(Attributes.sourceDebugExtension()).get();
        check(Arrays.equals(at.contents(), data), "inject contents roundtrip");
        check(at.contents().length == 7, "inject length");
        check(at.attributeMapper() == Attributes.sourceDebugExtension(), "parsed mapper identity");
        check(ClassFile.of().verify(m).isEmpty(), "verify clean");
        // 重复注入（allowMultiple=false）只保留一个
        byte[] b2 = ClassFile.of().build(ClassDesc.of("DynSDE2"), cb -> cb
                .with(SourceDebugExtensionAttribute.of(new byte[]{1}))
                .with(SourceDebugExtensionAttribute.of(new byte[]{2, 2})));
        int cnt = 0;
        for (Attribute<?> at2 : ClassFile.of().parse(b2).attributes()) if (at2 instanceof SourceDebugExtensionAttribute) cnt++;
        check(cnt == 1, "single SDE after double inject: " + cnt);
    }

    // ---- Part 5: javac 不产（负空间断言）----
    static void testJavacAbsence() throws Exception {
        byte[] self = SourceDebugExtensionAttributeTest.class.getResourceAsStream("SourceDebugExtensionAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.sourceDebugExtension()).isEmpty(), "javac never emits SDE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testInjectRoundTrip();
        testJavacAbsence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
