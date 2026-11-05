/*
 * @test
 * @summary java.lang.classfile.ClassBuilder conformance: white-box structure + black-box behavior.
 * @run main ClassBuilderTest
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

public class ClassBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassBuilder");
        check(c != null, "java.lang.classfile.ClassBuilder loads");
        check(c.getName().equals("java.lang.classfile.ClassBuilder"), "binary name java.lang.classfile.ClassBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassBuilder is public");
        check(c.isInterface(), "java.lang.classfile.ClassBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassBuilder has no superclass");
        check(declares(c, "withVersion"), "declares withVersion");
        check(declares(c, "withFlags"), "declares withFlags");
        check(declares(c, "withSuperclass"), "declares withSuperclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL).withField("f", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.return_()).withInterfaceSymbols(ClassDesc.of("java.io.Serializable")));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL), "flags");
        check(cm.interfaces().size() == 1 && cm.interfaces().get(0).asSymbol().equals(ClassDesc.of("java.io.Serializable")), "interface");
        check(cm.fields().get(0).fieldType().stringValue().equals("I"), "field type");
        check(cm.methods().get(0).code().isPresent(), "method code");
        check(cm.constantPool().size() > 0, "constant pool");

    }

    // ---- Part 3: 全通道注入 → build → re-parse 全等表（实测锁定） ----
    static void testChannelMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        // withField/withMethod/withMethodBody/withInterfaceSymbols/withSuperclass 全通道
        byte[] b = cf.build(ClassDesc.of("Cbt1.T"), cb -> cb
                .withVersion(52, 0)
                .withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER)
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withInterfaceSymbols(List.of(ClassDesc.of("java.io.Serializable")))
                .withField("fa", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withField("fb", ConstantDescs.CD_String, ClassFile.ACC_PUBLIC)
                .withMethod("m1", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, mb -> mb.withCode(c -> c.iconst_1().ireturn()))
                .withMethodBody("m2", MethodTypeDesc.ofDescriptor("(I)J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iload(0).i2l().lreturn()));
        ClassModel cm = cf.parse(b);
        check(cm.majorVersion() == 52 && cm.minorVersion() == 0, "withVersion 52.0");
        check(cm.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER), "class flags mask");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "superclass");
        check(cm.interfaces().size() == 1 && cm.interfaces().get(0).asInternalName().equals("java/io/Serializable"), "interface symbol");
        check(cm.fields().size() == 2, "field count 2");
        check(cm.fields().get(0).fieldName().stringValue().equals("fa") && cm.fields().get(0).fieldType().stringValue().equals("I"), "field0 name+desc");
        check(cm.fields().get(0).flags().flagsMask() == ClassFile.ACC_PRIVATE, "field0 flags");
        check(cm.fields().get(1).fieldName().stringValue().equals("fb") && cm.fields().get(1).fieldType().stringValue().equals("Ljava/lang/String;"), "field1 name+desc");
        check(cm.methods().size() == 2, "method count 2");
        check(cm.methods().get(0).methodName().stringValue().equals("m1") && cm.methods().get(0).methodType().stringValue().equals("()I"), "method0 name+desc");
        check(cm.methods().get(1).methodName().stringValue().equals("m2") && cm.methods().get(1).methodTypeSymbol().descriptorString().equals("(I)J"), "method1 name+typeSymbol");
        check(cf.verify(cm).isEmpty(), "channel matrix verify clean");
        // 顺序保持：fields()/methods() 按注入顺序
        List<String> fn = new ArrayList<>(), mn = new ArrayList<>();
        for (FieldModel f : cm.fields()) fn.add(f.fieldName().stringValue());
        for (MethodModel m : cm.methods()) mn.add(m.methodName().stringValue());
        check(fn.equals(List.of("fa", "fb")), "field order preserved: " + fn);
        check(mn.equals(List.of("m1", "m2")), "method order preserved: " + mn);
        // elementList 中成员顺序同样保持
        List<String> seq = new ArrayList<>();
        for (ClassElement e : cm.elementList())
            if (e instanceof FieldModel f) seq.add("F:" + f.fieldName().stringValue());
            else if (e instanceof MethodModel m) seq.add("M:" + m.methodName().stringValue());
        check(seq.equals(List.of("F:fa", "F:fb", "M:m1", "M:m2")), "elementList member order: " + seq);
        // withInterfaces(ClassEntry...) + withSuperclass(ClassEntry) 条目重载
        byte[] b2 = cf.build(ClassDesc.of("Cbt2.U"), cb -> cb
                .withSuperclass(cb.constantPool().classEntry(ClassDesc.of("java.lang.Number")))
                .withInterfaces(cb.constantPool().classEntry(ClassDesc.of("java.io.Serializable")),
                        cb.constantPool().classEntry(ClassDesc.of("java.lang.Cloneable"))));
        ClassModel cm2 = cf.parse(b2);
        check(cm2.superclass().get().asInternalName().equals("java/lang/Number"), "withSuperclass(ClassEntry)");
        check(cm2.interfaces().size() == 2
                && cm2.interfaces().get(0).asInternalName().equals("java/io/Serializable")
                && cm2.interfaces().get(1).asInternalName().equals("java/lang/Cloneable"), "withInterfaces(ClassEntry...) order");
        // withInterfaceSymbols(ClassDesc...) 可变参重载
        byte[] b3 = cf.build(ClassDesc.of("Cbt3.V"), cb -> cb
                .withInterfaceSymbols(ClassDesc.of("java.io.Serializable"), ClassDesc.of("java.lang.Runnable")));
        ClassModel cm3 = cf.parse(b3);
        check(cm3.interfaces().size() == 2 && cm3.interfaces().get(1).asInternalName().equals("java/lang/Runnable"), "withInterfaceSymbols varargs");
        // withField/withMethod Utf8Entry 重载（从 cb.constantPool() 取条目）
        byte[] b4 = cf.build(ClassDesc.of("Cbt4.W"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            cb.withField(cpb.utf8Entry("ef"), cpb.utf8Entry("I"), ClassFile.ACC_STATIC);
            cb.withMethod(cpb.utf8Entry("em"), cpb.utf8Entry("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, mb -> mb.withCode(c -> c.iconst_2().ireturn()));
            cb.withMethodBody(cpb.utf8Entry("eb"), cpb.utf8Entry("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_3().ireturn());
        });
        ClassModel cm4 = cf.parse(b4);
        check(cm4.fields().get(0).fieldName().stringValue().equals("ef"), "withField(Utf8Entry...)");
        check(cm4.methods().size() == 2 && cm4.methods().get(0).methodName().stringValue().equals("em"), "withMethod(Utf8Entry...)");
        check(cm4.methods().get(1).methodName().stringValue().equals("eb"), "withMethodBody(Utf8Entry...)");
        // withField(String,ClassDesc,Consumer) 回调通道 + withFlags(AccessFlag...)
        byte[] b5 = cf.build(ClassDesc.of("Cbt5.X"), cb -> cb
                .withField("cf", ConstantDescs.CD_int, fb -> fb.withFlags(AccessFlag.STATIC, AccessFlag.FINAL))
                .withFlags(AccessFlag.PUBLIC, AccessFlag.ABSTRACT));
        ClassModel cm5 = cf.parse(b5);
        check(cm5.fields().get(0).flags().flagsMask() == (ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "consumer withFlags(AccessFlag...)");
        check(cm5.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT), "class withFlags(AccessFlag...)");
        // with(ClassFileVersion) 元素注入（照 ClassFileTest.testVersionTable 模板）
        byte[] b6 = cf.build(ClassDesc.of("Cbt6.Y"), cb -> cb.with(ClassFileVersion.of(49, 0)).withSuperclass(ConstantDescs.CD_Object));
        ClassModel cm6 = cf.parse(b6);
        check(cm6.majorVersion() == 49 && cm6.minorVersion() == 0, "with(ClassFileVersion) roundtrip");
        boolean verElem = false;
        for (ClassElement e : cm6.elementList()) if (e instanceof ClassFileVersion cv && cv.majorVersion() == 49) verElem = true;
        check(verElem, "ClassFileVersion element in elementList");
        // with(Interfaces) 元素重注入（从已解析模型取元素再注入新构建）
        Interfaces ie = cm3.elementList().stream().filter(e -> e instanceof Interfaces).map(e -> (Interfaces) e).findFirst().get();
        byte[] b7 = cf.build(ClassDesc.of("Cbt7.Z"), cb -> cb.with(ie));
        check(cf.parse(b7).interfaces().size() == 2, "with(Interfaces element) re-inject");
    }

    // ---- Part 4: 负例矩阵（实测锁定精确异常+消息） ----
    static void testNegativeMatrix() {
        ClassFile cf = ClassFile.of();
        try {
            cf.build(ClassDesc.of("CbtN1"), cb -> cb.withSuperclass((ClassDesc) null));
            check(false, "withSuperclass(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("cd") && e.getMessage().contains("isClassOrInterface"), "withSuperclass(null) NPE msg: " + e.getMessage());
        }
        try {
            cf.build(ClassDesc.of("CbtN2"), cb -> cb.withField(null, ConstantDescs.CD_int, 0));
            check(false, "withField(null,...) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"s\" is null"), "withField(null name) NPE msg: " + e.getMessage());
        }
        try {
            cf.build(ClassDesc.of("CbtN3"), cb -> cb.withField("x", (ClassDesc) null, 0));
            check(false, "withField(x,null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"desc\" is null"), "withField(null type) NPE msg: " + e.getMessage());
        }
        try {
            cf.build(ClassDesc.of("CbtN4"), cb -> cb.withMethod(null, MethodTypeDesc.ofDescriptor("()V"), 0, mb -> { }));
            check(false, "withMethod(null,...) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"s\" is null"), "withMethod(null name) NPE msg: " + e.getMessage());
        }
        try {
            cf.build(ClassDesc.of("CbtN5"), cb -> cb.withMethod("m", (MethodTypeDesc) null, 0, mb -> { }));
            check(false, "withMethod(m,null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"desc\" is null"), "withMethod(null type) NPE msg: " + e.getMessage());
        }
        // withMethodBody 空 code → IAE（Code length 0；实测锁定）
        try {
            cf.build(ClassDesc.of("CbtN6"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> { }));
            check(false, "empty body must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Code length 0"), "empty body IAE msg: " + e.getMessage());
        }
    }

    // ---- Part 5: 重复成员 + transformClass 回调收到的就是 ClassBuilder ----
    static void testDuplicatesAndTransform() throws Exception {
        ClassFile cf = ClassFile.of();
        // 同名字段重复注入：允许（实测 count==2；verify 报错记录在案）
        byte[] b = cf.build(ClassDesc.of("CbtD1"), cb -> cb
                .withField("dup", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC)
                .withField("dup", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC));
        ClassModel cm = cf.parse(b);
        check(cm.fields().size() == 2, "duplicate same-name fields allowed (count 2)");
        check(cf.verify(cm).size() >= 1, "duplicate fields flagged by verify");
        // 同名同描述符方法重复注入：同样允许（JVMS 重复成员在 verify 阶段才拒绝）
        byte[] b2 = cf.build(ClassDesc.of("CbtD2"), cb -> cb
                .withMethodBody("dm", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_1().ireturn())
                .withMethodBody("dm", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_2().ireturn()));
        check(cf.parse(b2).methods().size() == 2, "duplicate methods allowed (count 2)");
        // transformClass 的 ClassTransform.accept(B,E) 中 B 即 ClassBuilder：constantPool() 非 null 且可继续 with
        byte[] src = cf.build(ClassDesc.of("CbtD3"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.iconst_1().ireturn()));
        ClassModel sm = cf.parse(src);
        final boolean[] ok = new boolean[2];
        byte[] out = cf.transformClass(sm, new ClassTransform() {
            public void accept(ClassBuilder cb2, ClassElement e) {
                ok[0] = cb2 instanceof ClassBuilder; // 回调收到的就是 ClassBuilder
                ok[1] = cb2.constantPool() != null; // ClassFileBuilder.constantPool() 通道可用
                cb2.with(e);
            }
        });
        check(ok[0] && ok[1], "transform callback receives ClassBuilder with live constantPool");
        check(cf.parse(out).methods().size() == 1, "transform passthrough member");
        // endHandler 中拿到的 builder 注入字段
        byte[] out2 = cf.transformClass(sm, ClassTransform.endHandler(cb2 -> cb2.withField("inj", ConstantDescs.CD_int, ClassFile.ACC_STATIC)));
        check(cf.parse(out2).fields().size() == 1, "endHandler ClassBuilder injects field");
        // transformField/transformMethod 通道（ClassBuilder 抽象方法）
        byte[] src2 = cf.build(ClassDesc.of("CbtD4"), cb -> cb
                .withField("f", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.bipush(6).ireturn()));
        ClassModel sm2 = cf.parse(src2);
        final boolean[] fok = new boolean[2];
        byte[] out3 = cf.transformClass(sm2, new ClassTransform() {
            public void accept(ClassBuilder cb2, ClassElement e) {
                if (e instanceof FieldModel fm) cb2.transformField(fm, FieldTransform.ACCEPT_ALL);
                else if (e instanceof MethodModel mm) {
                    cb2.transformMethod(mm, new MethodTransform() {
                        public void accept(MethodBuilder mb, MethodElement me) { fok[0] = mb.constantPool() != null; mb.with(me); }
                    });
                } else cb2.with(e);
            }
        });
        ClassModel om3 = cf.parse(out3);
        check(om3.fields().size() == 1 && om3.methods().size() == 1, "transformField/transformMethod preserve members");
        check(fok[0], "MethodBuilder in transform has constantPool");
    }

    // ---- Part 6: defineClass 运行闭环 ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynCB" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .withMethod("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        mb -> mb.withCode(c -> c.iconst_1().ireturn()))
                .withMethodBody("add", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iload(0).iload(1).iadd().ireturn()));
        check(b.length > 8 && b[0] == (byte) 0xCA, "dyn bytes non-empty");
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass roundtrip");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getName().equals(name), "defined class name");
        check(dc.getMethod("go").invoke(null).equals(1), "DynCB go()==1");
        check(dc.getMethod("add", int.class, int.class).invoke(null, 20, 22).equals(42), "DynCB add(20,22)==42");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testChannelMatrix();
        testNegativeMatrix();
        testDuplicatesAndTransform();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
