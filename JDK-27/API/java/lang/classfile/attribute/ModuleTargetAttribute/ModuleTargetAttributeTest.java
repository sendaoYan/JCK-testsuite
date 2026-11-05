/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleTargetAttribute conformance: white-box structure + black-box behavior.
 * @run main ModuleTargetAttributeTest
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

public class ModuleTargetAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleTargetAttribute");
        check(c != null, "java.lang.classfile.attribute.ModuleTargetAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleTargetAttribute"), "binary name java.lang.classfile.attribute.ModuleTargetAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleTargetAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleTargetAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleTargetAttribute has no superclass");
        check(declares(c, "targetPlatform"), "declares targetPlatform");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleTargetAttribute a = ModuleTargetAttribute.of("11");
        check(a.targetPlatform().stringValue().equals("11"), "targetPlatform");

    }


    // ---- Part 3: of(String)/of(Utf8Entry) 全重载 + 属性面 ----
    static void testFactoryOverloads() {
        ModuleTargetAttribute a1 = ModuleTargetAttribute.of("linux-amd64");
        check(a1.targetPlatform().stringValue().equals("linux-amd64"), "of(String)");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("macos-aarch64");
        ModuleTargetAttribute a2 = ModuleTargetAttribute.of(u);
        check(a2.targetPlatform() == u, "of(Utf8Entry) entry identity");
        check(a1.attributeName().stringValue().equals("ModuleTarget")
                && a1.attributeName().stringValue().equals(Attributes.NAME_MODULE_TARGET), "NAME_MODULE_TARGET");
        check(a1.attributeMapper() == Attributes.moduleTarget(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { ModuleTargetAttribute.of((String) null); check(false, "of(null String) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null String) NPE"); }
        try { ModuleTargetAttribute.of((Utf8Entry) null); check(false, "of(null Utf8) must throw"); }
        catch (NullPointerException e) { check(true, "of(null Utf8) NPE"); }
    }

    // ---- Part 4: 注入闭环（普通类 + module-info transform）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynMTA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(ModuleTargetAttribute.of("win-x64")));
        ClassModel m = ClassFile.of().parse(b);
        check(m.findAttribute(Attributes.moduleTarget()).get().targetPlatform().stringValue().equals("win-x64"), "plain-class inject readback");
        check(m.findAttribute(Attributes.moduleTarget()).get().attributeMapper() == Attributes.moduleTarget(), "parsed mapper identity");
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.tgt"), mb -> mb.moduleVersion("1"));
        byte[] mb2 = ClassFile.of().transformClass(ClassFile.of().parse(ClassFile.of().buildModule(ma)),
                ClassTransform.endHandler(cb -> cb.with(ModuleTargetAttribute.of("linux-amd64"))));
        ClassModel mm = ClassFile.of().parse(mb2);
        check(mm.isModuleInfo(), "still module-info after transform");
        check(mm.findAttribute(Attributes.moduleTarget()).get().targetPlatform().stringValue().equals("linux-amd64"), "module-info inject readback");
        check(ClassFile.of().verify(mm).isEmpty(), "verify clean");
    }

    // ---- Part 5: jrt 真实产物 + javac 不产 ----
    static void testRealProducts() throws Exception {
        ClassModel jm = ClassFile.of().parse(Path.of(java.net.URI.create("jrt:/java.logging/module-info.class")));
        check(jm.findAttribute(Attributes.moduleTarget()).get().targetPlatform().stringValue().equals("linux-amd64"),
                "java.logging ModuleTarget linux-amd64");
        byte[] self = ModuleTargetAttributeTest.class.getResourceAsStream("ModuleTargetAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.moduleTarget()).isEmpty(), "javac never emits ModuleTarget");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testInjectRoundTrip();
        testRealProducts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
