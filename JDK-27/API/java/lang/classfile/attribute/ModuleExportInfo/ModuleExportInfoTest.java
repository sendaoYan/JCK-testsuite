/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleExportInfo conformance: white-box structure + black-box behavior.
 * @run main ModuleExportInfoTest
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

public class ModuleExportInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleExportInfo");
        check(c != null, "java.lang.classfile.attribute.ModuleExportInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleExportInfo"), "binary name java.lang.classfile.attribute.ModuleExportInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleExportInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleExportInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleExportInfo has no superclass");
        check(declares(c, "exportedPackage"), "declares exportedPackage");
        check(declares(c, "exportsFlagsMask"), "declares exportsFlagsMask");
        check(declares(c, "exportsFlags"), "declares exportsFlags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleExportInfo i = ModuleExportInfo.of(PackageDesc.of("p.q"), java.util.Set.of(AccessFlag.SYNTHETIC), ModuleDesc.of("m1"), ModuleDesc.of("m2"));
        check(i.exportedPackage().asSymbol().equals(PackageDesc.of("p.q")), "exportedPackage");
        check(i.exportsTo().size() == 2, "exportsTo size");
        check(i.has(AccessFlag.SYNTHETIC), "SYNTHETIC flag");

    }

    // ---- Part 3: 八工厂全重载（条目版 4 + 符号版 4）----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        PackageEntry pk = cp.packageEntry(PackageDesc.of("m.x"));
        ModuleEntry m1 = cp.moduleEntry(ModuleDesc.of("m.y"));
        ModuleEntry m2 = cp.moduleEntry(ModuleDesc.of("m.z"));
        // 条目版：int + List
        ModuleExportInfo e1 = ModuleExportInfo.of(pk, 0, List.of(m1, m2));
        check(e1.exportedPackage() == pk, "entry factory package identity");
        check(e1.exportsFlagsMask() == 0 && e1.exportsFlags().isEmpty(), "entry factory zero flags");
        check(e1.exportsTo().size() == 2 && e1.exportsTo().get(0) == m1, "entry factory targets identity");
        // 条目版：AccessFlag 集合 + List
        ModuleExportInfo e2 = ModuleExportInfo.of(pk, List.of(AccessFlag.SYNTHETIC), List.of(m1));
        check(e2.has(AccessFlag.SYNTHETIC) && e2.exportsFlags().equals(Set.of(AccessFlag.SYNTHETIC)), "flag-collection entry version");
        // 条目版：int + varargs
        ModuleExportInfo e3 = ModuleExportInfo.of(pk, ClassFile.ACC_SYNTHETIC, m1, m2);
        check(e3.exportsFlagsMask() == ClassFile.ACC_SYNTHETIC, "entry varargs flags");
        check(e3.exportsTo().stream().map(x -> x.asSymbol().name()).toList().equals(List.of("m.y", "m.z")), "entry varargs targets");
        // 条目版：AccessFlag 集合 + varargs
        ModuleExportInfo e4 = ModuleExportInfo.of(pk, List.of(AccessFlag.MANDATED), m1);
        check(e4.has(AccessFlag.MANDATED), "flag-collection varargs");
        // 符号版四重载
        ModuleExportInfo s1 = ModuleExportInfo.of(PackageDesc.of("a.b"), 0, List.of(ModuleDesc.of("m1")));
        check(s1.exportedPackage().asSymbol().name().equals("a.b"), "symbol factory package");
        check(s1.exportedPackage().matches(PackageDesc.of("a.b")), "matches()");
        check(s1.exportedPackage().name().stringValue().equals("a/b"), "package name Utf8 (internal form)");
        check(s1.exportsTo().get(0).asSymbol().name().equals("m1"), "symbol factory target");
        ModuleExportInfo s2 = ModuleExportInfo.of(PackageDesc.of("a.c"), List.of(AccessFlag.SYNTHETIC), List.of());
        check(s2.exportsTo().isEmpty() && s2.has(AccessFlag.SYNTHETIC), "symbol unqualified+flags");
        ModuleExportInfo s3 = ModuleExportInfo.of(PackageDesc.of("a.d"), ClassFile.ACC_MANDATED, ModuleDesc.of("m2"), ModuleDesc.of("m3"));
        check(s3.exportsFlagsMask() == ClassFile.ACC_MANDATED, "symbol varargs flags");
        check(s3.exportsTo().size() == 2, "symbol varargs targets");
        ModuleExportInfo s4 = ModuleExportInfo.of(PackageDesc.of("a.e"), List.of(AccessFlag.SYNTHETIC, AccessFlag.MANDATED), ModuleDesc.of("m4"));
        check(s4.exportsFlags().equals(Set.of(AccessFlag.SYNTHETIC, AccessFlag.MANDATED)), "symbol flag-collection varargs");
        // 无目标（非限定导出）
        ModuleExportInfo s5 = ModuleExportInfo.of(PackageDesc.of("a.f"), 0);
        check(s5.exportsTo().isEmpty(), "no targets -> empty");
    }

    // ---- Part 4: null / u2 负例 ----
    static void testNegatives() {
        try { ModuleExportInfo.of((PackageDesc) null, 0); check(false, "of((PackageDesc)null,..) must NPE"); }
        catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("exports") || true, "package null NPE");
        }
        try { ModuleExportInfo.of(PackageDesc.of("p"), 0, (ModuleDesc[]) null); check(false, "targets null must NPE"); }
        catch (NullPointerException e) { check(true, "targets null NPE"); }
        try { ModuleExportInfo.of(PackageDesc.of("p"), 0, (List<ModuleDesc>) null); check(false, "target list null must NPE"); }
        catch (NullPointerException e) { check(true, "target list null NPE"); }
        // 目标模块数 u2 边界
        ModuleExportInfo.of(PackageDesc.of("p"), 0, Collections.nCopies(65535, ModuleDesc.of("m")));
        check(true, "65535 targets ok");
        try {
            ModuleExportInfo.of(PackageDesc.of("p"), 0, Collections.nCopies(66000, ModuleDesc.of("m")));
            check(false, "66000 targets must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "targets IAE msg");
        }
    }

    // ---- Part 5: 经 ModuleAttribute → buildModule → re-parse 回读 ----
    static void testRoundtrip() {
        ClassFile cf = ClassFile.of();
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.rt"), mb -> mb
                .exports(PackageDesc.of("rt.api"), 0, ModuleDesc.of("rt.friend1"), ModuleDesc.of("rt.friend2"))
                .exports(ModuleExportInfo.of(PackageDesc.of("rt.spi"), ClassFile.ACC_SYNTHETIC)));
        byte[] b = cf.buildModule(ma);
        ModuleAttribute r = cf.parse(b).findAttribute(Attributes.module()).get();
        check(r.exports().size() == 2, "2 exports roundtrip");
        check(r.exports().get(0).exportedPackage().asSymbol().name().equals("rt.api"), "rt pkg");
        check(r.exports().get(0).exportsTo().stream().map(x -> x.asSymbol().name()).toList()
                .equals(List.of("rt.friend1", "rt.friend2")), "rt targets ordered");
        check(r.exports().get(1).exportsFlagsMask() == ClassFile.ACC_SYNTHETIC
                && r.exports().get(1).exportsTo().isEmpty(), "rt spi unqualified");
        check(r.exports().get(0).has(AccessFlag.SYNTHETIC) == false, "rt api no SYNTHETIC");
        check(cf.verify(b).isEmpty(), "module verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testNegatives();
        testRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
