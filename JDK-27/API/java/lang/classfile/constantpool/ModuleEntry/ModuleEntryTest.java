/*
 * @test
 * @summary java.lang.classfile.constantpool.ModuleEntry conformance: white-box structure + black-box behavior.
 * @run main ModuleEntryTest
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

public class ModuleEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ModuleEntry");
        check(c != null, "java.lang.classfile.constantpool.ModuleEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ModuleEntry"), "binary name java.lang.classfile.constantpool.ModuleEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ModuleEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ModuleEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ModuleEntry has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "asSymbol"), "declares asSymbol");
        check(declares(c, "matches"), "declares matches");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ModuleEntry me = cpb.moduleEntry(ModuleDesc.of("java.base"));
        check(me.name().stringValue().equals("java.base"), "name");
        check(me.asSymbol().equals(ModuleDesc.of("java.base")), "asSymbol");
        check(me.matches(ModuleDesc.of("java.base")), "matches");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ModuleEntry 契约（双工厂/去重/符号面/buildModule 往返/普通类池零出现） ----
    static void testModulePutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ModuleEntry m1 = b.moduleEntry(ModuleDesc.of("m1"));
        ModuleEntry m2 = b.moduleEntry(ModuleDesc.of("m1"));
        check(m1 == m2, "same-name moduleEntry dedup identity");
        check(b.moduleEntry(b.utf8Entry("m1")) == m1, "cross-overload dedup");
        check(b.moduleEntry(ModuleDesc.of("m2")) != m1, "different module different entry");
    }

    static void testModuleFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ModuleEntry m = b.moduleEntry(ModuleDesc.of("m1"));
        check(m.name().stringValue().equals("m1"), "name utf8");
        check(m.name() == b.utf8Entry("m1"), "name is the shared utf8 entry");
        check(m.asSymbol().name().equals("m1"), "asSymbol().name()");
        check(m.matches(ModuleDesc.of("m1")), "matches positive");
        check(!m.matches(ModuleDesc.of("m2")), "matches negative");
        check(m.tag() == PoolEntry.TAG_MODULE && m.tag() == 19, "tag TAG_MODULE == 19");
        check(m.width() == 1 && m.index() > 0 && m.constantPool() == b, "pool face");
        check(m instanceof PoolEntry && !(((Object) m) instanceof LoadableConstantEntry), "hierarchy PoolEntry only");
        ModuleEntry dotted = b.moduleEntry(ModuleDesc.of("a.b"));
        check(dotted.asSymbol().name().equals("a.b"), "dotted module name preserved");
    }

    static void testModuleBuildRoundtrip() throws Exception {
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m1"), mb -> mb.moduleFlags(ClassFile.ACC_OPEN).moduleVersion("1.0"));
        byte[] bytes = ClassFile.of().buildModule(ma);
        ClassModel mm = ClassFile.of().parse(bytes);
        check(mm.isModuleInfo(), "buildModule produces module-info");
        ModuleEntry mo = mm.findAttribute(Attributes.module()).orElseThrow().moduleName();
        check(mo instanceof ModuleEntry, "module attribute exposes ModuleEntry");
        check(mo.name().stringValue().equals("m1"), "module entry name roundtrip");
        check(mo.asSymbol().name().equals("m1"), "module asSymbol roundtrip");
        check(mo.matches(ModuleDesc.of("m1")), "module matches roundtrip");
        check(mo.tag() == PoolEntry.TAG_MODULE, "module tag in parsed pool");
        check(mo.constantPool() == mm.constantPool(), "module entry bound to pool");
        check(mm.constantPool().entryByIndex(mo.index(), ModuleEntry.class) == mo, "typed entryByIndex returns module entry");
    }

    static void testModuleAbsentInOrdinaryClass() throws Exception {
        byte[] self = ModuleEntryTest.class.getResourceAsStream("ModuleEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int n = 0;
        for (PoolEntry pe : cp) if (pe instanceof ModuleEntry) n++;
        check(n == 0, "ordinary class pool has no ModuleEntry");
    }

    static void testModuleNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.moduleEntry((ModuleDesc) null);
            check(false, "moduleEntry(null ModuleDesc) must throw");
        } catch (NullPointerException e) { check(true, "moduleEntry(null ModuleDesc) NPE"); }
        try {
            b.moduleEntry((Utf8Entry) null);
            check(false, "moduleEntry(null Utf8Entry) must throw");
        } catch (NullPointerException e) { check(true, "moduleEntry(null Utf8Entry) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testModulePutDedup();
        testModuleFace();
        testModuleBuildRoundtrip();
        testModuleAbsentInOrdinaryClass();
        testModuleNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
