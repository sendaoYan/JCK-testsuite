/*
 * @test
 * @summary java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo conformance: white-box structure + black-box behavior.
 * @run main ClassHierarchyInfoTest
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

public class ClassHierarchyInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassHierarchyResolver$ClassHierarchyInfo");
        check(c != null, "java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo loads");
        check(c.getName().equals("java.lang.classfile.ClassHierarchyResolver$ClassHierarchyInfo"), "binary name java.lang.classfile.ClassHierarchyResolver$ClassHierarchyInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo is public");
        check(c.isInterface(), "java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo has no superclass");
        check(declares(c, "ofClass"), "declares ofClass");
        check(declares(c, "ofInterface"), "declares ofInterface");
        // JDK-27 形态：sealed 接口（impl 在 jdk.internal），公开面仅两个静态工厂——无实例访问器
        check(c.isSealed(), "ClassHierarchyInfo is sealed");
        check(c.getPermittedSubclasses() != null && c.getPermittedSubclasses().length >= 1,
                "permits at least the internal impl");
        boolean allStatic = true;
        for (Method m : c.getDeclaredMethods())
            if (!Modifier.isStatic(m.getModifiers())) allStatic = false;
        check(allStatic, "no public instance accessors (all declared methods static)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassHierarchyResolver.ClassHierarchyInfo ci = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Object"));
        check(ci != null, "ofClass non-null");
        ClassHierarchyResolver.ClassHierarchyInfo ii = ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
        check(ii != null, "ofInterface non-null");

    }

    // ---- Part 3: 值语义（equals/hashCode 实测锁定：非单例，按内容相等） ----
    static void testValueSemantics() {
        ClassHierarchyResolver.ClassHierarchyInfo o1 = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Object"));
        ClassHierarchyResolver.ClassHierarchyInfo o2 = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Object"));
        ClassHierarchyResolver.ClassHierarchyInfo i1 = ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
        ClassHierarchyResolver.ClassHierarchyInfo i2 = ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
        // 同参两次构造：不同实例但值相等（hashCode 一致）
        check(o1 != o2, "ofClass same args different instances");
        check(o1.equals(o2), "ofClass same args value-equal");
        check(o1.hashCode() == o2.hashCode(), "ofClass same args equal hashCode");
        check(i1 != i2, "ofInterface different instances");
        check(i1.equals(i2), "ofInterface value-equal");
        check(i1.hashCode() == i2.hashCode(), "ofInterface equal hashCode");
        // 类信息 vs 接口信息不相等；不同父类不相等；equals(null) false；equals 自反/对称
        check(!o1.equals(i1) && !i1.equals(o1), "ofClass vs ofInterface not equal");
        ClassHierarchyResolver.ClassHierarchyInfo n = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Number"));
        check(!o1.equals(n) && !n.equals(o1), "different superclass not equal");
        check(!o1.equals(null), "equals(null) false");
        check(o1.equals(o1), "equals reflexive");
        check(n.equals(ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Number"))), "Number equal symmetric");
        // toString 呈现父类描述符与 isInterface 标记（实测格式 ClassHierarchyInfoImpl[superClass=..., isInterface=...]）
        check(o1.toString().contains("Object"), "ofClass(Object) toString mentions Object");
        check(n.toString().contains("Number"), "ofClass(Number) toString mentions Number");
        check(!n.toString().contains("isInterface=true"), "ofClass toString isInterface=false");
        check(i1.toString().contains("isInterface=true"), "ofInterface toString isInterface=true");
        check(i1.toString().contains("Object"), "ofInterface superclass Object in toString");
        // ofClass(null) 不抛 NPE：superClass=null 的 info（实测锁定，null 表示「无父类/任意」占位）
        ClassHierarchyResolver.ClassHierarchyInfo nu = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(null);
        check(nu != null, "ofClass(null) tolerated (no NPE)");
        check(nu.toString().contains("superClass=null"), "ofClass(null) superClass=null");
        check(!nu.equals(i1), "ofClass(null) distinct from ofInterface");
    }

    // ---- Part 4: 在 resolver 体系中的真实回读（of/cached/defaultResolver 出来的 info 类型一致） ----
    static void testResolverIntegration() {
        // 自造层级中回读的 info：类型 instanceof + 值等于工厂产物
        java.util.Map<ClassDesc, ClassDesc> sup = new java.util.HashMap<>();
        sup.put(ClassDesc.of("java.lang.Integer"), ClassDesc.of("java.lang.Number"));
        ClassHierarchyResolver r = ClassHierarchyResolver.of(java.util.List.of(), sup);
        ClassHierarchyResolver.ClassHierarchyInfo fromResolver = r.getClassInfo(ClassDesc.of("java.lang.Integer"));
        check(fromResolver != null, "resolver info non-null");
        check(fromResolver instanceof ClassHierarchyResolver.ClassHierarchyInfo, "resolver info instanceof ClassHierarchyInfo");
        check(fromResolver.equals(ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Number"))),
                "resolver info value-equals ofClass(Number)");
        // defaultResolver 产物同型
        ClassHierarchyResolver.ClassHierarchyInfo si = ClassHierarchyResolver.defaultResolver()
                .getClassInfo(ClassDesc.of("java.lang.String"));
        check(si != null && si.toString().contains("Object"), "defaultResolver info superclass Object");
        // cached(Supplier) 透传同一实例（identity 保持，实测）
        ClassHierarchyResolver.ClassHierarchyInfo mine = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("a.C"));
        ClassHierarchyResolver cs = r.cached(() -> {
            java.util.Map<ClassDesc, ClassHierarchyResolver.ClassHierarchyInfo> m = new java.util.HashMap<>();
            m.put(ClassDesc.of("a.B"), mine);
            return m;
        });
        check(cs.getClassInfo(ClassDesc.of("a.B")) == mine, "cached(supplier) passes info instance through (identity)");
        check(cs.getClassInfo(ClassDesc.of("no.such.K")) == null, "cached(supplier) unknown null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValueSemantics();
        testResolverIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
