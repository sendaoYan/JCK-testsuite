/*
 * @test
 * @summary java.lang.classfile.ClassModel conformance: white-box structure + black-box behavior.
 * @run main ClassModelTest
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

public class ClassModelTest {
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

    // ---- real-class members (javac 产物属性源) ----
    static final int CV = 42;
    @Deprecated static int depField;
    interface NestedIf {}
    static class WithIfs implements Runnable, NestedIf, java.io.Serializable { public void run() {} }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.ClassModel");
        check(c != null, "java.lang.classfile.ClassModel loads");
        check(c.getName().equals("java.lang.classfile.ClassModel"), "binary name java.lang.classfile.ClassModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassModel is public");
        check(c.isInterface(), "java.lang.classfile.ClassModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassModel has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "thisClass"), "declares thisClass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withInterfaceSymbols(ClassDesc.of("java.lang.Runnable")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("add", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC, cob -> cob.iload(0).iload(1).iadd().ireturn()));
        ClassModel cm = cf.parse(b);
        check("pkg/Sample".equals(cm.thisClass().asInternalName()), "thisClass");
        check(cm.majorVersion() == 71 && cm.minorVersion() == 0, "version");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "superclass");
        check(cm.interfaces().size() == 1 && cm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "interfaces");
        check(!cm.isModuleInfo(), "not module-info");
        check(cm.flags().has(AccessFlag.PUBLIC), "flags PUBLIC");
        check(cm.constantPool().size() > 0, "constantPool nonempty");
        check(cm.fields().size() == 1 && cm.methods().size() == 1, "fields+methods");
        check(cm.fields().get(0).fieldName().stringValue().equals("x"), "field name");
        check(cm.methods().get(0).methodName().stringValue().equals("add"), "method name");
        check(cm.elementList().size() >= 3, "elementList");
        check(cm.toDebugString().contains("pkg"), "toDebugString");

    }

    // ---- Part 3: parse 自身 .class 字节（javac 真实产物全量回读） ----
    static void testSelfParse() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = ClassModelTest.class.getResourceAsStream("ClassModelTest.class").readAllBytes();
        check(self.length > 8, "self bytes nonempty");
        ClassModel cm = cf.parse(self);
        check(cm.thisClass().asInternalName().equals(ClassModelTest.class.getName().replace('.', '/')), "self thisClass internal name");
        check(cm.thisClass().asSymbol().equals(ClassDesc.of(ClassModelTest.class.getName())), "self thisClass asSymbol");
        check(cm.superclass().isPresent() && cm.superclass().get().asInternalName().equals("java/lang/Object"), "self superclass is Object");
        check(cm.superclass().get().asSymbol().descriptorString().equals("Ljava/lang/Object;"), "self superclass descriptor");
        check(cm.majorVersion() == 71, "self major 71 (javac 27 target)");
        check(cm.minorVersion() == 0, "self minor 0");
        check(cm.flags().has(AccessFlag.PUBLIC), "self flags PUBLIC");
        check(cm.flags().location() == AccessFlag.Location.CLASS, "self flags location CLASS");
        check(!cm.isModuleInfo(), "self not module-info");
        check(cm.constantPool().size() > 0, "self constantPool size > 0");
        check(cm.interfaces().isEmpty(), "self interfaces empty");
        // 元素类型直方图：contains 计数（非脆弱全等）
        int ver = 0, flg = 0, sup = 0, ifs = 0, fm = 0, mm = 0, sf = 0, other = 0;
        for (ClassElement e : cm.elementList()) {
            if (e instanceof ClassFileVersion) ver++;
            else if (e instanceof AccessFlags) flg++;
            else if (e instanceof Superclass) sup++;
            else if (e instanceof Interfaces) ifs++;
            else if (e instanceof FieldModel) fm++;
            else if (e instanceof MethodModel) mm++;
            else if (e instanceof SourceFileAttribute) sf++;
            else other++;
        }
        check(ver == 1, "self elementList ClassFileVersion exactly 1: " + ver);
        check(flg == 1, "self elementList AccessFlags exactly 1: " + flg);
        check(sup == 1, "self elementList Superclass exactly 1: " + sup);
        check(ifs == 1, "self elementList Interfaces exactly 1: " + ifs);
        check(sf == 1, "self elementList SourceFile exactly 1: " + sf);
        check(fm == cm.fields().size() && fm >= 2, "self FieldModel count == fields() (" + fm + "/" + cm.fields().size() + ")");
        check(mm == cm.methods().size() && mm >= 3, "self MethodModel count == methods() (" + mm + "/" + cm.methods().size() + ")");
        for (ClassElement e : cm.elementList()) check(e != null, "self element non-null");
        // SourceFile 属性值 == 类名 + ".java"
        check(cm.findAttribute(Attributes.sourceFile()).isPresent(), "self SourceFile attribute present");
        check(cm.findAttribute(Attributes.sourceFile()).get().sourceFile().stringValue().equals("ClassModelTest.java"), "self SourceFile value");
        check(cm.findAttribute(Attributes.sourceFile()).get().attributeName().stringValue().equals(Attributes.NAME_SOURCE_FILE), "SourceFile attributeName");
        // methods()/fields() 与 elementList 一致性 + parent 循环身份
        for (MethodModel m : cm.methods()) check(m.parent().isPresent() && m.parent().get() == cm, "method parent identity == cm");
        for (FieldModel f : cm.fields()) check(f.parent().isPresent() && f.parent().get() == cm, "field parent identity == cm");
        check(cf.verify(cm).isEmpty(), "self verify clean");
        // toDebugString
        check(cm.toDebugString() != null && cm.toDebugString().contains("ClassModelTest"), "self toDebugString contains simple name");
        // CompoundElement 层次
        check(cm instanceof CompoundElement, "ClassModel instanceof CompoundElement");
        check(cm instanceof AttributedElement, "ClassModel instanceof AttributedElement");
        int iter = 0; for (ClassElement e : cm) iter++;
        check(iter == cm.elementList().size(), "for-each count == elementList size (" + iter + ")");
    }

    // ---- Part 4: build 带接口/多字段/多方法全量回读 + 真实 implements 类 ----
    static void testBuildFullReadback() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.Full"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Exception"))
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"), ClassDesc.of("java.io.Serializable"))
                .withField("f1", ConstantDescs.CD_int,
                        fb -> fb.withFlags(ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC).with(ConstantValueAttribute.of(42)))
                .withField("f2", ConstantDescs.CD_String, ClassFile.ACC_PRIVATE)
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iconst_1().ireturn())
                .withMethod("abs", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT,
                        mb -> {}));
        ClassModel cm = cf.parse(b);
        check(cm.superclass().get().asInternalName().equals("java/lang/Exception"), "full superclass Exception");
        check(cm.interfaces().size() == 2, "full 2 interfaces");
        check(cm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "full iface[0] Runnable");
        check(cm.interfaces().get(1).asInternalName().equals("java/io/Serializable"), "full iface[1] Serializable");
        check(cm.fields().size() == 2, "full 2 fields");
        check(cm.methods().size() == 2, "full 2 methods");
        int mm = 0; for (ClassElement e : cm.elementList()) if (e instanceof MethodModel) mm++;
        check(mm == 2, "full MethodModel count in elementList == 2");
        check(!cm.isModuleInfo(), "full not module-info");
        check(cf.verify(cm).isEmpty(), "full verify clean");
        // 常量字段 ConstantValue 回读
        FieldModel f1 = cm.fields().get(0);
        check(f1.fieldName().stringValue().equals("f1"), "full f1 name");
        ConstantValueAttribute cva = f1.findAttribute(Attributes.constantValue()).get();
        check(((IntegerEntry) cva.constant()).intValue() == 42, "full f1 ConstantValue 42");
        // 抽象方法 code() empty
        MethodModel abs = cm.methods().get(1);
        check(abs.methodName().stringValue().equals("abs") && abs.code().isEmpty(), "full abstract code empty");
        // 真实 javac 产物：嵌套 implements 类
        byte[] wb = ClassModelTest.class.getResourceAsStream("ClassModelTest$WithIfs.class").readAllBytes();
        ClassModel wm = cf.parse(wb);
        List<String> names = new ArrayList<>();
        wm.interfaces().forEach(ce -> names.add(ce.asInternalName()));
        check(names.equals(List.of("java/lang/Runnable", "ClassModelTest$NestedIf", "java/io/Serializable")),
                "WithIfs interfaces exact order: " + names);
        check(!wm.isModuleInfo(), "WithIfs not module-info");
        // Object.class：superclass empty
        byte[] ob = java.lang.Object.class.getResourceAsStream("Object.class").readAllBytes();
        ClassModel om = cf.parse(ob);
        check(om.superclass().isEmpty(), "Object.class superclass empty");
        check(om.thisClass().asInternalName().equals("java/lang/Object"), "Object.class thisClass");
        check(om.interfaces().isEmpty(), "Object.class interfaces empty");
        // module-info：superclass empty + isModuleInfo true
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.cm"), mb -> {});
        ClassModel modm = cf.parse(cf.buildModule(ma));
        check(modm.isModuleInfo(), "module-info isModuleInfo");
        check(modm.superclass().isEmpty(), "module-info superclass empty");
        // 反例：坏 bytes
        try {
            cf.parse(new byte[0]);
            check(false, "parse(empty) must throw");
        } catch (IllegalArgumentException e) {
            check("Bad magic number".equals(e.getMessage()), "parse(empty) IAE message");
        }
        try {
            cf.parse(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 71});
            check(false, "parse(truncated header) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "parse(truncated) IAE non-null message");
        }
    }

    // ---- Part 5: defineClass 运行闭环 ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynClassModel" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"))
                .withField("V", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC)
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iconst_1().ireturn())
                .withMethodBody("run", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(name), "V", ConstantDescs.CD_int).pop().return_()));
        check(b.length > 8, "dyn bytes nonempty");
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass roundtrip");
        check(rm.interfaces().size() == 1 && rm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "dyn interface");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getName().equals(name), "dyn defined class name");
        check(Runnable.class.isAssignableFrom(dc), "dyn implements Runnable");
        check(dc.getMethod("go").invoke(null).equals(1), "dyn go()==1");
        dc.getMethod("run").invoke(null);
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSelfParse();
        testBuildFullReadback();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
