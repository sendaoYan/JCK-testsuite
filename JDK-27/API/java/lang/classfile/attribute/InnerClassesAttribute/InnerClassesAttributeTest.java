/*
 * @test
 * @summary java.lang.classfile.attribute.InnerClassesAttribute conformance: white-box structure + black-box behavior.
 * @run main InnerClassesAttributeTest
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

public class InnerClassesAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.InnerClassesAttribute");
        check(c != null, "java.lang.classfile.attribute.InnerClassesAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.InnerClassesAttribute"), "binary name java.lang.classfile.attribute.InnerClassesAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.InnerClassesAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.InnerClassesAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.InnerClassesAttribute has no superclass");
        check(declares(c, "classes"), "declares classes");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        InnerClassInfo ici = InnerClassInfo.of(ClassDesc.of("pkg.Outer$Inner"), java.util.Optional.of(ClassDesc.of("pkg.Outer")), java.util.Optional.of("Inner"), 0);
        InnerClassesAttribute a = InnerClassesAttribute.of(ici);
        check(a.classes().size() == 1, "classes size");
        check(a.classes().get(0).innerClass().asInternalName().equals("pkg/Outer$Inner"), "innerClass");
        check(a.classes().get(0).innerName().get().stringValue().equals("Inner"), "innerName");

    }

    // ---- Part 3: 工厂 + null/u2 负例 + 元数据 ----
    static void testFactories() {
        InnerClassInfo i1 = InnerClassInfo.of(ClassDesc.of("ICT$In"), Optional.of(ClassDesc.of("ICT")), Optional.of("In"), ClassFile.ACC_STATIC);
        InnerClassInfo i2 = InnerClassInfo.of(ClassDesc.of("ICT$1"), Optional.empty(), Optional.empty(), 0);
        // List 版 + varargs 版
        InnerClassesAttribute a1 = InnerClassesAttribute.of(List.of(i1, i2));
        check(a1.classes().size() == 2 && a1.classes().get(0) == i1, "of(List) identity");
        InnerClassesAttribute a2 = InnerClassesAttribute.of(i1, i2);
        check(a2.classes().size() == 2, "of(varargs) size");
        check(a2.classes().get(1).outerClass().isEmpty() && a2.classes().get(1).innerName().isEmpty(), "anon-style empties");
        check(InnerClassesAttribute.of(List.of()).classes().isEmpty(), "empty list ok");
        // null 负例
        try { InnerClassesAttribute.of((List<InnerClassInfo>) null); check(false, "of((List)null) must NPE"); }
        catch (NullPointerException ex) { check(true, "list null NPE"); }
        try { InnerClassesAttribute.of((InnerClassInfo[]) null); check(false, "of((arr)null) must NPE"); }
        catch (NullPointerException ex) { check(true, "array null NPE"); }
        // u2 尺寸边界
        InnerClassInfo shared = InnerClassInfo.of(ClassDesc.of("p.Q"), Optional.empty(), Optional.empty(), 0);
        check(InnerClassesAttribute.of(Collections.nCopies(65535, shared)).classes().size() == 65535, "65535 ok");
        try {
            InnerClassesAttribute.of(Collections.nCopies(66000, shared));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("u2"), "66000 IAE msg");
        }
        // 元数据
        check(a1.attributeName().stringValue().equals(Attributes.NAME_INNER_CLASSES), "attributeName==NAME_INNER_CLASSES");
        check(a1.attributeName().stringValue().equals("InnerClasses"), "attributeName literal");
        check(a1.attributeMapper() == Attributes.innerClasses(), "attributeMapper==Attributes.innerClasses()");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build 注入 → re-parse 回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        InnerClassInfo i1 = InnerClassInfo.of(ClassDesc.of("ICT2$In"), Optional.of(ClassDesc.of("ICT2")), Optional.of("In"), ClassFile.ACC_STATIC);
        InnerClassInfo i2 = InnerClassInfo.of(ClassDesc.of("ICT2$1"), Optional.empty(), Optional.empty(), 0);
        InnerClassInfo i3 = InnerClassInfo.of(ClassDesc.of("ICT2$Iface"), Optional.of(ClassDesc.of("ICT2")), Optional.of("Iface"), AccessFlag.INTERFACE, AccessFlag.ABSTRACT);
        byte[] b = cf.build(ClassDesc.of("ICT2"), cb -> cb.with(InnerClassesAttribute.of(List.of(i1, i2, i3))));
        ClassModel cm = cf.parse(b);
        var found = cm.findAttribute(Attributes.innerClasses());
        check(found.isPresent(), "injected InnerClasses present");
        List<InnerClassInfo> cs = found.get().classes();
        check(cs.size() == 3, "3 entries roundtrip");
        check(cs.get(0).innerClass().asInternalName().equals("ICT2$In"), "rt inner name");
        check(cs.get(0).outerClass().get().asInternalName().equals("ICT2"), "rt outer name");
        check(cs.get(0).innerName().get().stringValue().equals("In"), "rt simple name");
        check(cs.get(0).flagsMask() == ClassFile.ACC_STATIC && cs.get(0).flags().equals(Set.of(AccessFlag.STATIC)), "rt static flags");
        check(cs.get(1).outerClass().isEmpty() && cs.get(1).innerName().isEmpty() && cs.get(1).flagsMask() == 0, "rt anon entry");
        check(cs.get(2).flags().contains(AccessFlag.INTERFACE) && cs.get(2).flags().contains(AccessFlag.ABSTRACT), "rt interface flags");
        check(cs.get(2).has(AccessFlag.INTERFACE) && !cs.get(2).has(AccessFlag.STATIC), "has() query");
        boolean inElements = false;
        for (ClassElement e : cm.elementList()) if (e instanceof InnerClassesAttribute) inElements = true;
        check(inElements, "visible as ClassElement");
        check(cf.verify(b).isEmpty(), "injected class verifies");
    }

    // ---- Part 5: javac 真类产物（四类嵌套：class/enum/interface/record）----
    static class MyNested {}
    enum MyEnum { A }
    interface MyIface {}
    record MyRecord(int x) {}

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = InnerClassesAttributeTest.class.getResourceAsStream("InnerClassesAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        var found = cm.findAttribute(Attributes.innerClasses());
        check(found.isPresent(), "nested-bearing class has InnerClasses");
        Map<String, InnerClassInfo> own = new HashMap<>();
        for (InnerClassInfo i : found.get().classes())
            if (i.innerClass().asInternalName().startsWith("InnerClassesAttributeTest$"))
                own.put(i.innerClass().asInternalName(), i);
        check(own.size() >= 4, "own nested entries >= 4: " + own.size());
        check(own.containsKey("InnerClassesAttributeTest$MyNested"), "MyNested entry");
        check(own.get("InnerClassesAttributeTest$MyNested").outerClass().get().asInternalName().equals("InnerClassesAttributeTest"), "MyNested outer");
        check(own.get("InnerClassesAttributeTest$MyNested").innerName().get().stringValue().equals("MyNested"), "MyNested simple name");
        check(own.get("InnerClassesAttributeTest$MyNested").flags().equals(Set.of(AccessFlag.STATIC)), "MyNested flags [STATIC]");
        check(own.get("InnerClassesAttributeTest$MyEnum").flags().equals(Set.of(AccessFlag.STATIC, AccessFlag.FINAL, AccessFlag.ENUM)), "MyEnum flags [STATIC,FINAL,ENUM]");
        check(own.get("InnerClassesAttributeTest$MyEnum").flagsMask() == 0x4010 || own.get("InnerClassesAttributeTest$MyEnum").flagsMask() == 0x4018,
                "MyEnum mask 0x401x: " + Integer.toHexString(own.get("InnerClassesAttributeTest$MyEnum").flagsMask()));
        check(own.get("InnerClassesAttributeTest$MyIface").flags().containsAll(Set.of(AccessFlag.STATIC, AccessFlag.INTERFACE, AccessFlag.ABSTRACT)), "MyIface flags");
        check(own.get("InnerClassesAttributeTest$MyRecord").flags().containsAll(Set.of(AccessFlag.STATIC, AccessFlag.FINAL)), "MyRecord flags");
        // 嵌套类自身也带 InnerClasses（含自己 + 外围引用）
        byte[] nested = InnerClassesAttributeTest.class.getResourceAsStream("InnerClassesAttributeTest$MyNested.class").readAllBytes();
        var nestedFound = ClassFile.of().parse(nested).findAttribute(Attributes.innerClasses());
        check(nestedFound.isPresent(), "nested class itself carries InnerClasses");
        boolean selfEntry = false;
        for (InnerClassInfo i : nestedFound.get().classes())
            if (i.innerClass().asInternalName().equals("InnerClassesAttributeTest$MyNested")) selfEntry = true;
        check(selfEntry, "nested class lists itself");
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
