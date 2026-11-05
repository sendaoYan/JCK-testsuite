/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder conformance: white-box structure + black-box behavior.
 * @run main ModuleAttributeBuilderTest
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

public class ModuleAttributeBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleAttribute$ModuleAttributeBuilder");
        check(c != null, "java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleAttribute$ModuleAttributeBuilder"), "binary name java.lang.classfile.attribute.ModuleAttribute$ModuleAttributeBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder has no superclass");
        check(declares(c, "moduleName"), "declares moduleName");
        check(declares(c, "moduleFlags"), "declares moduleFlags");
        check(declares(c, "moduleVersion"), "declares moduleVersion");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("m"), mb -> mb.moduleVersion("2.0").requires(ModuleDesc.of("java.base"), 0x8000, null).exports(PackageDesc.of("p.q"), 0, ModuleDesc.of("m2")).uses(ClassDesc.of("p.Service")).provides(ClassDesc.of("p.Service"), ClassDesc.of("p.impl.Service")));
        check(a.moduleName().asSymbol().equals(ModuleDesc.of("m")), "moduleName");
        check(a.moduleVersion().get().stringValue().equals("2.0"), "moduleVersion");
        check(a.requires().size() == 1 && a.exports().size() == 1, "requires+exports");
        check(a.uses().size() == 1 && a.provides().size() == 1, "uses+provides");
        check(a.provides().get(0).provides().asSymbol().equals(ClassDesc.of("p.Service")), "provides service");

    }

    // ---- Part 3: 链式全方法返回 self + 每方法注入后属性回读 ----
    static void testBuilderChainReturnsSelf() {
        List<Boolean> self = new ArrayList<>();
        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("m.s"), mb -> {
            ModuleAttribute.ModuleAttributeBuilder r1 = mb.moduleVersion("9");
            ModuleAttribute.ModuleAttributeBuilder r2 = mb.requires(ModuleDesc.of("java.base"), 0, null);
            ModuleAttribute.ModuleAttributeBuilder r3 = mb.exports(PackageDesc.of("p1"), 0);
            ModuleAttribute.ModuleAttributeBuilder r4 = mb.opens(PackageDesc.of("p2"), 0);
            ModuleAttribute.ModuleAttributeBuilder r5 = mb.uses(ClassDesc.of("p.U"));
            ModuleAttribute.ModuleAttributeBuilder r6 = mb.provides(ClassDesc.of("p.S"), ClassDesc.of("p.SImpl"));
            ModuleAttribute.ModuleAttributeBuilder r7 = mb.moduleFlags(ClassFile.ACC_OPEN);
            ModuleAttribute.ModuleAttributeBuilder r8 = mb.moduleName(ModuleDesc.of("m.t"));
            self.add(r1 == mb); self.add(r2 == mb); self.add(r3 == mb); self.add(r4 == mb);
            self.add(r5 == mb); self.add(r6 == mb); self.add(r7 == mb); self.add(r8 == mb);
        });
        check(self.size() == 8 && self.stream().allMatch(x -> x), "all 8 chain methods return self");
        check(a.moduleName().asSymbol().name().equals("m.t"), "moduleName last write wins");
        check(a.moduleVersion().get().stringValue().equals("9"), "moduleVersion readback");
        check(a.moduleFlagsMask() == ClassFile.ACC_OPEN, "moduleFlags readback");
        check(a.requires().size() == 1 && a.exports().size() == 1 && a.opens().size() == 1
                && a.uses().size() == 1 && a.provides().size() == 1, "each table exactly 1");
    }

    // ---- Part 4: 各方法多值累积 + 重载族（int vs AccessFlag... vs Info 对象）----
    static void testAccumulationAndOverloads() {
        // requires 累积 + ModuleRequireInfo 对象重载
        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("m.a"), mb -> mb
                .requires(ModuleDesc.of("java.base"), 0, null)
                .requires(ModuleRequireInfo.of(ModuleDesc.of("m.other"), ClassFile.ACC_TRANSITIVE, "11"))
                .requires(ModuleDesc.of("m.third"), List.of(AccessFlag.TRANSITIVE, AccessFlag.STATIC_PHASE), "17"));
        check(a.requires().size() == 3, "3 requires accumulated");
        check(a.requires().get(1).has(AccessFlag.TRANSITIVE)
                && a.requires().get(1).requiresVersion().get().stringValue().equals("11"), "info-object overload");
        check(a.requires().get(2).requiresFlagsMask() == (ClassFile.ACC_TRANSITIVE | ClassFile.ACC_STATIC_PHASE), "flag-collection overload mask");
        // exports 累积 + ModuleExportInfo 重载 + AccessFlag 集合重载
        ModuleAttribute b = ModuleAttribute.of(ModuleDesc.of("m.b"), mb -> mb
                .exports(PackageDesc.of("p.a"), 0)
                .exports(ModuleExportInfo.of(PackageDesc.of("p.b"), ClassFile.ACC_SYNTHETIC))
                .exports(PackageDesc.of("p.c"), List.of(AccessFlag.SYNTHETIC), ModuleDesc.of("m.z")));
        check(b.exports().size() == 3, "3 exports accumulated");
        check(b.exports().get(0).exportsTo().isEmpty(), "unqualified export no targets");
        check(b.exports().get(1).has(AccessFlag.SYNTHETIC), "info-object export flags");
        check(b.exports().get(2).exportsTo().stream().map(e -> e.asSymbol().name()).toList().equals(List.of("m.z")), "flag-collection export target");
        // opens 累积 + ModuleOpenInfo 重载
        ModuleAttribute c = ModuleAttribute.of(ModuleDesc.of("m.c"), mb -> mb
                .opens(PackageDesc.of("q.a"), 0)
                .opens(ModuleOpenInfo.of(PackageDesc.of("q.b"), ClassFile.ACC_SYNTHETIC)));
        check(c.opens().size() == 2, "2 opens accumulated");
        check(c.opens().get(1).has(AccessFlag.SYNTHETIC), "info-object open");
        // uses 双通道（ClassDesc / ClassEntry）
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ModuleAttribute d = ModuleAttribute.of(ModuleDesc.of("m.d"), mb -> mb
                .uses(ClassDesc.of("p.U1"))
                .uses(cp.classEntry(ClassDesc.of("p.U2"))));
        check(d.uses().stream().map(ClassEntry::asInternalName).toList().equals(List.of("p/U1", "p/U2")), "uses both channels ordered");
        // provides 双通道（varargs / ModuleProvideInfo）
        ModuleAttribute e = ModuleAttribute.of(ModuleDesc.of("m.e"), mb -> mb
                .provides(ClassDesc.of("p.S"), ClassDesc.of("p.I1"), ClassDesc.of("p.I2"))
                .provides(ModuleProvideInfo.of(ClassDesc.of("p.T"), List.of(ClassDesc.of("p.I3")))));
        check(e.provides().size() == 2, "2 provides accumulated");
        check(e.provides().get(0).providesWith().size() == 2, "varargs provides with 2");
        check(e.provides().get(1).providesWith().stream().map(x -> x.asInternalName()).toList().equals(List.of("p/I3")), "info-object provides");
        // moduleFlags(AccessFlag...) 默认重载
        ModuleAttribute g = ModuleAttribute.of(ModuleDesc.of("m.g"), mb -> mb.moduleFlags(AccessFlag.OPEN, AccessFlag.SYNTHETIC));
        check(g.moduleFlagsMask() == (ClassFile.ACC_OPEN | ClassFile.ACC_SYNTHETIC), "moduleFlags(AccessFlag...) overload");
    }

    // ---- Part 5: 构造结果经 buildModule → parse 全链验证 ----
    static void testBuilderEndToEnd() {
        ClassFile cf = ClassFile.of();
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.full"), mb -> mb
                .moduleFlags(ClassFile.ACC_OPEN)
                .moduleVersion("5.0")
                .requires(ModuleDesc.of("java.base"), ClassFile.ACC_SYNTHETIC, null)
                .exports(PackageDesc.of("api"), 0, ModuleDesc.of("friend"))
                .opens(PackageDesc.of("impl"), 0)
                .uses(ClassDesc.of("api.Sp"))
                .provides(ClassDesc.of("api.Sp"), ClassDesc.of("impl.SpImpl")));
        byte[] bytes = cf.buildModule(ma);
        ModuleAttribute r = cf.parse(bytes).findAttribute(Attributes.module()).get();
        check(r.moduleName().asSymbol().name().equals("m.full"), "e2e name");
        check(r.moduleFlags().equals(Set.of(AccessFlag.OPEN)), "e2e flags");
        check(r.moduleVersion().get().stringValue().equals("5.0"), "e2e version");
        check(r.requires().get(0).requires().asSymbol().name().equals("java.base")
                && r.requires().get(0).has(AccessFlag.SYNTHETIC), "e2e require");
        check(r.exports().get(0).exportedPackage().asSymbol().name().equals("api")
                && r.exports().get(0).exportsTo().get(0).asSymbol().name().equals("friend"), "e2e export");
        check(r.opens().get(0).openedPackage().asSymbol().name().equals("impl"), "e2e open");
        check(r.uses().get(0).asInternalName().equals("api/Sp"), "e2e use");
        check(r.provides().get(0).providesWith().get(0).asInternalName().equals("impl/SpImpl"), "e2e provide");
        check(cf.verify(bytes).isEmpty(), "e2e verifies");
        // 空 builder → 最小 module-info
        byte[] min = cf.buildModule(ModuleAttribute.of(ModuleDesc.of("m.min"), mb -> {}));
        ClassModel mm = cf.parse(min);
        check(mm.isModuleInfo(), "minimal module-info");
        ModuleAttribute mr = mm.findAttribute(Attributes.module()).get();
        check(mr.moduleFlagsMask() == 0 && mr.requires().isEmpty() && mr.moduleVersion().isEmpty(), "minimal all empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuilderChainReturnsSelf();
        testAccumulationAndOverloads();
        testBuilderEndToEnd();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
