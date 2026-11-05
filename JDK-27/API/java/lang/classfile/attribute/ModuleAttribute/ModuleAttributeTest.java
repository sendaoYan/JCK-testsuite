/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleAttribute conformance: white-box structure + black-box behavior.
 * @run main ModuleAttributeTest
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

public class ModuleAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleAttribute");
        check(c != null, "java.lang.classfile.attribute.ModuleAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleAttribute"), "binary name java.lang.classfile.attribute.ModuleAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleAttribute has no superclass");
        check(declares(c, "moduleName"), "declares moduleName");
        check(declares(c, "moduleFlagsMask"), "declares moduleFlagsMask");
        check(declares(c, "moduleFlags"), "declares moduleFlags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("com.example"), b -> b.moduleVersion("1.0").requires(ModuleDesc.of("java.base"), java.util.Set.of(AccessFlag.MANDATED), null));
        check(a.moduleName().asSymbol().equals(ModuleDesc.of("com.example")), "moduleName");
        check(a.moduleVersion().get().stringValue().equals("1.0"), "moduleVersion");
        check(a.requires().size() == 1, "requires size");
        check(a.requires().get(0).requires().asSymbol().equals(ModuleDesc.of("java.base")), "requires module");
        check(a.moduleFlagsMask() >= 0, "moduleFlagsMask");

    }

    // ---- Part 3: of(ModuleDesc, builder) 全链构造 → buildModule → re-parse 全访问器 ----
    static void testFullModule() {
        ClassFile cf = ClassFile.of();
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.x"), mb -> mb
                .moduleFlags(ClassFile.ACC_OPEN | ClassFile.ACC_SYNTHETIC)
                .moduleVersion("1.2")
                .requires(ModuleDesc.of("java.base"), 0, null)
                .requires(ModuleRequireInfo.of(ModuleDesc.of("java.logging"), ClassFile.ACC_TRANSITIVE, "9"))
                .exports(PackageDesc.of("m.x.api"), 0, ModuleDesc.of("m.y"), ModuleDesc.of("m.z"))
                .exports(ModuleExportInfo.of(PackageDesc.of("m.x.spi"), ClassFile.ACC_SYNTHETIC))
                .opens(PackageDesc.of("m.x.impl"), ClassFile.ACC_TRANSITIVE, ModuleDesc.of("m.w"))
                .uses(ClassDesc.of("m.x.api.Svc"))
                .provides(ClassDesc.of("m.x.api.Svc"), ClassDesc.of("m.x.impl.SvcImpl")));
        byte[] b = cf.buildModule(ma);
        ClassModel cm = cf.parse(b);
        check(cm.isModuleInfo(), "isModuleInfo");
        check((cm.flags().flagsMask() & ClassFile.ACC_MODULE) != 0, "ACC_MODULE flag");
        ModuleAttribute r = cm.findAttribute(Attributes.module()).get();
        check(r.moduleName().asSymbol().name().equals("m.x"), "moduleName roundtrip");
        check(r.moduleFlagsMask() == (ClassFile.ACC_OPEN | ClassFile.ACC_SYNTHETIC), "moduleFlagsMask 0x1020");
        check(r.moduleFlags().equals(Set.of(AccessFlag.OPEN, AccessFlag.SYNTHETIC)), "moduleFlags set");
        check(r.has(AccessFlag.OPEN) && !r.has(AccessFlag.MANDATED), "has()");
        check(r.moduleVersion().get().stringValue().equals("1.2"), "moduleVersion 1.2");
        // requires
        check(r.requires().size() == 2, "2 requires");
        check(r.requires().get(0).requires().asSymbol().name().equals("java.base"), "req0 java.base");
        check(r.requires().get(0).requiresFlagsMask() == 0 && r.requires().get(0).requiresVersion().isEmpty(), "req0 no flags/version");
        check(r.requires().get(1).requires().asSymbol().name().equals("java.logging"), "req1 java.logging");
        check(r.requires().get(1).requiresFlagsMask() == ClassFile.ACC_TRANSITIVE, "req1 TRANSITIVE");
        check(r.requires().get(1).requiresVersion().get().stringValue().equals("9"), "req1 version 9");
        check(r.requires().get(1).has(AccessFlag.TRANSITIVE), "req1 has()");
        // exports
        check(r.exports().size() == 2, "2 exports");
        check(r.exports().get(0).exportedPackage().asSymbol().name().equals("m.x.api"), "exp0 pkg");
        check(r.exports().get(0).exportsTo().stream().map(e -> e.asSymbol().name()).toList()
                .equals(List.of("m.y", "m.z")), "exp0 targets ordered");
        check(r.exports().get(0).exportsFlagsMask() == 0, "exp0 no flags");
        check(r.exports().get(1).exportedPackage().asSymbol().name().equals("m.x.spi"), "exp1 pkg");
        check(r.exports().get(1).exportsTo().isEmpty(), "exp1 qualified->empty targets (unqualified)");
        check(r.exports().get(1).has(AccessFlag.SYNTHETIC), "exp1 SYNTHETIC");
        // opens
        check(r.opens().size() == 1, "1 open");
        check(r.opens().get(0).openedPackage().asSymbol().name().equals("m.x.impl"), "open pkg");
        check(r.opens().get(0).opensTo().stream().map(e -> e.asSymbol().name()).toList().equals(List.of("m.w")), "open target");
        check(r.opens().get(0).opensFlagsMask() == ClassFile.ACC_TRANSITIVE, "open flags");
        // uses / provides
        check(r.uses().stream().map(ClassEntry::asInternalName).toList().equals(List.of("m/x/api/Svc")), "uses");
        check(r.provides().size() == 1, "1 provides");
        check(r.provides().get(0).provides().asInternalName().equals("m/x/api/Svc"), "provides service");
        check(r.provides().get(0).providesWith().stream().map(ClassEntry::asInternalName).toList()
                .equals(List.of("m/x/impl/SvcImpl")), "providesWith impl");
        check(cf.verify(b).isEmpty(), "module-info verifies");
        // 元数据
        check(r.attributeName().stringValue().equals(Attributes.NAME_MODULE), "attributeName==NAME_MODULE");
        check(r.attributeName().stringValue().equals("Module"), "attributeName literal");
        check(r.attributeMapper() == Attributes.module(), "attributeMapper==Attributes.module()");
        check(r.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!r.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: 空 builder + of(ModuleEntry,...) 条目版 + u2 边界 ----
    static void testEntryFactoryAndBoundaries() {
        ClassFile cf = ClassFile.of();
        // 空 builder：全空表 + 0 flags
        ModuleAttribute empty = ModuleAttribute.of(ModuleDesc.of("m.e"), mb -> {});
        check(empty.moduleName().asSymbol().name().equals("m.e"), "empty name");
        check(empty.moduleFlagsMask() == 0 && empty.moduleVersion().isEmpty(), "empty flags/version");
        check(empty.requires().isEmpty() && empty.exports().isEmpty() && empty.opens().isEmpty()
                && empty.uses().isEmpty() && empty.provides().isEmpty(), "empty all tables");
        byte[] eb = cf.buildModule(empty);
        ModuleAttribute er = cf.parse(eb).findAttribute(Attributes.module()).get();
        check(er.requires().isEmpty() && er.moduleVersion().isEmpty(), "empty roundtrip");
        check(er.moduleName().asSymbol().name().equals("m.e"), "empty roundtrip name");
        // of(ModuleEntry, builder)：条目名
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ModuleAttribute viaEntry = ModuleAttribute.of(cp.moduleEntry(ModuleDesc.of("m.f")), mb -> mb.uses(cp.classEntry(ClassDesc.of("p.S"))));
        check(viaEntry.moduleName().asSymbol().name().equals("m.f"), "of(ModuleEntry, builder) name");
        check(viaEntry.uses().get(0).asInternalName().equals("p/S"), "uses via ClassEntry");
        // of(ModuleEntry, int, Utf8Entry, Collection x5) 8 参版
        ModuleAttribute full = ModuleAttribute.of(cp.moduleEntry(ModuleDesc.of("m.g")), ClassFile.ACC_OPEN, cp.utf8Entry("2.0"),
                List.of(ModuleRequireInfo.of(ModuleDesc.of("java.base"), 0, null)),
                List.of(ModuleExportInfo.of(PackageDesc.of("p"), 0)),
                List.of(ModuleOpenInfo.of(PackageDesc.of("q"), 0)),
                List.of(cp.classEntry(ClassDesc.of("p.U"))),
                List.of(ModuleProvideInfo.of(ClassDesc.of("p.S"), ClassDesc.of("p.Impl"))));
        check(full.moduleFlagsMask() == ClassFile.ACC_OPEN, "8-arg flags");
        check(full.moduleVersion().get().stringValue().equals("2.0"), "8-arg version");
        check(full.requires().size() == 1 && full.exports().size() == 1 && full.opens().size() == 1
                && full.uses().size() == 1 && full.provides().size() == 1, "8-arg all tables size 1");
        byte[] fb = cf.buildModule(full);
        ModuleAttribute fr = cf.parse(fb).findAttribute(Attributes.module()).get();
        check(fr.moduleName().asSymbol().name().equals("m.g") && fr.moduleVersion().get().stringValue().equals("2.0"), "8-arg roundtrip");
        // u2 边界：requires 66000 → IAE（与 JDK ListValidationTest 一致）
        ModuleEntry me = cp.moduleEntry(ModuleDesc.of("m.b"));
        ModuleRequireInfo rq = ModuleRequireInfo.of(ModuleDesc.of("java.base"), 0, null);
        ModuleExportInfo ex = ModuleExportInfo.of(PackageDesc.of("p"), 0, List.of());
        ModuleOpenInfo op = ModuleOpenInfo.of(PackageDesc.of("p"), 0, List.of());
        ClassEntry ce = cp.classEntry(ClassDesc.of("java.lang.String"));
        ModuleProvideInfo pv = ModuleProvideInfo.of(ClassDesc.of("p.S"), List.of());
        ModuleAttribute.of(me, 0, null, Collections.nCopies(65535, rq), List.of(ex), List.of(op), List.of(ce), List.of(pv));
        check(true, "65535 requires ok");
        try {
            ModuleAttribute.of(me, 0, null, Collections.nCopies(66000, rq), List.of(ex), List.of(op), List.of(ce), List.of(pv));
            check(false, "66000 requires must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "requires IAE msg");
        }
    }

    // ---- Part 5: buildModule 携带额外 class 属性（ModuleHashes 注入）----
    static void testBuildModuleWithExtras() {
        ClassFile cf = ClassFile.of();
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.h"), mb -> mb.moduleVersion("3.0"));
        ModuleHashesAttribute hashes = ModuleHashesAttribute.of("SHA-256",
                List.of(ModuleHashInfo.of(ModuleDesc.of("java.base"), new byte[]{1, 2, 3})));
        byte[] b = cf.buildModule(ma, cb -> cb.with(hashes));
        ClassModel cm = cf.parse(b);
        check(cm.isModuleInfo(), "with extras still module-info");
        check(cm.findAttribute(Attributes.module()).isPresent(), "module attr present");
        var h = cm.findAttribute(Attributes.moduleHashes());
        check(h.isPresent(), "moduleHashes co-injected");
        check(h.get().algorithm().stringValue().equals("SHA-256"), "hashes algo");
        check(h.get().hashes().get(0).moduleName().asSymbol().name().equals("java.base"), "hashes module");
        check(cf.verify(b).isEmpty(), "extras module verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFullModule();
        testEntryFactoryAndBoundaries();
        testBuildModuleWithExtras();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
