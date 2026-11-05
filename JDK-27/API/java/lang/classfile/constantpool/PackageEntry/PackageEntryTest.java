/*
 * @test
 * @summary java.lang.classfile.constantpool.PackageEntry conformance: white-box structure + black-box behavior.
 * @run main PackageEntryTest
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

public class PackageEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.PackageEntry");
        check(c != null, "java.lang.classfile.constantpool.PackageEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.PackageEntry"), "binary name java.lang.classfile.constantpool.PackageEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.PackageEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.PackageEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.PackageEntry has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "asSymbol"), "declares asSymbol");
        check(declares(c, "matches"), "declares matches");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        PackageEntry pe = cpb.packageEntry(PackageDesc.of("java.lang"));
        check(pe.name().stringValue().equals("java/lang"), "name internal");
        check(pe.asSymbol().equals(PackageDesc.of("java.lang")), "asSymbol");
        check(pe.matches(PackageDesc.of("java.lang")), "matches");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: PackageEntry 契约（双工厂/去重/internalName-name 双形/符号面/普通类池零出现） ----
    static void testPackagePutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        PackageEntry p1 = b.packageEntry(PackageDesc.of("p1"));
        PackageEntry p2 = b.packageEntry(PackageDesc.of("p1"));
        check(p1 == p2, "same-name packageEntry dedup identity");
        check(b.packageEntry(b.utf8Entry("p1")) == p1, "cross-overload dedup");
        check(b.packageEntry(PackageDesc.of("p2")) != p1, "different package different entry");
    }

    static void testPackageFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        PackageEntry p = b.packageEntry(PackageDesc.of("p1"));
        check(p.name().stringValue().equals("p1"), "name utf8 is internal form");
        check(p.name() == b.utf8Entry("p1"), "name is the shared utf8 entry");
        check(p.asSymbol().name().equals("p1"), "asSymbol().name()");
        check(p.asSymbol().internalName().equals("p1"), "asSymbol().internalName()");
        check(p.matches(PackageDesc.of("p1")), "matches positive");
        check(!p.matches(PackageDesc.of("p2")), "matches negative");
        check(p.tag() == PoolEntry.TAG_PACKAGE && p.tag() == 20, "tag TAG_PACKAGE == 20");
        check(p.width() == 1 && p.index() > 0 && p.constantPool() == b, "pool face");
        check(p instanceof PoolEntry && !(((Object) p) instanceof LoadableConstantEntry), "hierarchy PoolEntry only");
        // 点分名与内名互转：池内统一存内名（斜线形）
        PackageEntry dotted = b.packageEntry(PackageDesc.of("a.b.c"));
        check(dotted.name().stringValue().equals("a/b/c"), "dotted name stored as internal form");
        check(dotted.asSymbol().internalName().equals("a/b/c"), "internalName slashed");
        check(dotted.asSymbol().name().equals("a.b.c"), "name dotted");
        check(dotted.matches(PackageDesc.of("a.b.c")), "dotted matches");
        check(dotted.matches(PackageDesc.ofInternalName("a/b/c")), "internalName matches");
    }

    static void testPackageAbsentInOrdinaryClass() throws Exception {
        byte[] self = PackageEntryTest.class.getResourceAsStream("PackageEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int n = 0;
        for (PoolEntry pe : cp) if (pe instanceof PackageEntry) n++;
        check(n == 0, "ordinary class pool has no PackageEntry");
        // null 负例
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.packageEntry((PackageDesc) null);
            check(false, "packageEntry(null PackageDesc) must throw");
        } catch (NullPointerException e) { check(true, "packageEntry(null PackageDesc) NPE"); }
        try {
            b.packageEntry((Utf8Entry) null);
            check(false, "packageEntry(null Utf8Entry) must throw");
        } catch (NullPointerException e) { check(true, "packageEntry(null Utf8Entry) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testPackagePutDedup();
        testPackageFace();
        testPackageAbsentInOrdinaryClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
