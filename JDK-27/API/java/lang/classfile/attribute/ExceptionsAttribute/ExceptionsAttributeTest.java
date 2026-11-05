/*
 * @test
 * @summary java.lang.classfile.attribute.ExceptionsAttribute conformance: white-box structure + black-box behavior.
 * @run main ExceptionsAttributeTest
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

public class ExceptionsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ExceptionsAttribute");
        check(c != null, "java.lang.classfile.attribute.ExceptionsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ExceptionsAttribute"), "binary name java.lang.classfile.attribute.ExceptionsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ExceptionsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ExceptionsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ExceptionsAttribute has no superclass");
        check(declares(c, "exceptions"), "declares exceptions");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofSymbols"), "declares ofSymbols");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ExceptionsAttribute a = ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.RuntimeException"));
        check(a.exceptions().size() == 2, "size");
        check(a.exceptions().get(0).asInternalName().equals("java/io/IOException"), "IOException");
        check(a.exceptions().get(1).asSymbol().equals(ClassDesc.of("java.lang.RuntimeException")), "RuntimeException");

    }

    // ---- Part 3: 四工厂全重载 + null/u2 负例 ----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ClassEntry e1 = cp.classEntry(ClassDesc.of("java.io.IOException"));
        ClassEntry e2 = cp.classEntry(ClassDesc.of("java.lang.IllegalStateException"));
        // 条目版：List 与 varargs
        ExceptionsAttribute a1 = ExceptionsAttribute.of(List.of(e1, e2));
        check(a1.exceptions().size() == 2 && a1.exceptions().get(0) == e1, "of(List<ClassEntry>) identity");
        ExceptionsAttribute a2 = ExceptionsAttribute.of(e1, e2);
        check(a2.exceptions().stream().map(ClassEntry::asInternalName).toList()
                .equals(List.of("java/io/IOException", "java/lang/IllegalStateException")), "of(varargs) internal names");
        // 符号版：List 与 varargs
        ExceptionsAttribute a3 = ExceptionsAttribute.ofSymbols(List.of(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.IllegalStateException")));
        check(a3.exceptions().size() == 2, "ofSymbols(List) size");
        ExceptionsAttribute a4 = ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.IllegalStateException"));
        check(a4.exceptions().stream().map(ClassEntry::asSymbol).map(ClassDesc::descriptorString).toList()
                .equals(List.of("Ljava/io/IOException;", "Ljava/lang/IllegalStateException;")), "ofSymbols(varargs) descriptors");
        check(ExceptionsAttribute.of(List.of()).exceptions().isEmpty(), "empty list ok");
        check(ExceptionsAttribute.ofSymbols().exceptions().isEmpty(), "empty varargs ok");
        // null 负例
        try { ExceptionsAttribute.of((ClassEntry[]) null); check(false, "of((ClassEntry[])null) must NPE"); }
        catch (NullPointerException ex) { check(ex.getMessage() != null, "array null NPE"); }
        try { ExceptionsAttribute.of((List<ClassEntry>) null); check(false, "of((List)null) must NPE"); }
        catch (NullPointerException ex) { check(ex.getMessage() != null, "list null NPE"); }
        try { ExceptionsAttribute.of(new ClassEntry[]{null}); check(false, "of({null}) must NPE"); }
        catch (NullPointerException ex) { check(true, "element null NPE"); }
        // 列表尺寸 u2 边界
        check(ExceptionsAttribute.ofSymbols(Collections.nCopies(65535, ClassDesc.of("java.io.IOException"))).exceptions().size() == 65535, "65535 ok");
        try {
            ExceptionsAttribute.ofSymbols(Collections.nCopies(66000, ClassDesc.of("java.io.IOException")));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("u2"), "66000 IAE msg: " + ex.getMessage());
        }
        // 元数据
        check(a1.attributeName().stringValue().equals(Attributes.NAME_EXCEPTIONS), "attributeName==NAME_EXCEPTIONS");
        check(a1.attributeName().stringValue().equals("Exceptions"), "attributeName literal");
        check(a1.attributeMapper() == Attributes.exceptions(), "attributeMapper==Attributes.exceptions()");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build 注入（条目版 + 符号版）→ re-parse 全等 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        // 条目版注入（条目来自游离池，写入时拷贝进目标池）
        byte[] b1 = cf.build(ClassDesc.of("EXT1"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC, mb -> {
                    mb.withCode(c -> c.return_());
                    mb.with(ExceptionsAttribute.of(List.of(cp.classEntry(ClassDesc.of("java.io.IOException")), cp.classEntry(ClassDesc.of("java.lang.IllegalStateException")))));
                }));
        MethodModel m1 = cf.parse(b1).methods().get(0);
        var f1 = m1.findAttribute(Attributes.exceptions());
        check(f1.isPresent(), "entry-based injection present");
        check(f1.get().exceptions().stream().map(ClassEntry::asInternalName).toList()
                .equals(List.of("java/io/IOException", "java/lang/IllegalStateException")), "entry-based roundtrip order+names");
        // 符号版注入
        byte[] b2 = cf.build(ClassDesc.of("EXT2"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC, mb -> {
                    mb.withCode(c -> c.return_());
                    mb.with(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.InterruptedException")));
                }));
        MethodModel m2 = cf.parse(b2).methods().get(0);
        check(m2.findAttribute(Attributes.exceptions()).get().exceptions().stream().map(ClassEntry::asInternalName).toList()
                .equals(List.of("java/io/IOException", "java/lang/InterruptedException")), "symbol-based roundtrip");
        // 单异常 + 空异常表
        byte[] b3 = cf.build(ClassDesc.of("EXT3"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> {
                    mb.withCode(c -> c.return_());
                    mb.with(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.lang.Exception")));
                }));
        check(cf.parse(b3).methods().get(0).findAttribute(Attributes.exceptions()).get().exceptions().size() == 1, "single exception");
        byte[] b4 = cf.build(ClassDesc.of("EXT4"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> {
                    mb.withCode(c -> c.return_());
                    mb.with(ExceptionsAttribute.ofSymbols());
                }));
        check(cf.parse(b4).methods().get(0).findAttribute(Attributes.exceptions()).get().exceptions().isEmpty(), "empty exceptions roundtrip");
        // MethodElement 通道
        boolean asMethodElem = false;
        for (MethodElement el : m1.elementList()) if (el instanceof ExceptionsAttribute) asMethodElem = true;
        check(asMethodElem, "visible as MethodElement");
        // 无注入的方法无 Exceptions
        check(cf.parse(b1).methods().size() == 1, "only one method");
    }

    // ---- Part 5: javac 真类产物（throws 子句）----
    static void throwing() throws java.io.IOException, IllegalStateException {}
    static void noThrow() {}

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = ExceptionsAttributeTest.class.getResourceAsStream("ExceptionsAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        boolean found = false, clean = true;
        for (MethodModel mm : cm.methods()) {
            var o = mm.findAttribute(Attributes.exceptions());
            if (mm.methodName().stringValue().equals("throwing")) {
                found = o.isPresent();
                check(o.get().exceptions().stream().map(ClassEntry::asInternalName).toList()
                        .equals(List.of("java/io/IOException", "java/lang/IllegalStateException")), "real throws table exact");
            }
            if (mm.methodName().stringValue().equals("noThrow")) clean = o.isEmpty();
        }
        check(found, "throwing method carries Exceptions");
        check(clean, "no-throw method free of Exceptions");
        check(cf.verify(cm).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
