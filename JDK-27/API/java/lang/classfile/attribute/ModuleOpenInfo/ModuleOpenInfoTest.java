/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleOpenInfo conformance: white-box structure + black-box behavior.
 * @run main ModuleOpenInfoTest
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

public class ModuleOpenInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleOpenInfo");
        check(c != null, "java.lang.classfile.attribute.ModuleOpenInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleOpenInfo"), "binary name java.lang.classfile.attribute.ModuleOpenInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleOpenInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleOpenInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleOpenInfo has no superclass");
        check(declares(c, "openedPackage"), "declares openedPackage");
        check(declares(c, "opensFlagsMask"), "declares opensFlagsMask");
        check(declares(c, "opensFlags"), "declares opensFlags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleOpenInfo i = ModuleOpenInfo.of(PackageDesc.of("p.q"), java.util.Set.of(AccessFlag.MANDATED), ModuleDesc.of("m1"));
        check(i.openedPackage().asSymbol().equals(PackageDesc.of("p.q")), "openedPackage");
        check(i.opensTo().size() == 1, "opensTo size");
        check(i.has(AccessFlag.MANDATED), "MANDATED flag");

    }


    // ---- Part 3: 工厂全重载矩阵（8 个 of，javap 锁定）----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        PackageEntry pe = cpb.packageEntry(PackageDesc.of("p.q"));
        ModuleEntry me1 = cpb.moduleEntry(ModuleDesc.of("m.one"));
        ModuleEntry me2 = cpb.moduleEntry(ModuleDesc.of("m.two"));
        ModuleOpenInfo i1 = ModuleOpenInfo.of(pe, 0x1000, List.of(me1, me2));
        check(i1.openedPackage() == pe, "of(entry,int,List) entry identity");
        check(i1.opensFlagsMask() == 0x1000, "of(entry,int,List) mask");
        check(i1.opensTo().size() == 2 && i1.opensTo().get(0) == me1 && i1.opensTo().get(1) == me2, "opensTo entries identity");
        ModuleOpenInfo i2 = ModuleOpenInfo.of(pe, Set.of(AccessFlag.SYNTHETIC, AccessFlag.MANDATED), List.of(me1));
        check(i2.opensFlagsMask() == 0x9000, "of(entry,Collection,List) mask 0x9000");
        check(i2.opensFlags().equals(Set.of(AccessFlag.SYNTHETIC, AccessFlag.MANDATED)), "opensFlags set");
        check(i2.has(AccessFlag.SYNTHETIC) && i2.has(AccessFlag.MANDATED) && !i2.has(AccessFlag.TRANSITIVE), "has() tri-state");
        ModuleOpenInfo i3 = ModuleOpenInfo.of(pe, 0x8000, me1, me2);
        check(i3.opensFlagsMask() == 0x8000 && i3.opensTo().size() == 2, "of(entry,int,varargs)");
        ModuleOpenInfo i4 = ModuleOpenInfo.of(pe, Set.of(), new ModuleEntry[0]);
        check(i4.opensTo().isEmpty() && i4.opensFlagsMask() == 0, "of(entry,emptyCol,emptyVarargs)");
        ModuleOpenInfo i5 = ModuleOpenInfo.of(PackageDesc.of("p.r"), 0x1000, List.of(ModuleDesc.of("m.three")));
        check(i5.openedPackage().asSymbol().name().equals("p.r"), "of(desc,int,List) openedPackage");
        check(i5.openedPackage().matches(PackageDesc.of("p.r")), "openedPackage matches");
        check(i5.opensTo().get(0).asSymbol().name().equals("m.three"), "of(desc,int,List) opensTo symbol");
        ModuleOpenInfo i6 = ModuleOpenInfo.of(PackageDesc.of("p.s"), Set.of(AccessFlag.SYNTHETIC), List.of(ModuleDesc.of("m.four")));
        check(i6.opensFlagsMask() == 0x1000, "of(desc,Collection,List) mask");
        ModuleOpenInfo i7 = ModuleOpenInfo.of(PackageDesc.of("p.t"), 0, ModuleDesc.of("m.a"), ModuleDesc.of("m.b"));
        check(i7.opensTo().size() == 2 && i7.opensTo().get(1).asSymbol().name().equals("m.b"), "of(desc,int,varargs)");
        ModuleOpenInfo i8 = ModuleOpenInfo.of(PackageDesc.of("p.u"), Set.of(), new ModuleDesc[0]);
        check(i8.opensTo().isEmpty(), "of(desc,Collection,empty)");
        check(i7.openedPackage().name().stringValue().equals("p/t"), "openedPackage name Utf8 slash form");
        check(AccessFlag.SYNTHETIC.locations().contains(AccessFlag.Location.MODULE_OPENS), "SYNTHETIC valid in MODULE_OPENS");
        check(AccessFlag.MANDATED.locations().contains(AccessFlag.Location.MODULE_OPENS), "MANDATED valid in MODULE_OPENS");
    }

    // ---- Part 4: null 负例 + 非法位掩码语义 ----
    static void testNegatives() {
        try { ModuleOpenInfo.of((PackageDesc) null, 0, new ModuleDesc[0]); check(false, "of(null pkg) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null pkg) NPE"); }
        try { ModuleOpenInfo.of(PackageDesc.of("p"), 0, (ModuleDesc[]) null); check(false, "of(null modules) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null modules) NPE"); }
        try { ModuleOpenInfo.of((PackageEntry) null, 0, List.of()); check(false, "of(null entry) must throw"); }
        catch (NullPointerException e) { check(true, "of(null entry) NPE"); }
        // 构造期不做标志位校验（保留原值），转换 Set 时按 MODULE_OPENS 位置精确报 IAE
        ModuleOpenInfo bad = ModuleOpenInfo.of(PackageDesc.of("p"), 0x0001, new ModuleDesc[0]);
        check(bad.opensFlagsMask() == 1, "bad mask kept at construction");
        try { bad.opensFlags(); check(false, "opensFlags bad bit must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unmatched bit position 0x1 for location MODULE_OPENS"),
                    "opensFlags IAE exact msg: " + e.getMessage());
        }
    }

    // ---- Part 5: buildModule 注入→re-parse 全链闭环 ----
    static void testBuildModuleRoundTrip() throws Exception {
        ModuleOpenInfo o1 = ModuleOpenInfo.of(PackageDesc.of("p.open"), 0x1000, ModuleDesc.of("m.to1"), ModuleDesc.of("m.to2"));
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.moi"), mb -> mb
                .opens(o1)
                .opens(PackageDesc.of("p.open2"), 0x8000)
                .opens(PackageDesc.of("p.open3"), Set.of(AccessFlag.MANDATED), ModuleDesc.of("m.x")));
        byte[] b = ClassFile.of().buildModule(ma);
        ClassModel mm = ClassFile.of().parse(b);
        check(mm.isModuleInfo(), "module-info product");
        List<ModuleOpenInfo> opens = mm.findAttribute(Attributes.module()).get().opens();
        check(opens.size() == 3, "opens count");
        check(opens.get(0).openedPackage().asSymbol().name().equals("p.open"), "open0 pkg");
        check(opens.get(0).opensFlagsMask() == 0x1000, "open0 mask");
        check(opens.get(0).opensTo().size() == 2
                && opens.get(0).opensTo().get(0).asSymbol().name().equals("m.to1")
                && opens.get(0).opensTo().get(1).asSymbol().name().equals("m.to2"), "open0 opensTo table");
        check(opens.get(0).has(AccessFlag.SYNTHETIC), "open0 has SYNTHETIC");
        check(opens.get(1).openedPackage().asSymbol().name().equals("p.open2"), "open1 pkg");
        check(opens.get(1).opensFlagsMask() == 0x8000 && opens.get(1).opensTo().isEmpty(), "open1 mask + empty opensTo");
        check(opens.get(2).opensTo().get(0).asSymbol().name().equals("m.x"), "open2 opensTo");
        check(ClassFile.of().verify(mm).isEmpty(), "module verify clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testNegatives();
        testBuildModuleRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
