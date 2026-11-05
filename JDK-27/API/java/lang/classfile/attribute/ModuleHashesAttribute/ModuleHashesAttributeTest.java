/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleHashesAttribute conformance: white-box structure + black-box behavior.
 * @run main ModuleHashesAttributeTest
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

public class ModuleHashesAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleHashesAttribute");
        check(c != null, "java.lang.classfile.attribute.ModuleHashesAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleHashesAttribute"), "binary name java.lang.classfile.attribute.ModuleHashesAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleHashesAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleHashesAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleHashesAttribute has no superclass");
        check(declares(c, "algorithm"), "declares algorithm");
        check(declares(c, "hashes"), "declares hashes");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleHashInfo h = ModuleHashInfo.of(ModuleDesc.of("java.base"), new byte[]{1,2,3});
        ModuleHashesAttribute a = ModuleHashesAttribute.of("SHA-256", h);
        check(a.algorithm().stringValue().equals("SHA-256"), "algorithm");
        check(a.hashes().size() == 1, "hashes size");
        check(a.hashes().get(0).moduleName().asSymbol().equals(ModuleDesc.of("java.base")), "moduleName");

    }

    // ---- Part 3: 四工厂全重载 + null/u2 负例 + 元数据 ----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ModuleHashInfo h1 = ModuleHashInfo.of(ModuleDesc.of("m.a"), new byte[]{1, 2, 3});
        ModuleHashInfo h2 = ModuleHashInfo.of(ModuleDesc.of("m.b"), new byte[0]);
        // String + List
        ModuleHashesAttribute a1 = ModuleHashesAttribute.of("SHA-256", List.of(h1, h2));
        check(a1.algorithm().stringValue().equals("SHA-256"), "of(String,List) algo");
        check(a1.hashes().size() == 2 && a1.hashes().get(0) == h1, "of(String,List) identity");
        // String + varargs
        ModuleHashesAttribute a2 = ModuleHashesAttribute.of("MD5", h1, h2);
        check(a2.hashes().stream().map(h -> h.moduleName().asSymbol().name()).toList()
                .equals(List.of("m.a", "m.b")), "of(String,varargs) names");
        // Utf8Entry + List / varargs
        Utf8Entry algo = cp.utf8Entry("SHA-1");
        ModuleHashesAttribute a3 = ModuleHashesAttribute.of(algo, List.of(h1));
        check(a3.algorithm() == algo, "of(Utf8Entry,List) algo identity");
        ModuleHashesAttribute a4 = ModuleHashesAttribute.of(algo, h1, h2);
        check(a4.algorithm().stringValue().equals("SHA-1") && a4.hashes().size() == 2, "of(Utf8Entry,varargs)");
        check(ModuleHashesAttribute.of("X", List.of()).hashes().isEmpty(), "empty list ok");
        // null 负例
        try { ModuleHashesAttribute.of((String) null, List.of(h1)); check(false, "of((String)null,..) must NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "algo null NPE"); }
        try { ModuleHashesAttribute.of("X", (List<ModuleHashInfo>) null); check(false, "of(..,(List)null) must NPE"); }
        catch (NullPointerException e) { check(true, "list null NPE"); }
        try { ModuleHashesAttribute.of("X", (ModuleHashInfo[]) null); check(false, "of(..,(varargs)null) must NPE"); }
        catch (NullPointerException e) { check(true, "varargs null NPE"); }
        // u2 边界
        check(ModuleHashesAttribute.of("X", Collections.nCopies(65535, h1)).hashes().size() == 65535, "65535 ok");
        try {
            ModuleHashesAttribute.of("X", Collections.nCopies(66000, h1));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "66000 IAE msg");
        }
        // 元数据
        check(a1.attributeName().stringValue().equals(Attributes.NAME_MODULE_HASHES), "attributeName==NAME_MODULE_HASHES");
        check(a1.attributeName().stringValue().equals("ModuleHashes"), "attributeName literal");
        check(a1.attributeMapper() == Attributes.moduleHashes(), "attributeMapper==Attributes.moduleHashes()");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: buildModule 注入 → re-parse 回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.h1"), mb -> mb.moduleVersion("1.0"));
        ModuleHashesAttribute ha = ModuleHashesAttribute.of("SHA-256", List.of(
                ModuleHashInfo.of(ModuleDesc.of("java.base"), new byte[]{1, 2, 3}),
                ModuleHashInfo.of(ModuleDesc.of("java.logging"), new byte[]{4, 5})));
        byte[] b = cf.buildModule(ma, cb -> cb.with(ha));
        ClassModel cm = cf.parse(b);
        var found = cm.findAttribute(Attributes.moduleHashes());
        check(found.isPresent(), "injected ModuleHashes present");
        check(found.get().algorithm().stringValue().equals("SHA-256"), "roundtrip algorithm");
        List<ModuleHashInfo> hs = found.get().hashes();
        check(hs.size() == 2, "2 hashes");
        check(hs.get(0).moduleName().asSymbol().name().equals("java.base"), "hash0 module");
        check(hs.get(0).hash().length == 3 && hs.get(0).hash()[0] == 1 && hs.get(0).hash()[2] == 3, "hash0 bytes");
        check(hs.get(1).hash().length == 2 && hs.get(1).hash()[0] == 4, "hash1 bytes");
        check(found.get().attributeName().stringValue().equals("ModuleHashes"), "bound attributeName");
        boolean inElements = false;
        for (ClassElement e : cm.elementList()) if (e instanceof ModuleHashesAttribute) inElements = true;
        check(inElements, "visible as ClassElement");
        check(cf.verify(b).isEmpty(), "module with hashes verifies");
    }

    // ---- Part 5: 普通 class 注入（module 属性之外的场景容忍）----
    static void testPlainClassInjection() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("MHT1"), cb -> cb
                .with(ModuleHashesAttribute.of("MD5", ModuleHashInfo.of(ModuleDesc.of("m.x"), new byte[]{9}))));
        var found = cf.parse(b).findAttribute(Attributes.moduleHashes());
        check(found.isPresent(), "plain class tolerates ModuleHashes");
        check(found.get().algorithm().stringValue().equals("MD5"), "plain class algo");
        check(found.get().hashes().get(0).hash()[0] == 9, "plain class hash byte");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testBuildRoundtrip();
        testPlainClassInjection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
