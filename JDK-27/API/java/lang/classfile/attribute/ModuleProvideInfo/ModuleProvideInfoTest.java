/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleProvideInfo conformance: white-box structure + black-box behavior.
 * @run main ModuleProvideInfoTest
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

public class ModuleProvideInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleProvideInfo");
        check(c != null, "java.lang.classfile.attribute.ModuleProvideInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleProvideInfo"), "binary name java.lang.classfile.attribute.ModuleProvideInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleProvideInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleProvideInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleProvideInfo has no superclass");
        check(declares(c, "provides"), "declares provides");
        check(declares(c, "providesWith"), "declares providesWith");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleProvideInfo i = ModuleProvideInfo.of(ClassDesc.of("p.Service"), ClassDesc.of("p.impl.A"), ClassDesc.of("p.impl.B"));
        check(i.provides().asSymbol().equals(ClassDesc.of("p.Service")), "provides");
        check(i.providesWith().size() == 2, "providesWith size");
        check(i.providesWith().get(0).asSymbol().equals(ClassDesc.of("p.impl.A")), "first impl");

    }


    // ---- Part 3: 工厂全重载矩阵（4 个 of，javap 锁定）----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry svc = cpb.classEntry(ClassDesc.of("java.lang.Runnable"));
        ClassEntry p1 = cpb.classEntry(ClassDesc.of("p.Impl1"));
        ClassEntry p2 = cpb.classEntry(ClassDesc.of("p.Impl2"));
        ModuleProvideInfo i1 = ModuleProvideInfo.of(svc, List.of(p1, p2));
        check(i1.provides() == svc && i1.providesWith().get(0) == p1 && i1.providesWith().get(1) == p2, "of(entry,List) identity");
        ModuleProvideInfo i2 = ModuleProvideInfo.of(svc, p1);
        check(i2.providesWith().size() == 1 && i2.providesWith().get(0) == p1, "of(entry,varargs)");
        ModuleProvideInfo i3 = ModuleProvideInfo.of(ClassDesc.of("java.lang.Comparable"), List.of(ClassDesc.of("p.A")));
        check(i3.provides().asInternalName().equals("java/lang/Comparable"), "of(desc,List) service");
        check(i3.providesWith().get(0).asInternalName().equals("p/A"), "of(desc,List) provider");
        ModuleProvideInfo i4 = ModuleProvideInfo.of(ClassDesc.of("java.util.List"), ClassDesc.of("p.B"), ClassDesc.of("p.C"));
        check(i4.providesWith().size() == 2 && i4.providesWith().get(1).asInternalName().equals("p/C"), "of(desc,varargs)");
        check(i4.provides().asSymbol().descriptorString().equals("Ljava/util/List;"), "service asSymbol");
        check(i3.provides().asSymbol().descriptorString().equals("Ljava/lang/Comparable;"), "of(entry) service asSymbol");
    }

    // ---- Part 4: null 负例 ----
    static void testNegatives() {
        try { ModuleProvideInfo.of((ClassDesc) null, new ClassDesc[0]); check(false, "of(null svc) must throw"); }
        catch (NullPointerException e) { check(true, "of(null svc) NPE"); }
        try { ModuleProvideInfo.of(ClassDesc.of("S"), (List<ClassDesc>) null); check(false, "of(null list) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null list) NPE"); }
    }

    // ---- Part 5: buildModule 注入→re-parse 全链闭环 ----
    static void testBuildModuleRoundTrip() throws Exception {
        ModuleProvideInfo pi = ModuleProvideInfo.of(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("p.ImplA"), ClassDesc.of("p.ImplB"));
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.mpi"), mb -> mb
                .provides(pi)
                .provides(ClassDesc.of("java.lang.Comparable"), ClassDesc.of("p.Cmp")));
        ClassModel mm = ClassFile.of().parse(ClassFile.of().buildModule(ma));
        List<ModuleProvideInfo> ps = mm.findAttribute(Attributes.module()).get().provides();
        check(ps.size() == 2, "provides count");
        check(ps.get(0).provides().asInternalName().equals("java/lang/Runnable"), "p0 service");
        check(ps.get(0).providesWith().size() == 2 && ps.get(0).providesWith().get(0).asInternalName().equals("p/ImplA"), "p0 providers");
        check(ps.get(1).provides().asInternalName().equals("java/lang/Comparable"), "p1 service");
        check(ps.get(1).providesWith().get(0).asInternalName().equals("p/Cmp"), "p1 provider");
        check(ClassFile.of().verify(mm).isEmpty(), "verify clean");
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
