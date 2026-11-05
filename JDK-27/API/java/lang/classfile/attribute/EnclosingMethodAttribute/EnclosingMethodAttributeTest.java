/*
 * @test
 * @summary java.lang.classfile.attribute.EnclosingMethodAttribute conformance: white-box structure + black-box behavior.
 * @run main EnclosingMethodAttributeTest
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

public class EnclosingMethodAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.EnclosingMethodAttribute");
        check(c != null, "java.lang.classfile.attribute.EnclosingMethodAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.EnclosingMethodAttribute"), "binary name java.lang.classfile.attribute.EnclosingMethodAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.EnclosingMethodAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.EnclosingMethodAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.EnclosingMethodAttribute has no superclass");
        check(declares(c, "enclosingClass"), "declares enclosingClass");
        check(declares(c, "enclosingMethod"), "declares enclosingMethod");
        check(declares(c, "enclosingMethodName"), "declares enclosingMethodName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        EnclosingMethodAttribute a = EnclosingMethodAttribute.of(ClassDesc.of("pkg.Outer"), java.util.Optional.of("m"), java.util.Optional.of(MethodTypeDesc.of(ConstantDescs.CD_void)));
        check(a.enclosingClass().asInternalName().equals("pkg/Outer"), "enclosingClass");
        check(a.enclosingMethodName().get().stringValue().equals("m"), "enclosingMethodName");
        check(a.enclosingMethodTypeSymbol().get().equals(MethodTypeDesc.of(ConstantDescs.CD_void)), "enclosingMethodTypeSymbol");

    }

    // ---- Part 3: 双工厂族全重载 + 默认方法三兄弟 ----
    static void testFactories() {
        // of(ClassEntry, Optional<NameAndTypeEntry>)：条目版
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ClassEntry ce = cp.classEntry(ClassDesc.of("pkg.Outer"));
        NameAndTypeEntry nt = cp.nameAndTypeEntry("run", MethodTypeDesc.ofDescriptor("()V"));
        EnclosingMethodAttribute e1 = EnclosingMethodAttribute.of(ce, Optional.of(nt));
        check(e1.enclosingClass().asInternalName().equals("pkg/Outer"), "entry factory class");
        check(e1.enclosingMethod().get().name().stringValue().equals("run"), "entry factory method name");
        check(e1.enclosingMethod().get().type().stringValue().equals("()V"), "entry factory method type");
        check(e1.enclosingMethodName().get().stringValue().equals("run"), "enclosingMethodName default");
        check(e1.enclosingMethodType().get().stringValue().equals("()V"), "enclosingMethodType default");
        check(e1.enclosingMethodTypeSymbol().get().descriptorString().equals("()V"), "enclosingMethodTypeSymbol default");
        // 空方法（类初始化块/类型上下文之外）三兄弟全空
        EnclosingMethodAttribute e2 = EnclosingMethodAttribute.of(ce, Optional.empty());
        check(e2.enclosingMethod().isEmpty(), "empty enclosingMethod");
        check(e2.enclosingMethodName().isEmpty() && e2.enclosingMethodType().isEmpty() && e2.enclosingMethodTypeSymbol().isEmpty(), "empty derived trio");
        // of(ClassDesc, Optional<String>, Optional<MethodTypeDesc>)：符号版
        EnclosingMethodAttribute s1 = EnclosingMethodAttribute.of(ClassDesc.of("pkg.Outer"), Optional.of("go"), Optional.of(MethodTypeDesc.ofDescriptor("(I)I")));
        check(s1.enclosingClass().asSymbol().descriptorString().equals("Lpkg/Outer;"), "symbol factory class");
        check(s1.enclosingMethodName().get().stringValue().equals("go"), "symbol factory name");
        check(s1.enclosingMethodTypeSymbol().get().parameterCount() == 1, "symbol factory type params");
        EnclosingMethodAttribute s2 = EnclosingMethodAttribute.of(ClassDesc.of("pkg.Outer"), Optional.empty(), Optional.empty());
        check(s2.enclosingMethod().isEmpty() && s2.enclosingMethodName().isEmpty(), "symbol factory empty method");
        // null 负例
        try { EnclosingMethodAttribute.of((ClassEntry) null, Optional.empty()); check(false, "entry of(null) must NPE"); }
        catch (NullPointerException ex) { check(true, "entry of(null) NPE"); }
        try { EnclosingMethodAttribute.of(null, Optional.empty(), Optional.empty()); check(false, "symbol of(null) must NPE"); }
        catch (NullPointerException ex) { check(true, "symbol of(null) NPE"); }
        // 元数据
        check(e1.attributeName().stringValue().equals(Attributes.NAME_ENCLOSING_METHOD), "attributeName==NAME_ENCLOSING_METHOD");
        check(e1.attributeName().stringValue().equals("EnclosingMethod"), "attributeName literal");
        check(e1.attributeMapper() == Attributes.enclosingMethod(), "attributeMapper==Attributes.enclosingMethod()");
        check(e1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!e1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build 注入 → re-parse 回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("EMT1"), cb -> cb
                .with(EnclosingMethodAttribute.of(ClassDesc.of("pkg.Outer"), Optional.of("run"), Optional.of(MethodTypeDesc.ofDescriptor("()V")))));
        ClassModel cm = cf.parse(b);
        var found = cm.findAttribute(Attributes.enclosingMethod());
        check(found.isPresent(), "injected EnclosingMethod present");
        check(found.get().enclosingClass().asInternalName().equals("pkg/Outer"), "roundtrip class");
        check(found.get().enclosingMethodName().get().stringValue().equals("run"), "roundtrip name");
        check(found.get().enclosingMethodType().get().stringValue().equals("()V"), "roundtrip type");
        check(found.get().enclosingMethod().get().name().stringValue().equals("run"), "bound NameAndType readback");
        // 空方法版
        byte[] b2 = cf.build(ClassDesc.of("EMT2"), cb -> cb
                .with(EnclosingMethodAttribute.of(ClassDesc.of("pkg.Outer2"), Optional.empty(), Optional.empty())));
        EnclosingMethodAttribute e = cf.parse(b2).findAttribute(Attributes.enclosingMethod()).get();
        check(e.enclosingClass().asInternalName().equals("pkg/Outer2") && e.enclosingMethod().isEmpty(), "roundtrip empty method");
        boolean inElements = false;
        for (ClassElement el : cf.parse(b2).elementList()) if (el instanceof EnclosingMethodAttribute) inElements = true;
        check(inElements, "visible as ClassElement");
    }

    // ---- Part 5: javac 真类产物（匿名类 + 方法内 local class）----
    static Runnable makeAnon() { return new Runnable() { public void run() {} }; }
    static int withLocal() { class LocalIn {} return new LocalIn().hashCode(); }

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        // 匿名类：EnclosingMethod = 外围类 + 外围方法（名+描述符）
        byte[] anon = EnclosingMethodAttributeTest.class
                .getResourceAsStream("EnclosingMethodAttributeTest$1.class").readAllBytes();
        EnclosingMethodAttribute ea = cf.parse(anon).findAttribute(Attributes.enclosingMethod()).get();
        check(ea.enclosingClass().asInternalName().equals("EnclosingMethodAttributeTest"), "anon enclosing class");
        check(ea.enclosingMethod().get().name().stringValue().equals("makeAnon"), "anon enclosing method name");
        check(ea.enclosingMethod().get().type().stringValue().equals("()Ljava/lang/Runnable;"), "anon enclosing method type");
        check(ea.enclosingMethodTypeSymbol().get().descriptorString().equals("()Ljava/lang/Runnable;"), "anon type symbol");
        // local class：同样记录外围方法
        byte[] local = EnclosingMethodAttributeTest.class
                .getResourceAsStream("EnclosingMethodAttributeTest$1LocalIn.class").readAllBytes();
        EnclosingMethodAttribute el = cf.parse(local).findAttribute(Attributes.enclosingMethod()).get();
        check(el.enclosingClass().asInternalName().equals("EnclosingMethodAttributeTest"), "local enclosing class");
        check(el.enclosingMethodName().get().stringValue().equals("withLocal"), "local enclosing method name");
        // 顶层类无 EnclosingMethod
        byte[] self = EnclosingMethodAttributeTest.class
                .getResourceAsStream("EnclosingMethodAttributeTest.class").readAllBytes();
        check(cf.parse(self).findAttribute(Attributes.enclosingMethod()).isEmpty(), "top-level class has none");
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
