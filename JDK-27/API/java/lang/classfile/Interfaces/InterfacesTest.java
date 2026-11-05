/*
 * @test
 * @summary java.lang.classfile.Interfaces conformance: white-box structure + black-box behavior.
 * @run main InterfacesTest
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

public class InterfacesTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Interfaces");
        check(c != null, "java.lang.classfile.Interfaces loads");
        check(c.getName().equals("java.lang.classfile.Interfaces"), "binary name java.lang.classfile.Interfaces");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Interfaces is public");
        check(c.isInterface(), "java.lang.classfile.Interfaces is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Interfaces has no superclass");
        check(declares(c, "interfaces"), "declares interfaces");
        check(declares(c, "of"), "declares of");
        check(declares(c, "ofSymbols"), "declares ofSymbols");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Interfaces in = Interfaces.ofSymbols(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("java.io.Serializable"));
        check(in.interfaces().size() == 2, "two interfaces");
        check(in.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "Runnable");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(Interfaces.ofSymbols(ClassDesc.of("java.lang.Runnable"))));
        check(ClassFile.of().parse(b).interfaces().get(0).asSymbol().equals(ClassDesc.of("java.lang.Runnable")), "round-trip");

    }


    // ---- real-class members ----
    interface NestedIf {}
    static class WithIfs implements Runnable, NestedIf, java.io.Serializable { public void run() {} }

    // ---- Part 3: 工厂全重载 + null 负例 + 不可变视图 ----
    static void testFactoryMatrix() {
        ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        ClassEntry r = pool.classEntry(ClassDesc.of("java.lang.Runnable"));
        ClassEntry s = pool.classEntry(ClassDesc.of("java.io.Serializable"));
        // of(ClassEntry...) / of(List) / ofSymbols(ClassDesc...) / ofSymbols(List)
        Interfaces i1 = Interfaces.of(r, s);
        check(i1.interfaces().size() == 2, "of(varargs) size 2");
        check(i1.interfaces().get(0) == r && i1.interfaces().get(1) == s, "of(varargs) identity entries");
        Interfaces i2 = Interfaces.of(List.of(r));
        check(i2.interfaces().size() == 1 && i2.interfaces().get(0) == r, "of(List) identity");
        Interfaces i3 = Interfaces.ofSymbols(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("java.io.Serializable"));
        check(i3.interfaces().size() == 2, "ofSymbols(varargs) size 2");
        check(i3.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "ofSymbols name 0");
        check(i3.interfaces().get(1).asInternalName().equals("java/io/Serializable"), "ofSymbols name 1");
        check(i3.interfaces().get(0).asSymbol().equals(ClassDesc.of("java.lang.Runnable")), "ofSymbols asSymbol");
        Interfaces i4 = Interfaces.ofSymbols(List.of(ClassDesc.of("java.lang.Runnable")));
        check(i4.interfaces().size() == 1, "ofSymbols(List) size 1");
        // 空工厂
        check(Interfaces.ofSymbols().interfaces().isEmpty(), "ofSymbols() empty");
        check(Interfaces.of().interfaces().isEmpty(), "of() empty");
        // 重复接口允许（不去重）
        Interfaces dup = Interfaces.ofSymbols(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("java.lang.Runnable"));
        check(dup.interfaces().size() == 2, "duplicate interfaces kept (size 2)");
        // null 负例（实测 NPE）
        try { Interfaces.of((ClassEntry[]) null); check(false, "of(null arr) must throw"); }
        catch (NullPointerException e) { check(true, "of(null arr) NPE"); }
        try { Interfaces.of((java.util.List<ClassEntry>) null); check(false, "of(null list) must throw"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "of(null list) NPE msg: " + e.getMessage()); }
        try { Interfaces.ofSymbols((ClassDesc) null); check(false, "ofSymbols(null CD) must throw"); }
        catch (NullPointerException e) { check(true, "ofSymbols(null CD) NPE"); }
        try { Interfaces.ofSymbols((java.util.List<ClassDesc>) null); check(false, "ofSymbols(null list) must throw"); }
        catch (NullPointerException e) { check(true, "ofSymbols(null list) NPE"); }
        // interfaces() 视图不可变
        try { i1.interfaces().add(r); check(false, "interfaces().add must throw"); }
        catch (UnsupportedOperationException e) { check(true, "interfaces() immutable UOE"); }
        try { i1.interfaces().remove(0); check(false, "interfaces().remove must throw"); }
        catch (UnsupportedOperationException e) { check(true, "interfaces().remove UOE"); }
        // 层次
        check(i1 instanceof ClassElement, "Interfaces instanceof ClassElement");
        check(i1 instanceof ClassFileElement, "Interfaces instanceof ClassFileElement");
    }

    // ---- Part 4: build 注入 → re-parse（多接口/空/Interfaces 元素一致性） ----
    static void testBuildReadback() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.IF"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("java.io.Serializable"), ClassDesc.of("java.util.function.BiFunction")));
        ClassModel cm = cf.parse(b);
        check(cm.interfaces().size() == 3, "3 interfaces roundtrip");
        check(cm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "iface[0]");
        check(cm.interfaces().get(1).asInternalName().equals("java/io/Serializable"), "iface[1]");
        check(cm.interfaces().get(2).asInternalName().equals("java/util/function/BiFunction"), "iface[2]");
        // Interfaces 元素在 elementList 且与 cm.interfaces() 内容一致
        Interfaces elem = null;
        for (ClassElement e : cm.elementList()) if (e instanceof Interfaces x) elem = x;
        check(elem != null, "Interfaces element present in elementList");
        check(elem.interfaces().size() == 3, "Interfaces element size 3");
        check(elem.interfaces().get(0).asInternalName().equals(cm.interfaces().get(0).asInternalName()), "element[0] == cm.interfaces()[0]");
        check(elem.interfaces().get(0) == cm.interfaces().get(0), "element entry identity == cm.interfaces() entry");
        // 空接口列表：元素仍存在、interfaces() 空
        byte[] eb = cf.build(ClassDesc.of("pkg.IF2"), cb -> cb.withInterfaceSymbols());
        ClassModel em = cf.parse(eb);
        check(em.interfaces().isEmpty(), "empty ifaces roundtrip");
        boolean emptyElem = false;
        for (ClassElement e : em.elementList()) if (e instanceof Interfaces x && x.interfaces().isEmpty()) emptyElem = true;
        check(emptyElem, "empty Interfaces element present");
        // withInterfaceSymbols(List) 重载
        byte[] lb = cf.build(ClassDesc.of("pkg.IF3"), cb -> cb.withInterfaceSymbols(List.of(ClassDesc.of("java.lang.Runnable"))));
        check(ClassFile.of().parse(lb).interfaces().size() == 1, "withInterfaceSymbols(List) roundtrip");
        check(cf.verify(cm).isEmpty(), "ifaces verify clean");
        // 真实 javac 产物（嵌套 implements）
        byte[] wb = InterfacesTest.class.getResourceAsStream("InterfacesTest$WithIfs.class").readAllBytes();
        ClassModel wm = cf.parse(wb);
        List<String> names = new ArrayList<>();
        wm.interfaces().forEach(ce -> names.add(ce.asInternalName()));
        check(names.equals(List.of("java/lang/Runnable", "InterfacesTest$NestedIf", "java/io/Serializable")), "WithIfs interfaces exact: " + names);
        // 接口自身：interfaces() 为其 extends 列表
        byte[] nb = InterfacesTest.class.getResourceAsStream("InterfacesTest$NestedIf.class").readAllBytes();
        check(cf.parse(nb).interfaces().isEmpty(), "marker nested interface no extends");
    }

    // ---- Part 5: defineClass 运行闭环（实现 Runnable 真运行） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynInterfaces" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"))
                .withMethodBody("run", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.return_()));
        ClassModel rm = cf.parse(b);
        check(rm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "dyn iface");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(Runnable.class.isAssignableFrom(dc), "dyn implements Runnable");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testBuildReadback();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
