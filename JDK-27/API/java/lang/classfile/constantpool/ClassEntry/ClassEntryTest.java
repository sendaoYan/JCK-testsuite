/*
 * @test
 * @summary java.lang.classfile.constantpool.ClassEntry conformance: white-box structure + black-box behavior.
 * @run main ClassEntryTest
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

public class ClassEntryTest {
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

    static final java.lang.Runnable LAMBDA_R = () -> {};
    private static void touchLambda() { LAMBDA_R.run(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ClassEntry");
        check(c != null, "java.lang.classfile.constantpool.ClassEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ClassEntry"), "binary name java.lang.classfile.constantpool.ClassEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ClassEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ClassEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ClassEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "name"), "declares name");
        check(declares(c, "asInternalName"), "declares asInternalName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check(ce.asInternalName().equals("java/lang/String"), "asInternalName");
        check(ce.asSymbol().equals(ClassDesc.of("java.lang.String")), "asSymbol");
        check(ce.name().stringValue().equals("java/lang/String"), "name");
        check(ce.matches(ClassDesc.of("java.lang.String")), "matches");
        check(ce.constantValue().equals(ClassDesc.of("java.lang.String")), "constantValue");
        check(ce.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ClassEntry 契约（双工厂重载/跨重载去重/符号面/边界/发射闭环/真类池） ----
    static void testClassEntryPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ClassEntry c1 = b.classEntry(ClassDesc.of("java.lang.Object"));
        ClassEntry c2 = b.classEntry(ClassDesc.of("java.lang.Object"));
        check(c1 == c2, "same-value classEntry dedup: identical instance");
        check(c1.index() == c2.index(), "dedup shares index");
        ClassEntry c3 = b.classEntry(ClassDesc.of("java.lang.String"));
        check(c3 != c1 && c3.index() != c1.index(), "different value different entry/index");
        ClassEntry c4 = b.classEntry(b.utf8Entry("java/lang/Object"));
        check(c4 == c1, "cross-overload dedup: classEntry(ClassDesc) == classEntry(Utf8Entry)");
    }

    static void testClassEntryFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ClassEntry c = b.classEntry(ClassDesc.of("java.lang.Object"));
        check("java/lang/Object".equals(c.asInternalName()), "asInternalName slashed form");
        check(c.name().stringValue().equals("java/lang/Object"), "name utf8 is internal name");
        check(c.name() == b.utf8Entry("java/lang/Object"), "name is the shared utf8 entry (identity)");
        check(c.asSymbol().descriptorString().equals("Ljava/lang/Object;"), "asSymbol descriptorString");
        check(c.matches(ClassDesc.of("java.lang.Object")), "matches positive");
        check(!c.matches(ClassDesc.of("java.lang.String")), "matches negative");
        check(c.constantValue() instanceof ClassDesc, "constantValue is ClassDesc");
        check(c.constantValue().equals(c.asSymbol()), "constantValue equals asSymbol");
        check(c.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");
        check(c.tag() == PoolEntry.TAG_CLASS && c.tag() == 7, "tag TAG_CLASS == 7");
        check(c.width() == 1, "single-slot entry");
        check(c.index() > 0, "index positive");
        check(c.constantPool() == b, "constantPool is the builder");
        check(c instanceof LoadableConstantEntry && c instanceof PoolEntry, "hierarchy Loadable+PoolEntry");
        check(!(((Object) c) instanceof ConstantValueEntry), "ClassEntry is NOT ConstantValueEntry");
        // 数组类型描述符同样可作类名
        ClassEntry arr = b.classEntry(ClassDesc.ofDescriptor("[Ljava/lang/String;"));
        check(arr.asInternalName().equals("[Ljava/lang/String;"), "array internal name preserved");
        check(arr.matches(ClassDesc.ofDescriptor("[Ljava/lang/String;")), "matches array descriptor");
        check(b.classEntry(ClassDesc.ofDescriptor("[Ljava/lang/String;")) == arr, "array classEntry dedup");
    }

    static void testClassEntryEmissionAndRealPool() throws Exception {
        // 发射 checkcast -> re-parse 池含对应 ClassEntry 且值回读
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitC"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"), ClassFile.ACC_STATIC,
                        cob -> cob.aconst_null().checkcast(ClassDesc.of("java.lang.String")).areturn()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof ClassEntry ce && ce.asInternalName().equals("java/lang/String") && ce.matches(ClassDesc.of("java.lang.String"))) saw = true;
        check(saw, "emitted checkcast leaves java/lang/String ClassEntry in pool");
        boolean sawSelf = false;
        for (PoolEntry pe : cp2) if (pe instanceof ClassEntry ce && ce.asInternalName().equals("pkg/EmitC")) sawSelf = true;
        check(sawSelf, "this class entry present in built pool");
        // 真类池：thisClass 三段闭环
        byte[] self = ClassEntryTest.class.getResourceAsStream("ClassEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        check(cm.thisClass().asInternalName().equals(ClassEntryTest.class.getName().replace('.', '/')), "real thisClass internal name");
        check(cm.thisClass().matches(ClassDesc.of(ClassEntryTest.class.getName())), "real thisClass matches own symbol");
        check(cm.thisClass().constantPool() == cp, "thisClass.constantPool == model pool");
        check(cp.entryByIndex(cm.thisClass().index(), ClassEntry.class) == cm.thisClass(), "typed entryByIndex returns thisClass identity");
        int clsCount = 0; boolean sawObject = false;
        for (PoolEntry pe : cp) if (pe instanceof ClassEntry ce) { clsCount++; if (ce.asInternalName().equals("java/lang/Object")) sawObject = true; }
        check(clsCount > 0, "real pool has ClassEntry entries");
        check(sawObject, "real pool references java/lang/Object");
        // 越界经 ClassEntry 视角同样抛 ConstantPoolException
        try {
            cp.entryByIndex(cm.thisClass().index(), Utf8Entry.class);
            check(false, "typed mismatch must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "typed mismatch message non-null");
        }
    }

    static void testClassEntryNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.classEntry((ClassDesc) null);
            check(false, "classEntry(null ClassDesc) must throw");
        } catch (NullPointerException e) { check(true, "classEntry(null ClassDesc) NPE"); }
        try {
            b.classEntry((Utf8Entry) null);
            check(false, "classEntry(null Utf8Entry) must throw");
        } catch (NullPointerException e) { check(true, "classEntry(null Utf8Entry) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testClassEntryPutDedup();
        testClassEntryFace();
        testClassEntryEmissionAndRealPool();
        testClassEntryNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
