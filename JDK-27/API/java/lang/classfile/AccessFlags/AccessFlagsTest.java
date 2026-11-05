/*
 * @test
 * @summary java.lang.classfile.AccessFlags conformance: white-box structure + black-box behavior.
 * @run main AccessFlagsTest
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

public class AccessFlagsTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AccessFlags");
        check(c != null, "java.lang.classfile.AccessFlags loads");
        check(c.getName().equals("java.lang.classfile.AccessFlags"), "binary name java.lang.classfile.AccessFlags");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AccessFlags is public");
        check(c.isInterface(), "java.lang.classfile.AccessFlags is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AccessFlags has no superclass");
        check(declares(c, "flagsMask"), "declares flagsMask");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "has"), "declares has");
        check(declares(c, "location"), "declares location");
        // AccessFlags 同时是三种成员位置的元素（javap 实测：extends ClassElement, MethodElement, FieldElement）
        Class<?>[] ifaces = c.getInterfaces();
        boolean hasCE = false, hasME = false, hasFE = false;
        for (Class<?> i : ifaces) {
            if (i.getName().equals("java.lang.classfile.ClassElement")) hasCE = true;
            if (i.getName().equals("java.lang.classfile.MethodElement")) hasME = true;
            if (i.getName().equals("java.lang.classfile.FieldElement")) hasFE = true;
        }
        check(hasCE && hasME && hasFE, "AccessFlags is ClassElement+MethodElement+FieldElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        AccessFlags af = cm.flags();
        check(af.location() == AccessFlag.Location.CLASS, "location CLASS");
        check(af.has(AccessFlag.PUBLIC), "has PUBLIC");
        check(af.has(AccessFlag.SUPER), "has SUPER");
        check(!af.has(AccessFlag.FINAL), "no FINAL");
        check(af.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flagsMask PUBLIC|SUPER");
        check(af.flags().contains(AccessFlag.PUBLIC) && af.flags().contains(AccessFlag.SUPER), "flags set");
        AccessFlags mf = cm.methods().get(0).flags();
        check(mf.location() == AccessFlag.Location.METHOD, "method location METHOD");
        check(mf.has(AccessFlag.PUBLIC) && mf.has(AccessFlag.STATIC), "method PUBLIC|STATIC");
        check(mf.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC), "method flagsMask");

    }

    // ---- Part 3: 三级 location + 富标志位掩码/集合/toString（build 注入回读，实测锁定） ----
    static void testFlagMatrix() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.AfM"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withField("x", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL)
                .withField("y", ConstantDescs.CD_Object, ClassFile.ACC_PRIVATE | ClassFile.ACC_VOLATILE | ClassFile.ACC_TRANSIENT)
                .withField("z", ConstantDescs.CD_Object, ClassFile.ACC_PROTECTED | ClassFile.ACC_ENUM)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC
                        | ClassFile.ACC_SYNCHRONIZED | ClassFile.ACC_BRIDGE, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        // class 级：build 默认加 ACC_PUBLIC|ACC_SUPER，location=CLASS
        AccessFlags cf_ = cm.flags();
        check(cf_.location() == AccessFlag.Location.CLASS, "class location CLASS");
        check(cf_.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "class mask 0x21");
        check(cf_.flags().equals(java.util.Set.of(AccessFlag.PUBLIC, AccessFlag.SUPER)), "class set {PUBLIC,SUPER}");
        check(cf_.has(AccessFlag.PUBLIC) && cf_.has(AccessFlag.SUPER), "class has");
        check(!cf_.has(AccessFlag.INTERFACE) && !cf_.has(AccessFlag.FINAL), "class lacks INTERFACE/FINAL");
        // toString 形如 AccessFlags[flags=33]（十进制掩码，实测锁定——不含标志名文本）
        check("AccessFlags[flags=33]".equals(cf_.toString()), "class toString decimal mask: " + cf_);
        // field 三组：location=FIELD + 精确 set/mask
        FieldModel fx = cm.fields().get(0), fy = cm.fields().get(1), fz = cm.fields().get(2);
        check(fx.flags().location() == AccessFlag.Location.FIELD, "field location FIELD");
        check(fx.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "field x mask 0x19");
        check(fx.flags().flags().equals(java.util.Set.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL)), "field x set");
        check(fx.flags().has(AccessFlag.STATIC) && fx.flags().has(AccessFlag.FINAL) && !fx.flags().has(AccessFlag.TRANSIENT), "field x has()");
        check("AccessFlags[flags=25]".equals(fx.flags().toString()), "field x toString: " + fx.flags());
        check(fy.flags().flagsMask() == (ClassFile.ACC_PRIVATE | ClassFile.ACC_VOLATILE | ClassFile.ACC_TRANSIENT), "field y mask 0xc2");
        check(fy.flags().flags().equals(java.util.Set.of(AccessFlag.PRIVATE, AccessFlag.VOLATILE, AccessFlag.TRANSIENT)), "field y set");
        check(fz.flags().flags().equals(java.util.Set.of(AccessFlag.PROTECTED, AccessFlag.ENUM)), "field z set {PROTECTED,ENUM}");
        // method 级：location=METHOD + SYNCHRONIZED/BRIDGE 位
        MethodModel mm = cm.methods().get(0);
        check(mm.flags().location() == AccessFlag.Location.METHOD, "method location METHOD");
        check(mm.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_SYNCHRONIZED | ClassFile.ACC_BRIDGE),
                "method mask 0x79");
        check(mm.flags().flags().equals(java.util.Set.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.SYNCHRONIZED, AccessFlag.BRIDGE)),
                "method set");
        check(mm.flags().has(AccessFlag.SYNCHRONIZED) && !mm.flags().has(AccessFlag.FINAL), "method has()");
        // flags() 返回不可变 Set（实测：add → UnsupportedOperationException）
        try {
            mm.flags().flags().add(AccessFlag.FINAL);
            check(false, "flags() set add must throw");
        } catch (UnsupportedOperationException e) {
            check(true, "flags() set immutable");
        }
        // has(null) → NPE（消息含 flag）
        try {
            mm.flags().has(null);
            check(false, "has(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"flag\" is null"),
                    "has(null) NPE message: " + e.getMessage());
        }
        // AccessFlags 元素在三级 elementList 中可见（多重继承身份）
        boolean cls = false, mtd = false, fld = false;
        for (ClassElement e : cm.elementList()) if (e instanceof AccessFlags) cls = true;
        for (MethodElement e : mm.elementList()) if (e instanceof AccessFlags) mtd = true;
        for (FieldElement e : fx.elementList()) if (e instanceof AccessFlags) fld = true;
        check(cls && mtd && fld, "AccessFlags element present at class/method/field elementLists");
    }

    // ---- Part 4: 非法位组合跨位置（ACC_STRICT 0x800 于 METHOD；ACC_INTERFACE 0x200 于 METHOD） ----
    static void testInvalidBits() throws Exception {
        // ACC_STRICT(0x800) 在 v71 已从 METHOD 位置合法集中移除：flagsMask 原样保留，
        // flags() 抛 IAE "Unmatched bit position 0x800 for location METHOD"，has(合法位) 仍可用（实测锁定）
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.AfS"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"),
                        0x0800 | ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        MethodModel sm = ClassFile.of().parse(b).methods().get(0);
        check(sm.flags().flagsMask() == (0x0800 | ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC), "STRICT mask 0x809 preserved");
        try {
            sm.flags().flags();
            check(false, "STRICT flags() must throw IAE");
        } catch (IllegalArgumentException e) {
            check("Unmatched bit position 0x800 for location METHOD".equals(e.getMessage()),
                    "STRICT IAE message: " + e.getMessage());
        }
        check(sm.flags().has(AccessFlag.PUBLIC), "STRICT has(PUBLIC) still works");
        check("AccessFlags[flags=2057]".equals(sm.flags().toString()), "STRICT toString 2057: " + sm.flags());
        // ACC_INTERFACE(0x200) 位在 METHOD 位置同样非法（合法位置是 CLASS/INNER_CLASS）
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.AfI"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"),
                        0x0200 | ClassFile.ACC_PUBLIC, cob -> cob.iconst_0().ireturn()));
        MethodModel im = ClassFile.of().parse(b2).methods().get(0);
        check(im.flags().flagsMask() == (0x0200 | ClassFile.ACC_PUBLIC), "IFACE-on-method mask 0x201 preserved");
        try {
            im.flags().flags();
            check(false, "IFACE-on-method flags() must throw IAE");
        } catch (IllegalArgumentException e) {
            check("Unmatched bit position 0x200 for location METHOD".equals(e.getMessage()),
                    "IFACE-on-method IAE message: " + e.getMessage());
        }
        check(im.flags().has(AccessFlag.PUBLIC), "IFACE-on-method has(PUBLIC) still works");
        check("AccessFlags[flags=513]".equals(im.flags().toString()), "IFACE-on-method toString 513: " + im.flags());
    }

    // ---- Part 5: 接口类/withFlags(AccessFlag...) 工厂注入 + INNER_CLASS 位置语义 ----
    static void testLocationVariants() throws Exception {
        // withFlags(int) 建 0x200|ABSTRACT 接口类：location CLASS 下 INTERFACE 位合法
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.AfItf"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"))
                .withFlags(0x0200 | ClassFile.ACC_ABSTRACT));
        ClassModel m = ClassFile.of().parse(b);
        check(m.flags().location() == AccessFlag.Location.CLASS, "itf location CLASS");
        check(m.flags().has(AccessFlag.INTERFACE) && m.flags().has(AccessFlag.ABSTRACT), "itf has INTERFACE|ABSTRACT");
        check(m.flags().flags().equals(java.util.Set.of(AccessFlag.INTERFACE, AccessFlag.ABSTRACT)), "itf set");
        check(m.flags().flagsMask() == 0x0600, "itf mask 0x600");
        // withFlags(AccessFlag...) 变体（javap 锁定的第二重载）
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.AfWf"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL));
        ClassModel m2 = ClassFile.of().parse(b2);
        check(m2.flags().flags().equals(java.util.Set.of(AccessFlag.PUBLIC, AccessFlag.FINAL)), "withFlags(varargs) set");
        check(m2.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL), "withFlags(varargs) mask");
        check(m2.flags().has(AccessFlag.FINAL), "withFlags(varargs) has FINAL");
        // withField/withMethod 默认 0 标志 → 空 flags()
        byte[] b3 = ClassFile.of().build(ClassDesc.of("pkg.AfZ"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withField("f", ConstantDescs.CD_int, 0));
        ClassModel m3 = ClassFile.of().parse(b3);
        check(m3.fields().get(0).flags().flagsMask() == 0, "zero-flag field mask 0");
        check(m3.fields().get(0).flags().flags().isEmpty(), "zero-flag field empty set");
        check(!m3.fields().get(0).flags().has(AccessFlag.PUBLIC), "zero-flag field has nothing");
        // AccessFlag.Location 九个位置（实测 values()==9）
        check(AccessFlag.Location.values().length == 9, "9 AccessFlag locations");
        // 抽象方法（无 code）flags 回读
        byte[] b4 = ClassFile.of().build(ClassDesc.of("pkg.AfAm"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethod("n", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_ABSTRACT | ClassFile.ACC_PUBLIC, mb -> { }));
        ClassModel m4 = ClassFile.of().parse(b4);
        check(m4.methods().get(0).flags().flags().equals(java.util.Set.of(AccessFlag.ABSTRACT, AccessFlag.PUBLIC)),
                "abstract method flags");
        check(m4.methods().get(0).flags().has(AccessFlag.ABSTRACT), "abstract method has ABSTRACT");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFlagMatrix();
        testInvalidBits();
        testLocationVariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
