/*
 * @test
 * @summary java.lang.classfile.attribute.NestMembersAttribute conformance: white-box structure + black-box behavior.
 * @run main NestMembersAttributeTest
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

public class NestMembersAttributeTest {
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
    static class NMExtra {}
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.attribute.NestMembersAttribute");
        check(c != null, "java.lang.classfile.attribute.NestMembersAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.NestMembersAttribute"), "binary name java.lang.classfile.attribute.NestMembersAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.NestMembersAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.NestMembersAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.NestMembersAttribute has no superclass");
        check(declares(c, "nestMembers"), "declares nestMembers");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofSymbols"), "declares ofSymbols");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        NestMembersAttribute a = NestMembersAttribute.ofSymbols(ClassDesc.of("pkg.Outer$A"), ClassDesc.of("pkg.Outer$B"));
        check(a.nestMembers().size() == 2, "size");
        check(a.nestMembers().get(1).asInternalName().equals("pkg/Outer$B"), "member B");

    }


    // ---- Part 3: javac 真产物（精确 nest 成员集合）----
    static void testRealProduct() throws Exception {
        byte[] self = NestMembersAttributeTest.class.getResourceAsStream("NestMembersAttributeTest.class").readAllBytes();
        var nm = ClassFile.of().parse(self).findAttribute(Attributes.nestMembers()).get();
        Set<String> names = new HashSet<>();
        for (ClassEntry e : nm.nestMembers()) names.add(e.asInternalName());
        check(names.size() == nm.nestMembers().size(), "no duplicate nest members");
        check(names.equals(new HashSet<>(List.of("NestMembersAttributeTest$CA", "NestMembersAttributeTest$CA2",
                "NestMembersAttributeTest$CA3", "NestMembersAttributeTest$NMExtra"))), "exact nest member set: " + names);
        byte[] inner = NestMembersAttributeTest.class.getResourceAsStream("NestMembersAttributeTest$NMExtra.class").readAllBytes();
        check(ClassFile.of().parse(inner).findAttribute(Attributes.nestMembers()).isEmpty(), "member class itself has no NestMembers");
        check(ClassFile.of().parse(inner).findAttribute(Attributes.nestHost()).isPresent(), "member class has NestHost");
        check(nm.attributeMapper() == Attributes.nestMembers(), "parsed mapper identity");
    }

    // ---- Part 4: 工厂 4 重载 + null 负例 + 属性面 ----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce1 = cpb.classEntry(ClassDesc.of("N$A"));
        ClassEntry ce2 = cpb.classEntry(ClassDesc.of("N$B"));
        NestMembersAttribute a1 = NestMembersAttribute.of(List.of(ce1, ce2));
        check(a1.nestMembers().size() == 2 && a1.nestMembers().get(0) == ce1, "of(List) entry identity");
        NestMembersAttribute a2 = NestMembersAttribute.of(ce1);
        check(a2.nestMembers().get(0) == ce1, "of(varargs)");
        NestMembersAttribute a3 = NestMembersAttribute.ofSymbols(List.of(ClassDesc.of("N$C")));
        check(a3.nestMembers().get(0).asInternalName().equals("N$C"), "ofSymbols(List)");
        NestMembersAttribute a4 = NestMembersAttribute.ofSymbols(ClassDesc.of("N$D"), ClassDesc.of("N$E"));
        check(a4.nestMembers().size() == 2 && a4.nestMembers().get(1).asInternalName().equals("N$E"), "ofSymbols(varargs)");
        check(NestMembersAttribute.ofSymbols().nestMembers().isEmpty(), "ofSymbols(empty)");
        check(a1.attributeName().stringValue().equals("NestMembers")
                && a1.attributeName().stringValue().equals(Attributes.NAME_NEST_MEMBERS), "NAME_NEST_MEMBERS");
        check(a1.attributeMapper() == Attributes.nestMembers(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { NestMembersAttribute.of((List<ClassEntry>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { NestMembersAttribute.ofSymbols((List<ClassDesc>) null); check(false, "ofSymbols(null) must throw"); }
        catch (NullPointerException e) { check(true, "ofSymbols(null) NPE"); }
        try { NestMembersAttribute.ofSymbols((ClassDesc[]) null); check(false, "ofSymbols(null varargs) must throw"); }
        catch (NullPointerException e) { check(true, "ofSymbols(null varargs) NPE"); }
    }

    // ---- Part 5: 注入闭环 ----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynNMA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(NestMembersAttribute.ofSymbols(ClassDesc.of("DynNMA$X"), ClassDesc.of("DynNMA$Y"))));
        ClassModel m = ClassFile.of().parse(b);
        List<String> names = new ArrayList<>();
        for (ClassEntry e : m.findAttribute(Attributes.nestMembers()).get().nestMembers()) names.add(e.asInternalName());
        check(names.equals(List.of("DynNMA$X", "DynNMA$Y")), "inject readback order preserved: " + names);
        check(m.findAttribute(Attributes.nestMembers()).get().attributeMapper() == Attributes.nestMembers(), "parsed mapper identity");
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
