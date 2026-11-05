/*
 * @test
 * @summary java.lang.classfile.attribute.ModulePackagesAttribute conformance: white-box structure + black-box behavior.
 * @run main ModulePackagesAttributeTest
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

public class ModulePackagesAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModulePackagesAttribute");
        check(c != null, "java.lang.classfile.attribute.ModulePackagesAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModulePackagesAttribute"), "binary name java.lang.classfile.attribute.ModulePackagesAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModulePackagesAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModulePackagesAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModulePackagesAttribute has no superclass");
        check(declares(c, "packages"), "declares packages");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofNames"), "declares ofNames");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModulePackagesAttribute a = ModulePackagesAttribute.ofNames(PackageDesc.of("p"), PackageDesc.of("q"));
        check(a.packages().size() == 2, "size");
        check(a.packages().get(0).asSymbol().equals(PackageDesc.of("p")), "first package");

    }


    // ---- Part 3: 工厂全重载矩阵（of/ofNames × List/varargs，javap 锁定）----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        PackageEntry pe1 = cpb.packageEntry(PackageDesc.of("p.one"));
        PackageEntry pe2 = cpb.packageEntry(PackageDesc.of("p.two"));
        ModulePackagesAttribute a1 = ModulePackagesAttribute.of(pe1, pe2);
        check(a1.packages().size() == 2 && a1.packages().get(0) == pe1, "of(entry,varargs) identity");
        ModulePackagesAttribute a2 = ModulePackagesAttribute.of(List.of(pe1));
        check(a2.packages().get(0) == pe1, "of(List<PackageEntry>) identity");
        ModulePackagesAttribute a3 = ModulePackagesAttribute.ofNames(PackageDesc.of("q.a"), PackageDesc.of("q.b"));
        check(a3.packages().size() == 2, "ofNames(varargs)");
        check(a3.packages().get(0).asSymbol().name().equals("q.a"), "ofNames asSymbol dot form");
        check(a3.packages().get(0).name().stringValue().equals("q/a"), "package name Utf8 slash form");
        ModulePackagesAttribute a4 = ModulePackagesAttribute.ofNames(List.of(PackageDesc.of("q.c")));
        check(a4.packages().get(0).asSymbol().name().equals("q.c"), "ofNames(List)");
        ModulePackagesAttribute a5 = ModulePackagesAttribute.ofNames(new PackageDesc[0]);
        check(a5.packages().isEmpty(), "ofNames(empty)");
        check(a1.attributeName().stringValue().equals("ModulePackages")
                && a1.attributeName().stringValue().equals(Attributes.NAME_MODULE_PACKAGES), "NAME_MODULE_PACKAGES");
        check(a1.attributeMapper() == Attributes.modulePackages(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: null 负例 ----
    static void testNegatives() {
        try { ModulePackagesAttribute.of((List<PackageEntry>) null); check(false, "of(null list) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null list) NPE"); }
        try { ModulePackagesAttribute.ofNames((List<PackageDesc>) null); check(false, "ofNames(null) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "ofNames(null) NPE"); }
    }

    // ---- Part 5: 注入闭环（普通类 + module-info transform）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynMPA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(ModulePackagesAttribute.ofNames(PackageDesc.of("in.j"), PackageDesc.of("in.k"))));
        ClassModel m = ClassFile.of().parse(b);
        var at = m.findAttribute(Attributes.modulePackages()).get();
        check(at.packages().size() == 2 && at.packages().get(1).asSymbol().name().equals("in.k"), "plain-class inject readback");
        check(at.attributeMapper() == Attributes.modulePackages(), "parsed mapper identity");
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.pkgs"), mb -> mb.moduleVersion("1"));
        byte[] mb2 = ClassFile.of().transformClass(ClassFile.of().parse(ClassFile.of().buildModule(ma)),
                ClassTransform.endHandler(cb -> cb.with(ModulePackagesAttribute.ofNames(PackageDesc.of("x.y")))));
        ClassModel mm = ClassFile.of().parse(mb2);
        check(mm.isModuleInfo(), "still module-info after transform");
        check(mm.findAttribute(Attributes.modulePackages()).get().packages().get(0).asSymbol().name().equals("x.y"), "module-info inject readback");
        check(ClassFile.of().verify(mm).isEmpty(), "verify clean");
    }

    // ---- Part 6: jrt 真实 module-info 产物（javac 无通道，运行时镜像为自然产物源）----
    static void testRealModuleInfo() throws Exception {
        ClassModel jm = ClassFile.of().parse(Path.of(java.net.URI.create("jrt:/java.logging/module-info.class")));
        var pk = jm.findAttribute(Attributes.modulePackages()).get();
        List<String> names = new ArrayList<>();
        for (PackageEntry p : pk.packages()) names.add(p.asSymbol().name());
        check(names.equals(List.of("java.util.logging", "sun.net.www.protocol.http.logging",
                "sun.util.logging.internal", "sun.util.logging.resources")), "jrt java.logging packages exact: " + names);
        check(pk.packages().get(0).matches(PackageDesc.of("java.util.logging")), "PackageEntry.matches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testNegatives();
        testInjectRoundTrip();
        testRealModuleInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
