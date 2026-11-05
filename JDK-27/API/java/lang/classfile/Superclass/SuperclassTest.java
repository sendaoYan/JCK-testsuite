/*
 * @test
 * @summary java.lang.classfile.Superclass conformance: white-box structure + black-box behavior.
 * @run main SuperclassTest
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

public class SuperclassTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Superclass");
        check(c != null, "java.lang.classfile.Superclass loads");
        check(c.getName().equals("java.lang.classfile.Superclass"), "binary name java.lang.classfile.Superclass");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Superclass is public");
        check(c.isInterface(), "java.lang.classfile.Superclass is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Superclass has no superclass");
        check(declares(c, "superclassEntry"), "declares superclassEntry");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Superclass sc = Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")));
        check(sc.superclassEntry().asInternalName().equals("java/lang/Object"), "superclassEntry");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")))));
        check(ClassFile.of().parse(b).superclass().get().asInternalName().equals("java/lang/Object"), "round-trip");

    }


    // ---- Part 3: null 负例 + 默认 superclass + 层次 ----
    static void testNullAndDefaults() {
        // Superclass.of(null) → NPE（实测）
        try { Superclass.of(null); check(false, "Superclass.of(null) must throw"); }
        catch (NullPointerException e) { check(true, "Superclass.of(null) NPE"); }
        // withSuperclass((ClassDesc)null) → NPE（实测，消息含 ClassDesc.isClassOrInterface）
        try {
            ClassFile.of().build(ClassDesc.of("pkg.Bad"), cb -> cb.withSuperclass((ClassDesc) null));
            check(false, "withSuperclass(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("isClassOrInterface"), "withSuperclass(null) NPE msg: " + e.getMessage());
        }
        // 层次
        Superclass sc = Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")));
        check(sc instanceof ClassElement, "Superclass instanceof ClassElement");
        check(sc instanceof ClassFileElement, "Superclass instanceof ClassFileElement");
    }

    // ---- Part 4: build 注入 → re-parse（默认/自定义/元素一致性/Object/module-info 空） ----
    static void testBuildReadback() throws Exception {
        ClassFile cf = ClassFile.of();
        // 不指定 superclass：默认 java/lang/Object 且 Superclass 元素存在
        byte[] db = cf.build(ClassDesc.of("pkg.Def"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> c.return_()));
        ClassModel dm = cf.parse(db);
        check(dm.superclass().isPresent() && dm.superclass().get().asInternalName().equals("java/lang/Object"), "default superclass Object");
        int scCount = 0;
        for (ClassElement e : dm.elementList()) if (e instanceof Superclass) scCount++;
        check(scCount == 1, "default Superclass element exactly 1");
        // withSuperclass(ClassDesc) + withSuperclass(ClassEntry) 两重载
        byte[] b1 = cf.build(ClassDesc.of("pkg.Ex"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Exception")));
        ClassModel m1 = cf.parse(b1);
        check(m1.superclass().get().asInternalName().equals("java/lang/Exception"), "withSuperclass(ClassDesc) roundtrip");
        byte[] b2 = cf.build(ClassDesc.of("pkg.Ex2"), cb -> cb.withSuperclass(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.RuntimeException"))));
        ClassModel m2 = cf.parse(b2);
        check(m2.superclass().get().asInternalName().equals("java/lang/RuntimeException"), "withSuperclass(ClassEntry) roundtrip");
        // Superclass 元素与 cm.superclass() 同一 entry
        Superclass elem = null;
        for (ClassElement e : m1.elementList()) if (e instanceof Superclass x) elem = x;
        check(elem != null, "Superclass element present in elementList");
        check(elem.superclassEntry() == m1.superclass().get(), "element entry identity == cm.superclass().get()");
        check(elem.superclassEntry().asSymbol().equals(ClassDesc.of("java.lang.Exception")), "element entry asSymbol");
        check(cf.verify(m1).isEmpty() && cf.verify(m2).isEmpty(), "superclass verify clean");
        // Object.class：superclass empty（JVMS：Object 无父类）
        byte[] ob = java.lang.Object.class.getResourceAsStream("Object.class").readAllBytes();
        ClassModel om = cf.parse(ob);
        check(om.superclass().isEmpty(), "Object.class superclass empty");
        // module-info：superclass empty
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.sc"), mb -> {});
        ClassModel mm = cf.parse(cf.buildModule(ma));
        check(mm.superclass().isEmpty(), "module-info superclass empty");
        // 接口 build：superclass 仍是 java/lang/Object
        byte[] ib = cf.build(ClassDesc.of("pkg.MyIf"), cb -> cb.withFlags(ClassFile.ACC_INTERFACE | ClassFile.ACC_ABSTRACT));
        ClassModel im = cf.parse(ib);
        check(im.superclass().get().asInternalName().equals("java/lang/Object"), "interface superclass Object");
        check(im.flags().has(AccessFlag.INTERFACE), "interface has INTERFACE flag");
        // 真实 javac 产物
        byte[] self = SuperclassTest.class.getResourceAsStream("SuperclassTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        check(sm.superclass().get().asInternalName().equals("java/lang/Object"), "self superclass Object");
    }

    // ---- Part 5: defineClass 运行闭环（继承 Exception 真实例化） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynSuperclass" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Exception"))
                .withMethod("<init>", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"), ClassFile.ACC_PUBLIC,
                        mb -> mb.withCode(c -> c.aload(0).aload(1).invokespecial(ClassDesc.of("java.lang.Exception"), "<init>",
                                MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V")).return_())));
        ClassModel rm = cf.parse(b);
        check(rm.superclass().get().asInternalName().equals("java/lang/Exception"), "dyn superclass Exception");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        Exception ex = (Exception) dc.getConstructor(String.class).newInstance("boom");
        check(ex.getMessage().equals("boom"), "dyn Exception 消息运行闭环");
        check(ex instanceof java.lang.Exception, "dyn instanceof Exception");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullAndDefaults();
        testBuildReadback();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
