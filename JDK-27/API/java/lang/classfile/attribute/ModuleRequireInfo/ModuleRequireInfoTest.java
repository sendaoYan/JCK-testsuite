/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleRequireInfo conformance: white-box structure + black-box behavior.
 * @run main ModuleRequireInfoTest
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

public class ModuleRequireInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleRequireInfo");
        check(c != null, "java.lang.classfile.attribute.ModuleRequireInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleRequireInfo"), "binary name java.lang.classfile.attribute.ModuleRequireInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleRequireInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleRequireInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleRequireInfo has no superclass");
        check(declares(c, "requires"), "declares requires");
        check(declares(c, "requiresFlagsMask"), "declares requiresFlagsMask");
        check(declares(c, "requiresFlags"), "declares requiresFlags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleRequireInfo i = ModuleRequireInfo.of(ModuleDesc.of("java.base"), java.util.Set.of(AccessFlag.MANDATED), "11");
        check(i.requires().asSymbol().equals(ModuleDesc.of("java.base")), "requires");
        check(i.requiresVersion().get().stringValue().equals("11"), "requiresVersion");
        check(i.has(AccessFlag.MANDATED), "MANDATED");

    }


    // ---- Part 3: 工厂全重载矩阵（4 个 of，javap 锁定）----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ModuleEntry me = cpb.moduleEntry(ModuleDesc.of("java.base"));
        Utf8Entry v17 = cpb.utf8Entry("17");
        ModuleRequireInfo r1 = ModuleRequireInfo.of(me, 0x0020 | 0x0040, v17);
        check(r1.requires() == me, "of(entry,int,utf) entry identity");
        check(r1.requiresFlagsMask() == 0x60, "of(entry,int,utf) mask");
        check(r1.requiresVersion().get() == v17, "version entry identity");
        ModuleRequireInfo r2 = ModuleRequireInfo.of(me, Set.of(AccessFlag.TRANSITIVE, AccessFlag.SYNTHETIC), v17);
        check(r2.requiresFlagsMask() == 0x1020, "of(entry,Collection,utf) mask 0x1020");
        check(r2.requiresFlags().equals(Set.of(AccessFlag.TRANSITIVE, AccessFlag.SYNTHETIC)), "requiresFlags set");
        check(r2.has(AccessFlag.TRANSITIVE) && !r2.has(AccessFlag.STATIC_PHASE), "has() tri-state");
        ModuleRequireInfo r3 = ModuleRequireInfo.of(ModuleDesc.of("m.n"), 0x8000, "1.0");
        check(r3.requires().asSymbol().name().equals("m.n"), "of(desc,int,String) requires");
        check(r3.requiresVersion().get().stringValue().equals("1.0"), "version string");
        check(r3.has(AccessFlag.MANDATED), "MANDATED");
        ModuleRequireInfo r4 = ModuleRequireInfo.of(ModuleDesc.of("m.o"), Set.of(AccessFlag.STATIC_PHASE), null);
        check(r4.requiresFlagsMask() == 0x0040 && r4.requiresVersion().isEmpty(), "of(desc,Collection,null) empty version");
        ModuleRequireInfo r5 = ModuleRequireInfo.of(cpb.moduleEntry(ModuleDesc.of("m.p")), 0, null);
        check(r5.requiresFlagsMask() == 0 && r5.requiresVersion().isEmpty(), "of(entry,0,null) zero mask + empty version");
        check(r1.requires().name().stringValue().equals("java.base"), "requires name Utf8");
        check(AccessFlag.TRANSITIVE.locations().equals(Set.of(AccessFlag.Location.MODULE_REQUIRES)), "TRANSITIVE only in MODULE_REQUIRES");
    }

    // ---- Part 4: null 负例 + 非法位掩码 ----
    static void testNegatives() {
        try { ModuleRequireInfo.of((ModuleDesc) null, 0, "v"); check(false, "of(null desc) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null desc) NPE"); }
        try { ModuleRequireInfo.of((ModuleEntry) null, 0, null); check(false, "of(null entry) must throw"); }
        catch (NullPointerException e) { check(true, "of(null entry) NPE"); }
        ModuleRequireInfo bad = ModuleRequireInfo.of(ModuleDesc.of("m"), 0x0001, "v");
        check(bad.requiresFlagsMask() == 1, "bad mask kept at construction");
        try { bad.requiresFlags(); check(false, "requiresFlags bad bit must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unmatched bit position 0x1 for location MODULE_REQUIRES"),
                    "requiresFlags IAE exact msg: " + e.getMessage());
        }
    }

    // ---- Part 5: buildModule 注入→re-parse 全链闭环 ----
    static void testBuildModuleRoundTrip() throws Exception {
        ModuleRequireInfo rr = ModuleRequireInfo.of(ModuleDesc.of("java.base"), 0x0020, "17");
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.mri"), mb -> mb
                .requires(rr)
                .requires(ModuleDesc.of("m.other"), 0x0040, null));
        ClassModel mm = ClassFile.of().parse(ClassFile.of().buildModule(ma));
        List<ModuleRequireInfo> req = mm.findAttribute(Attributes.module()).get().requires();
        check(req.size() == 2, "requires count");
        check(req.get(0).requires().asSymbol().name().equals("java.base"), "req0 name");
        check(req.get(0).requiresFlagsMask() == 0x0020, "req0 mask");
        check(req.get(0).requiresVersion().get().stringValue().equals("17"), "req0 version");
        check(req.get(0).has(AccessFlag.TRANSITIVE), "req0 has TRANSITIVE");
        check(req.get(1).requiresVersion().isEmpty(), "req1 empty version roundtrip");
        check(ClassFile.of().verify(mm).isEmpty(), "verify clean");
    }

    // ---- Part 6: jrt 真实 module-info 产物 ----
    static void testRealModuleInfo() throws Exception {
        ClassModel jm = ClassFile.of().parse(Path.of(java.net.URI.create("jrt:/java.logging/module-info.class")));
        check(jm.isModuleInfo(), "jrt module-info parsed");
        List<ModuleRequireInfo> req = jm.findAttribute(Attributes.module()).get().requires();
        boolean base = false;
        for (ModuleRequireInfo r : req)
            if (r.requires().asSymbol().name().equals("java.base")) {
                base = true;
                check(r.requiresFlagsMask() == 0x8000, "java.base required MANDATED 0x8000");
                check(r.requiresVersion().isEmpty(), "java.base require no version");
                check(r.has(AccessFlag.MANDATED), "java.base has MANDATED");
            }
        check(base, "jrt java.logging requires java.base");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testNegatives();
        testBuildModuleRoundTrip();
        testRealModuleInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
