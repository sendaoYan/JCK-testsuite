/*
 * @test
 * @summary java.lang.classfile.attribute.InnerClassInfo conformance: white-box structure + black-box behavior.
 * @run main InnerClassInfoTest
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

public class InnerClassInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.InnerClassInfo");
        check(c != null, "java.lang.classfile.attribute.InnerClassInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.InnerClassInfo"), "binary name java.lang.classfile.attribute.InnerClassInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.InnerClassInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.InnerClassInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.InnerClassInfo has no superclass");
        check(declares(c, "innerClass"), "declares innerClass");
        check(declares(c, "outerClass"), "declares outerClass");
        check(declares(c, "innerName"), "declares innerName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        InnerClassInfo i = InnerClassInfo.of(ClassDesc.of("pkg.Outer$Inner"), java.util.Optional.of(ClassDesc.of("pkg.Outer")), java.util.Optional.of("Inner"), 0);
        check(i.innerClass().asInternalName().equals("pkg/Outer$Inner"), "innerClass");
        check(i.outerClass().get().asSymbol().equals(ClassDesc.of("pkg.Outer")), "outerClass");
        check(i.innerName().get().stringValue().equals("Inner"), "innerName");
        check(i.flagsMask() == 0, "flagsMask");

    }

    // ---- Part 3: 三工厂全重载 + 访问器矩阵 ----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ClassEntry in = cp.classEntry(ClassDesc.of("ICIT$In"));
        ClassEntry out = cp.classEntry(ClassDesc.of("ICIT"));
        Utf8Entry nm = cp.utf8Entry("In");
        // 条目版
        InnerClassInfo e1 = InnerClassInfo.of(in, Optional.of(out), Optional.of(nm), ClassFile.ACC_STATIC);
        check(e1.innerClass() == in, "entry factory keeps innerClass identity");
        check(e1.outerClass().get() == out, "entry factory outerClass identity");
        check(e1.innerName().get() == nm, "entry factory innerName identity");
        check(e1.flagsMask() == ClassFile.ACC_STATIC, "entry factory flags");
        check(e1.flags().equals(Set.of(AccessFlag.STATIC)), "flags() set");
        check(e1.has(AccessFlag.STATIC) && !e1.has(AccessFlag.PUBLIC), "has()");
        // 符号版（ClassDesc + Optional<String> + int）
        InnerClassInfo e2 = InnerClassInfo.of(ClassDesc.of("ICIT$A"), Optional.of(ClassDesc.of("ICIT")), Optional.of("A"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC);
        check(e2.innerClass().asInternalName().equals("ICIT$A"), "symbol factory inner name");
        check(e2.outerClass().get().asSymbol().descriptorString().equals("LICIT;"), "symbol factory outer");
        check(e2.innerName().get().stringValue().equals("A"), "symbol factory simple name");
        check(e2.flags().equals(Set.of(AccessFlag.PUBLIC, AccessFlag.STATIC)), "symbol factory flags");
        // 符号版（AccessFlag...）
        InnerClassInfo e3 = InnerClassInfo.of(ClassDesc.of("ICIT$I"), Optional.of(ClassDesc.of("ICIT")), Optional.of("I"), AccessFlag.INTERFACE, AccessFlag.ABSTRACT);
        check(e3.flagsMask() == (ClassFile.ACC_INTERFACE | ClassFile.ACC_ABSTRACT), "flag-varargs mask 0x400+0x400? -> interface|abstract");
        check(e3.flags().equals(Set.of(AccessFlag.INTERFACE, AccessFlag.ABSTRACT)), "flag-varargs set");
        // 匿名风格：outer/innerName 均空
        InnerClassInfo e4 = InnerClassInfo.of(ClassDesc.of("ICIT$1"), Optional.empty(), Optional.empty(), 0);
        check(e4.outerClass().isEmpty() && e4.innerName().isEmpty() && e4.flagsMask() == 0 && e4.flags().isEmpty(), "anon-style all empty");
        // protected/private 组合
        InnerClassInfo e5 = InnerClassInfo.of(ClassDesc.of("ICIT$P"), Optional.empty(), Optional.of("P"), ClassFile.ACC_PROTECTED);
        check(e5.has(AccessFlag.PROTECTED), "protected flag");
    }

    // ---- Part 4: null / u2 负例 ----
    static void testNegatives() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        try { InnerClassInfo.of((ClassEntry) null, Optional.empty(), Optional.empty(), 0); check(false, "innerClass null must NPE"); }
        catch (NullPointerException ex) { check(true, "innerClass null NPE"); }
        try { InnerClassInfo.of(cp.classEntry(ClassDesc.of("X")), null, Optional.empty(), 0); check(false, "outerClass Optional null must NPE"); }
        catch (NullPointerException ex) { check(true, "outerClass null NPE"); }
        try { InnerClassInfo.of(cp.classEntry(ClassDesc.of("X")), Optional.empty(), null, 0); check(false, "innerName Optional null must NPE"); }
        catch (NullPointerException ex) { check(true, "innerName null NPE"); }
        try { InnerClassInfo.of(ClassDesc.of("X"), Optional.empty(), Optional.empty(), -1); check(false, "neg flags must IAE"); }
        catch (IllegalArgumentException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("flags"), "flags IAE msg: " + ex.getMessage());
        }
        try { InnerClassInfo.of(ClassDesc.of("X"), Optional.empty(), Optional.empty(), 0x10000); check(false, "flags 65536 must IAE"); }
        catch (IllegalArgumentException ex) { check(true, "flags 65536 IAE"); }
        check(InnerClassInfo.of(ClassDesc.of("X"), Optional.empty(), Optional.empty(), 0xFFFF).flagsMask() == 0xFFFF, "u2 max ok");
    }

    // ---- Part 5: 注入 InnerClasses → re-parse 回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        InnerClassInfo info = InnerClassInfo.of(ClassDesc.of("ICIT2$In"), Optional.of(ClassDesc.of("ICIT2")), Optional.of("In"), ClassFile.ACC_STATIC);
        byte[] b = cf.build(ClassDesc.of("ICIT2"), cb -> cb.with(InnerClassesAttribute.of(info)));
        InnerClassInfo r = cf.parse(b).findAttribute(Attributes.innerClasses()).get().classes().get(0);
        check(r.innerClass().asInternalName().equals("ICIT2$In"), "rt inner");
        check(r.outerClass().get().asInternalName().equals("ICIT2"), "rt outer");
        check(r.innerName().get().stringValue().equals("In"), "rt name");
        check(r.flagsMask() == ClassFile.ACC_STATIC && r.has(AccessFlag.STATIC), "rt flags");
        check(r.innerClass().asSymbol().descriptorString().equals("LICIT2$In;"), "rt inner asSymbol");
        check(r.outerClass().get().asSymbol().descriptorString().equals("LICIT2;"), "rt outer asSymbol");
    }

    // ---- Part 6: javac 真类产物（成员嵌套类条目）----
    static class NI {}

    static void testRealClass() throws Exception {
        byte[] self = InnerClassInfoTest.class.getResourceAsStream("InnerClassInfoTest.class").readAllBytes();
        InnerClassesAttribute ic = ClassFile.of().parse(self).findAttribute(Attributes.innerClasses()).get();
        InnerClassInfo hit = null;
        for (InnerClassInfo i : ic.classes())
            if (i.innerClass().asInternalName().equals("InnerClassInfoTest$NI")) hit = i;
        check(hit != null, "NI entry present");
        check(hit.outerClass().get().asInternalName().equals("InnerClassInfoTest"), "NI outer exact");
        check(hit.innerName().get().stringValue().equals("NI"), "NI innerName exact");
        check(hit.flagsMask() == ClassFile.ACC_STATIC, "NI flags ACC_STATIC only");
        check(hit.flags().equals(Set.of(AccessFlag.STATIC)), "NI flags set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testNegatives();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
