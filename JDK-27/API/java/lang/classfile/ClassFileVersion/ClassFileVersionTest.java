/*
 * @test
 * @summary java.lang.classfile.ClassFileVersion conformance: white-box structure + black-box behavior.
 * @run main ClassFileVersionTest
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

public class ClassFileVersionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileVersion");
        check(c != null, "java.lang.classfile.ClassFileVersion loads");
        check(c.getName().equals("java.lang.classfile.ClassFileVersion"), "binary name java.lang.classfile.ClassFileVersion");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileVersion is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileVersion is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFileVersion has no superclass");
        check(declares(c, "majorVersion"), "declares majorVersion");
        check(declares(c, "minorVersion"), "declares minorVersion");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFileVersion v = ClassFileVersion.of(52, 0);
        check(v.majorVersion() == 52, "major 52");
        check(v.minorVersion() == 0, "minor 0");
        ClassFileVersion v2 = ClassFileVersion.of(71, 0xFFFF);
        check(v2.majorVersion() == 71 && v2.minorVersion() == 0xFFFF, "preview minor");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(ClassFileVersion.of(52, 0)));
        check(ClassFile.of().parse(b).majorVersion() == 52, "version element round-trip");

    }


    // ---- Part 3: 工厂矩阵 + u2 边界 + 值语义（实测锁定） ----
    static void testFactoryMatrix() {
        check(ClassFileVersion.of(71, 0).majorVersion() == 71 && ClassFileVersion.of(71, 0).minorVersion() == 0, "of(71,0) readback");
        check(ClassFileVersion.of(45, 3).majorVersion() == 45 && ClassFileVersion.of(45, 3).minorVersion() == 3, "of(45,3) readback");
        check(ClassFileVersion.of(0, 0).majorVersion() == 0 && ClassFileVersion.of(0, 0).minorVersion() == 0, "of(0,0) readback");
        check(ClassFileVersion.of(65535, 65535).majorVersion() == 65535 && ClassFileVersion.of(65535, 65535).minorVersion() == 65535, "of(u2max,u2max) readback");
        // 负数/越界 → IAE 精确消息（实测）
        try { ClassFileVersion.of(-1, 0); check(false, "of(-1,0) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("major version out of range of u2: -1"), "of(-1,0) IAE msg: " + e.getMessage()); }
        try { ClassFileVersion.of(71, -5); check(false, "of(71,-5) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("minor version out of range of u2: -5"), "of(71,-5) IAE msg: " + e.getMessage()); }
        try { ClassFileVersion.of(65536, 0); check(false, "of(65536,0) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("out of range"), "of(65536,0) IAE msg contains out of range"); }
        // toString 精确
        check(ClassFileVersion.of(52, 0).toString().equals("ClassFileVersion[majorVersion=52, minorVersion=0]"), "toString exact");
        check(ClassFileVersion.of(71, 0xFFFF).toString().equals("ClassFileVersion[majorVersion=71, minorVersion=65535]"), "toString preview minor");
        // 值语义：无 equals/hashCode 值相等 —— 两个 of(52,0) 互不相等（身份语义，实测锁定）
        ClassFileVersion a = ClassFileVersion.of(52, 0);
        ClassFileVersion b2 = ClassFileVersion.of(52, 0);
        check(a.equals(a), "reflexive equals");
        check(!a.equals(b2), "two of(52,0) NOT equals (identity semantics, 实测)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(ClassFileVersion.of(71, 0)), "different version not equals");
        check(a.hashCode() == a.hashCode(), "hashCode self-consistent");
        // 层次：ClassElement + ClassFileElement
        check(a instanceof ClassElement, "ClassFileVersion instanceof ClassElement");
        check(a instanceof ClassFileElement, "ClassFileVersion instanceof ClassFileElement");
    }

    // ---- Part 4: build 注入 → re-parse（withVersion 默认方法 + with 元素） ----
    static void testBuildInjection() throws Exception {
        ClassFile cf = ClassFile.of();
        // with(ClassFileVersion) 元素注入
        byte[] b1 = cf.build(ClassDesc.of("pkg.Old1"), cb -> cb.with(ClassFileVersion.of(50, 0)).withSuperclass(ClassDesc.of("java.lang.Object")));
        ClassModel m1 = cf.parse(b1);
        check(m1.majorVersion() == 50 && m1.minorVersion() == 0, "with(ClassFileVersion(50,0)) roundtrip");
        boolean found = false;
        for (ClassElement e : m1.elementList()) if (e instanceof ClassFileVersion cv && cv.majorVersion() == 50 && cv.minorVersion() == 0) found = true;
        check(found, "ClassFileVersion element in elementList");
        // withVersion(int,int) 默认方法
        byte[] b2 = cf.build(ClassDesc.of("pkg.Old2"), cb -> cb.withVersion(45, 3).withSuperclass(ClassDesc.of("java.lang.Object")));
        ClassModel m2 = cf.parse(b2);
        check(m2.majorVersion() == 45 && m2.minorVersion() == 3, "withVersion(45,3) roundtrip");
        // minor 预览值可往返；major 超过 latestMajorVersion 的类 parse 拒绝（实测 IAE）
        byte[] b3 = cf.build(ClassDesc.of("pkg.Prev"), cb -> cb.withVersion(71, 65535).withSuperclass(ClassDesc.of("java.lang.Object")));
        ClassModel m3 = cf.parse(b3);
        check(m3.majorVersion() == 71 && m3.minorVersion() == 65535, "withVersion(71,preview) roundtrip");
        byte[] b5 = cf.build(ClassDesc.of("pkg.Fut"), cb -> cb.withVersion(72, 0).withSuperclass(ClassDesc.of("java.lang.Object")));
        try {
            cf.parse(b5);
            check(false, "parse future version must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Unsupported class file version: 72"), "future version IAE msg: " + e.getMessage());
        }
        // 默认版本 == latest
        byte[] b4 = cf.build(ClassDesc.of("pkg.New"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")));
        ClassModel m4 = cf.parse(b4);
        check(m4.majorVersion() == ClassFile.latestMajorVersion() && m4.majorVersion() == ClassFile.JAVA_27_VERSION, "default build major == JAVA_27_VERSION == 71");
        check(m4.minorVersion() == ClassFile.latestMinorVersion() && m4.minorVersion() == 0, "default build minor == latestMinorVersion 0");
        // 注入后 verify 仍干净
        check(cf.verify(m1).isEmpty(), "old-version verify clean");
        check(cf.verify(m4).isEmpty(), "latest-version verify clean");
    }

    // ---- Part 5: defineClass 运行闭环（低版本类仍可定义运行） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynClassFileVersion" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .with(ClassFileVersion.of(52, 0))
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.bipush(7).ireturn()));
        ClassModel rm = cf.parse(b);
        check(rm.majorVersion() == 52, "dyn v52 roundtrip");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("go").invoke(null).equals(7), "dyn v52 go()==7 运行闭环");
        check(cf.verify(rm).isEmpty(), "dyn v52 verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testBuildInjection();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
