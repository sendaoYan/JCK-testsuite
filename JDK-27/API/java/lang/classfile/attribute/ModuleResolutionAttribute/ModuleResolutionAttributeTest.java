/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleResolutionAttribute conformance: white-box structure + black-box behavior.
 * @run main ModuleResolutionAttributeTest
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

public class ModuleResolutionAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleResolutionAttribute");
        check(c != null, "java.lang.classfile.attribute.ModuleResolutionAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleResolutionAttribute"), "binary name java.lang.classfile.attribute.ModuleResolutionAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleResolutionAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleResolutionAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleResolutionAttribute has no superclass");
        check(declares(c, "resolutionFlags"), "declares resolutionFlags");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleResolutionAttribute a = ModuleResolutionAttribute.of(0x0002);
        check(a.resolutionFlags() == 0x0002, "resolutionFlags");

    }


    // ---- Part 3: of(int) 全值域 + 属性面 ----
    static void testFactoryAndFlags() {
        check(ModuleResolutionAttribute.of(0).resolutionFlags() == 0, "of(0)");
        check(ModuleResolutionAttribute.of(1).resolutionFlags() == 1, "of(1) DO_NOT_RESOLVE_BY_DEFAULT (JVMS 4.7.26)");
        check(ModuleResolutionAttribute.of(2).resolutionFlags() == 2, "of(2) WARN_DEPRECATED (JVMS 4.7.26)");
        check(ModuleResolutionAttribute.of(3).resolutionFlags() == 3, "of(3) combined");
        check(ModuleResolutionAttribute.of(0xFFFF).resolutionFlags() == 0xFFFF, "of(0xFFFF) 位不校验保留");
        ModuleResolutionAttribute a = ModuleResolutionAttribute.of(1);
        check(a.attributeName().stringValue().equals("ModuleResolution")
                && a.attributeName().stringValue().equals(Attributes.NAME_MODULE_RESOLUTION), "NAME_MODULE_RESOLUTION");
        check(a.attributeMapper() == Attributes.moduleResolution(), "mapper identity");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.STATELESS, "stability STATELESS");
        check(!a.attributeMapper().allowMultiple(), "allowMultiple false");
        // 接口不导出任何标志常量（1/2 位不是 API 常量）——白盒负空间
        check(ModuleResolutionAttribute.class.getFields().length == 0, "no flag constants on interface");
    }

    // ---- Part 4: 注入闭环（普通类 + module-info transform）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynMRA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(ModuleResolutionAttribute.of(1)));
        ClassModel m = ClassFile.of().parse(b);
        check(m.findAttribute(Attributes.moduleResolution()).get().resolutionFlags() == 1, "plain-class inject readback");
        check(m.findAttribute(Attributes.moduleResolution()).get().attributeMapper() == Attributes.moduleResolution(), "parsed mapper identity");
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.res"), mb -> mb.moduleVersion("1"));
        byte[] mb2 = ClassFile.of().transformClass(ClassFile.of().parse(ClassFile.of().buildModule(ma)),
                ClassTransform.endHandler(cb -> cb.with(ModuleResolutionAttribute.of(2))));
        ClassModel mm = ClassFile.of().parse(mb2);
        check(mm.isModuleInfo(), "still module-info after transform");
        check(mm.findAttribute(Attributes.moduleResolution()).get().resolutionFlags() == 2, "module-info inject readback");
        check(ClassFile.of().verify(mm).isEmpty(), "verify clean");
    }

    // ---- Part 5: javac 不产；运行时镜像 java.logging 无该属性 ----
    static void testRealProducts() throws Exception {
        byte[] self = ModuleResolutionAttributeTest.class.getResourceAsStream("ModuleResolutionAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.moduleResolution()).isEmpty(), "javac never emits ModuleResolution");
        ClassModel jm = ClassFile.of().parse(Path.of(java.net.URI.create("jrt:/java.logging/module-info.class")));
        check(jm.findAttribute(Attributes.moduleResolution()).isEmpty(), "java.logging has no ModuleResolution");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryAndFlags();
        testInjectRoundTrip();
        testRealProducts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
