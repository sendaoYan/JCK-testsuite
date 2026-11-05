/*
 * @test
 * @summary java.lang.classfile.ClassHierarchyResolver conformance: white-box structure + black-box behavior.
 * @run main ClassHierarchyResolverTest
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

public class ClassHierarchyResolverTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassHierarchyResolver");
        check(c != null, "java.lang.classfile.ClassHierarchyResolver loads");
        check(c.getName().equals("java.lang.classfile.ClassHierarchyResolver"), "binary name java.lang.classfile.ClassHierarchyResolver");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassHierarchyResolver is public");
        check(c.isInterface(), "java.lang.classfile.ClassHierarchyResolver is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassHierarchyResolver has no superclass");
        check(declares(c, "defaultResolver"), "declares defaultResolver");
        check(declares(c, "getClassInfo"), "declares getClassInfo");
        check(declares(c, "orElse"), "declares orElse");
        // javap 锁定的全部静态工厂与默认方法
        check(declares(c, "ofResourceParsing"), "declares ofResourceParsing");
        check(declares(c, "of"), "declares of(Collection,Map)");
        check(declares(c, "ofClassLoading"), "declares ofClassLoading");
        check(declares(c, "cached"), "declares cached");
        // JDK-27 契约：getClassInfo 返回裸 ClassHierarchyInfo（可空），非 Optional（javap/反射实测锁定）
        try {
            java.lang.reflect.Method m = c.getMethod("getClassInfo", java.lang.constant.ClassDesc.class);
            check(m.getReturnType() == ClassHierarchyResolver.ClassHierarchyInfo.class,
                    "getClassInfo returns raw ClassHierarchyInfo (not Optional)");
        } catch (Exception e) {
            check(false, "reflect getClassInfo: " + e);
        }
        // @FunctionalInterface：可用 lambda 定制
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassHierarchyResolver r = ClassHierarchyResolver.ofClassLoading(ClassLoader.getSystemClassLoader());
        ClassHierarchyResolver.ClassHierarchyInfo i = r.getClassInfo(ClassDesc.of("java.lang.String"));
        check(i != null, "getClassInfo String");
        ClassHierarchyResolver.ClassHierarchyInfo o = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Object"));
        ClassHierarchyResolver.ClassHierarchyInfo itf = ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
        check(r.orElse(ClassHierarchyResolver.ofClassLoading(ClassLoader.getSystemClassLoader())) != null, "orElse");
        check(r.cached() != null, "cached()");

    }

    // ---- Part 3: defaultResolver 本体语义（实测锁定） ----
    static void testDefaultResolver() {
        ClassHierarchyResolver dr = ClassHierarchyResolver.defaultResolver();
        check(dr != null, "defaultResolver non-null");
        check(ClassHierarchyResolver.defaultResolver() == dr, "defaultResolver same instance (cached)");
        // 已知类 → 非空 info；Object 自身 superclass=null；接口 isInterface=true
        ClassHierarchyResolver.ClassHierarchyInfo si = dr.getClassInfo(ClassDesc.of("java.lang.String"));
        check(si != null, "default String info non-null");
        check(si instanceof ClassHierarchyResolver.ClassHierarchyInfo, "info type");
        check(si.toString().contains("Object"), "String superclass Object (toString)");
        check(dr.getClassInfo(ClassDesc.of("java.lang.Object")) != null, "Object info non-null");
        check(dr.getClassInfo(ClassDesc.of("java.lang.Object")).toString().contains("superClass=null"),
                "Object itself has null superclass");
        ClassHierarchyResolver.ClassHierarchyInfo ri = dr.getClassInfo(ClassDesc.of("java.lang.Runnable"));
        check(ri != null && ri.toString().contains("isInterface=true"), "Runnable isInterface=true");
        // 未知类 → null（裸返回而非 Optional.empty，JDK-27 契约）
        check(dr.getClassInfo(ClassDesc.of("no.such.Clazz") ) == null, "unknown class -> null");
        // 数组描述符非 class-or-interface → null（实测锁定：getClassInfo 只认类/接口 ClassDesc）
        check(dr.getClassInfo(ClassDesc.ofDescriptor("[Ljava/lang/String;")) == null, "array desc -> null");
        check(dr.getClassInfo(ClassDesc.ofDescriptor("[I")) == null, "primitive array desc -> null");
        // getClassInfo(null) → NPE
        try {
            dr.getClassInfo(null);
            check(false, "getClassInfo(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"cd\" is null"),
                    "getClassInfo(null) NPE message: " + e.getMessage());
        }
    }

    // ---- Part 4: of(interfaces, superclassMap) 静态层级（实测锁定） ----
    static void testStaticHierarchy() {
        Map<ClassDesc, ClassDesc> sup = new java.util.HashMap<>();
        sup.put(ClassDesc.of("java.lang.Integer"), ClassDesc.of("java.lang.Number"));
        sup.put(ClassDesc.of("java.lang.Number"), ClassDesc.of("java.lang.Object"));
        sup.put(ClassDesc.of("java.lang.String"), ClassDesc.of("java.lang.Object"));
        ClassHierarchyResolver r = ClassHierarchyResolver.of(List.of(), sup);
        check(r != null, "of(interfaces, map) non-null");
        check(r.getClassInfo(ClassDesc.of("java.lang.Integer")).toString().contains("Number"), "Integer -> Number");
        check(r.getClassInfo(ClassDesc.of("java.lang.Number")).toString().contains("Object"), "Number -> Object");
        check(r.getClassInfo(ClassDesc.of("java.lang.String")).toString().contains("superClass=ClassOrInterfaceDesc[Object]"),
                "String -> Object");
        // 不在 map 中的未知类 → null；Object 不在 map 也已知（superClass=null）
        check(r.getClassInfo(ClassDesc.of("no.such.X")) == null, "static-of unknown -> null");
        check(r.getClassInfo(ClassDesc.of("java.lang.Object")) != null, "Object known without map entry");
        // 接口集合成员：isInterface=true、superclass=Object
        ClassHierarchyResolver r2 = ClassHierarchyResolver.of(List.of(ClassDesc.of("pkg.SomeInterface")), sup);
        ClassHierarchyResolver.ClassHierarchyInfo ii = r2.getClassInfo(ClassDesc.of("pkg.SomeInterface"));
        check(ii != null && ii.toString().contains("isInterface=true"), "declared interface info");
        check(ii.toString().contains("Object"), "interface superclass Object");
        // 同一 ClassDesc 不同实例（binary name 相同）解析一致
        check(r.getClassInfo(ClassDesc.of("java.lang.String"))
                .equals(r.getClassInfo(ClassDesc.of("java.lang.String"))), "value-based info equality across calls");
    }

    // ---- Part 5: orElse / cached 组合语义（实测锁定） ----
    static void testComposition() {
        Map<ClassDesc, ClassDesc> sup = new java.util.HashMap<>();
        sup.put(ClassDesc.of("java.lang.Integer"), ClassDesc.of("java.lang.Number"));
        ClassHierarchyResolver primary = ClassHierarchyResolver.of(List.of(), sup);
        ClassHierarchyResolver fallback = ClassHierarchyResolver.defaultResolver();
        ClassHierarchyResolver or = primary.orElse(fallback);
        check(or != null, "orElse non-null");
        // 主命中：Integer（primary 知道）
        check(or.getClassInfo(ClassDesc.of("java.lang.Integer")).toString().contains("Number"), "orElse primary hit");
        // 主缺失、备命中：java.lang.Class（default resolver 可解析）
        check(or.getClassInfo(ClassDesc.of("java.lang.Class")) != null, "orElse fallback hit");
        // 两边皆无 → null
        check(or.getClassInfo(ClassDesc.of("no.such.Q")) == null, "orElse both miss -> null");
        // orElse(null) → NPE
        try {
            primary.orElse(null);
            check(false, "orElse(null) must throw");
        } catch (NullPointerException e) {
            check(true, "orElse(null) NPE");
        }
        // cached()：包装新实例（两次 !=），语义透传
        ClassHierarchyResolver c1 = primary.cached();
        ClassHierarchyResolver c2 = primary.cached();
        check(c1 != primary && c2 != primary, "cached() wraps new instance");
        check(c1 != c2, "cached() new instance per call");
        check(c1.getClassInfo(ClassDesc.of("java.lang.Integer")).toString().contains("Number"), "cached() passthrough hit");
        check(c1.getClassInfo(ClassDesc.of("no.such.W")) == null, "cached() passthrough miss");
        // cached(Supplier)：懒加载且只调用一次（计数器实测）；supplier 必须给可变 Map（缓存会写入）
        int[] calls = {0};
        Map<ClassDesc, ClassHierarchyResolver.ClassHierarchyInfo> seed = new java.util.HashMap<>();
        seed.put(ClassDesc.of("a.B"), ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("a.C")));
        ClassHierarchyResolver cs = primary.cached(() -> { calls[0]++; return new java.util.HashMap<>(seed); });
        ClassHierarchyResolver.ClassHierarchyInfo got = cs.getClassInfo(ClassDesc.of("a.B"));
        check(got != null && got.toString().contains("ClassOrInterfaceDesc[C]"), "cached(supplier) hit");
        check(calls[0] == 1, "supplier lazy (0 before first lookup)");
        cs.getClassInfo(ClassDesc.of("a.B"));
        check(calls[0] == 1, "supplier invoked exactly once across 2 lookups");
        check(cs.getClassInfo(ClassDesc.of("no.such.V")) == null, "cached(supplier) miss -> null");
        // 不可变 Map 作为缓存容器 → UnsupportedOperationException（契约：须可变）
        ClassHierarchyResolver bad = primary.cached(() -> java.util.Map.of());
        try {
            bad.getClassInfo(ClassDesc.of("a.D"));
            check(false, "immutable supplier map must fail");
        } catch (UnsupportedOperationException e) {
            check(true, "cached(supplier) requires mutable map (UOE)");
        }
    }

    // ---- Part 6: 其余静态工厂 + lambda 定制 ----
    static void testOtherFactories() {
        // ofClassLoading(ClassLoader)
        ClassHierarchyResolver cl = ClassHierarchyResolver.ofClassLoading(ClassLoader.getSystemClassLoader());
        check(cl.getClassInfo(ClassDesc.of("java.lang.String")) != null, "ofClassLoading(CL) String");
        check(cl.getClassInfo(ClassDesc.of("no.such.Y")) == null, "ofClassLoading(CL) unknown null");
        // ofClassLoading(Lookup)
        ClassHierarchyResolver lk = ClassHierarchyResolver.ofClassLoading(MethodHandles.lookup());
        check(lk.getClassInfo(ClassDesc.of("java.lang.String")) != null, "ofClassLoading(Lookup) String");
        check(lk.getClassInfo(ClassDesc.of(ClassHierarchyResolverTest.class.getName())) != null,
                "ofClassLoading(Lookup) resolves own test class");
        // ofResourceParsing(ClassLoader)
        ClassHierarchyResolver rp = ClassHierarchyResolver.ofResourceParsing(ClassLoader.getSystemClassLoader());
        check(rp.getClassInfo(ClassDesc.of("java.lang.String")) != null, "ofResourceParsing(CL) String");
        check(rp.getClassInfo(ClassDesc.of("no.such.Z")) == null, "ofResourceParsing(CL) unknown null");
        // FunctionalInterface：lambda 直达 getClassInfo
        ClassHierarchyResolver lambda = cd -> ClassDesc.of("java.lang.Number").equals(cd)
                ? ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.Object"))
                : null;
        check(lambda.getClassInfo(ClassDesc.of("java.lang.Number")) != null, "lambda resolver hit");
        check(lambda.getClassInfo(ClassDesc.of("java.lang.String")) == null, "lambda resolver miss null");
    }

    // ---- Part 7: 与 ClassHierarchyResolverOption 组合走 verify 真闭环（实测锁定） ----
    static void testVerifyClosedLoop() {
        // 构造 a.Child extends 虚拟父类 a.Parent（任何 resolver 之外不可解析）
        byte[] b = ClassFile.of().build(ClassDesc.of("a.Child"), cb -> cb
                .withSuperclass(ClassDesc.of("a.Parent"))
                .withMethodBody("g", MethodTypeDesc.ofDescriptor("()La/Parent;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.new_(ClassDesc.of("a.Child")).dup()
                                .invokespecial(ClassDesc.of("a.Child"), "<init>", MethodTypeDesc.ofDescriptor("()V"))
                                .areturn()));
        // 默认 resolver：verify 报 1 个 VerifyError（Could not resolve class Parent）
        List<VerifyError> errs = ClassFile.of().verify(b);
        check(errs.size() == 1, "default resolver verify 1 error");
        check(errs.get(0).getMessage() != null && errs.get(0).getMessage().contains("Parent"),
                "default verify error mentions Parent: " + errs.get(0).getMessage());
        // 自造层级 {Child->Parent, Parent->Object} 注入后 verify 干净
        Map<ClassDesc, ClassDesc> sup = new java.util.HashMap<>();
        sup.put(ClassDesc.of("a.Child"), ClassDesc.of("a.Parent"));
        sup.put(ClassDesc.of("a.Parent"), ClassDesc.of("java.lang.Object"));
        ClassHierarchyResolver res = ClassHierarchyResolver.of(List.of(), sup);
        ClassFile.ClassHierarchyResolverOption opt = ClassFile.ClassHierarchyResolverOption.of(res);
        check(opt.classHierarchyResolver() == res, "option accessor returns same resolver");
        check(ClassFile.of(opt).verify(b).isEmpty(), "custom-resolver verify clean (closed loop)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDefaultResolver();
        testStaticHierarchy();
        testComposition();
        testOtherFactories();
        testVerifyClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
