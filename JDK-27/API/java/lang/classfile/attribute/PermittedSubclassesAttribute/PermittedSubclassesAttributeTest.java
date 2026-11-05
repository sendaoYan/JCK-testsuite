/*
 * @test
 * @summary java.lang.classfile.attribute.PermittedSubclassesAttribute conformance: white-box structure + black-box behavior.
 * @run main PermittedSubclassesAttributeTest
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

public class PermittedSubclassesAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.PermittedSubclassesAttribute");
        check(c != null, "java.lang.classfile.attribute.PermittedSubclassesAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.PermittedSubclassesAttribute"), "binary name java.lang.classfile.attribute.PermittedSubclassesAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.PermittedSubclassesAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.PermittedSubclassesAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.PermittedSubclassesAttribute has no superclass");
        check(declares(c, "permittedSubclasses"), "declares permittedSubclasses");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofSymbols"), "declares ofSymbols");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        PermittedSubclassesAttribute a = PermittedSubclassesAttribute.ofSymbols(ClassDesc.of("pkg.A"), ClassDesc.of("pkg.B"));
        check(a.permittedSubclasses().size() == 2, "size");
        check(a.permittedSubclasses().get(0).asInternalName().equals("pkg/A"), "first");

    }


    // ---- real-product members ----
    sealed interface PSI permits PSA, PSB {}
    static final class PSA implements PSI {}
    static final class PSB implements PSI {}
    interface PNI {} // 非 sealed 对照

    // ---- Part 3: javac 真产物（sealed permits 表）----
    static void testRealProduct() throws Exception {
        byte[] si = PermittedSubclassesAttributeTest.class.getResourceAsStream("PermittedSubclassesAttributeTest$PSI.class").readAllBytes();
        var at = ClassFile.of().parse(si).findAttribute(Attributes.permittedSubclasses()).get();
        List<String> names = new ArrayList<>();
        for (ClassEntry e : at.permittedSubclasses()) names.add(e.asInternalName());
        check(names.equals(List.of("PermittedSubclassesAttributeTest$PSA", "PermittedSubclassesAttributeTest$PSB")),
                "sealed permits order preserved: " + names);
        check(at.attributeMapper() == Attributes.permittedSubclasses(), "parsed mapper identity");
        byte[] pni = PermittedSubclassesAttributeTest.class.getResourceAsStream("PermittedSubclassesAttributeTest$PNI.class").readAllBytes();
        check(ClassFile.of().parse(pni).findAttribute(Attributes.permittedSubclasses()).isEmpty(), "non-sealed interface has none");
        byte[] sa = PermittedSubclassesAttributeTest.class.getResourceAsStream("PermittedSubclassesAttributeTest$PSA.class").readAllBytes();
        check(ClassFile.of().parse(sa).findAttribute(Attributes.permittedSubclasses()).isEmpty(), "subclass itself has none");
    }

    // ---- Part 4: 工厂 4 重载 + null 负例 + 属性面 ----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce1 = cpb.classEntry(ClassDesc.of("I$A"));
        ClassEntry ce2 = cpb.classEntry(ClassDesc.of("I$B"));
        PermittedSubclassesAttribute a1 = PermittedSubclassesAttribute.of(List.of(ce1, ce2));
        check(a1.permittedSubclasses().size() == 2 && a1.permittedSubclasses().get(0) == ce1, "of(List) entry identity");
        PermittedSubclassesAttribute a2 = PermittedSubclassesAttribute.of(ce1);
        check(a2.permittedSubclasses().get(0) == ce1, "of(varargs)");
        PermittedSubclassesAttribute a3 = PermittedSubclassesAttribute.ofSymbols(List.of(ClassDesc.of("I$C")));
        check(a3.permittedSubclasses().get(0).asInternalName().equals("I$C"), "ofSymbols(List)");
        PermittedSubclassesAttribute a4 = PermittedSubclassesAttribute.ofSymbols(ClassDesc.of("I$D"), ClassDesc.of("I$E"));
        check(a4.permittedSubclasses().size() == 2 && a4.permittedSubclasses().get(1).asInternalName().equals("I$E"), "ofSymbols(varargs)");
        check(PermittedSubclassesAttribute.ofSymbols().permittedSubclasses().isEmpty(), "ofSymbols(empty)");
        check(a1.attributeName().stringValue().equals("PermittedSubclasses")
                && a1.attributeName().stringValue().equals(Attributes.NAME_PERMITTED_SUBCLASSES), "NAME_PERMITTED_SUBCLASSES");
        check(a1.attributeMapper() == Attributes.permittedSubclasses(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { PermittedSubclassesAttribute.of((List<ClassEntry>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { PermittedSubclassesAttribute.ofSymbols((ClassDesc[]) null); check(false, "ofSymbols(null) must throw"); }
        catch (NullPointerException e) { check(true, "ofSymbols(null) NPE"); }
    }

    // ---- Part 5: 注入闭环（类元数据回读 + verify 干净）----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynPSA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(PermittedSubclassesAttribute.ofSymbols(ClassDesc.of("DynPSA$S1"), ClassDesc.of("DynPSA$S2"))));
        ClassModel m = ClassFile.of().parse(b);
        List<String> names = new ArrayList<>();
        for (ClassEntry e : m.findAttribute(Attributes.permittedSubclasses()).get().permittedSubclasses()) names.add(e.asInternalName());
        check(names.equals(List.of("DynPSA$S1", "DynPSA$S2")), "inject readback: " + names);
        check(m.findAttribute(Attributes.permittedSubclasses()).get().attributeMapper() == Attributes.permittedSubclasses(), "parsed mapper identity");
        check(ClassFile.of().verify(m).isEmpty(), "verify clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealProduct();
        testFactoryOverloads();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
