/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPool conformance: white-box structure + black-box behavior.
 * @run main ConstantPoolTest
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

public class ConstantPoolTest {
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

    static final int CONST_INT = 31337;
    static final long CONST_LONG = 0x123456789ABCDEFL;
    static final float CONST_FLOAT = -2.75f;
    static final double CONST_DOUBLE = 1.25e300;
    static final String CONST_STRING = "constStringValue7";
    static final java.lang.Runnable LAMBDA_R = () -> {};
    private static void touchLambda() { LAMBDA_R.run(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantPool");
        check(c != null, "java.lang.classfile.constantpool.ConstantPool loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantPool"), "binary name java.lang.classfile.constantpool.ConstantPool");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantPool is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantPool is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantPool has no superclass");
        check(declares(c, "entryByIndex"), "declares entryByIndex");
        check(declares(c, "size"), "declares size");
        check(declares(c, "iterator"), "declares iterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()));
        ConstantPool cp = ClassFile.of().parse(b).constantPool();
        check(cp.size() > 0, "size > 0");
        check(cp.entryByIndex(1) != null, "entryByIndex(1)");
        check(cp.entryByIndex(1, Utf8Entry.class).stringValue().equals("pkg/T"), "entryByIndex typed");
        int count = 0;
        for (PoolEntry e : cp) { count++; }
        check(count == cp.size() - 1, "iteration matches size-1");
        check(cp.bootstrapMethodCount() >= 0, "bootstrapMethodCount");
        boolean thrown = false;
        try { cp.entryByIndex(0); } catch (ConstantPoolException e) { thrown = true; }
        check(thrown, "entryByIndex(0) throws ConstantPoolException");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ConstantPool 契约（真类池全面/双槽语义/错误矩阵/typed 查找/迭代器/BSM 面） ----
    static void testRealPoolFace() throws Exception {
        byte[] self = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        check(cp.size() > 0, "real pool size > 0");
        check(cp.entryByIndex(1) != null, "index 1 is the first usable entry");
        check(cp.entryByIndex(1).index() == 1, "entryByIndex returns entry with that index");
        int iterCount = 0, maxIdx = 0;
        boolean consistent = true, monotonic = true;
        int prev = 0;
        for (PoolEntry pe : cp) {
            iterCount++;
            if (cp.entryByIndex(pe.index()) != pe) consistent = false;
            if (pe.index() <= prev) monotonic = false;
            prev = pe.index();
            if (pe.index() > maxIdx) maxIdx = pe.index();
        }
        check(iterCount > 0, "iterator yields entries");
        check(consistent, "entryByIndex(e.index()) == e for every iterated entry");
        check(monotonic, "iterated indices strictly increasing");
        check(prev == maxIdx && cp.size() == maxIdx + 1, "size() == maxIndex + 1");
        check(iterCount < cp.size(), "iterator skips slot 0 and phantom slots");
        int utf8 = 0, cls = 0, mref = 0;
        for (PoolEntry pe : cp) {
            if (pe instanceof Utf8Entry) utf8++;
            else if (pe instanceof ClassEntry) cls++;
            else if (pe instanceof MethodRefEntry) mref++;
        }
        check(utf8 > 0 && cls > 0 && mref > 0, "real pool holds Utf8/Class/MethodRef entries: " + utf8 + "/" + cls + "/" + mref);
        check(cp.entryByIndex(cm.thisClass().index(), ClassEntry.class) == cm.thisClass(), "typed entryByIndex identity for thisClass");
    }

    static void testDoubleSlotSemantics() throws Exception {
        byte[] self = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        // LONG/DOUBLE 双槽：占 index+1 幽灵位（不可用），size 相应多 1
        int twoSlot = 0;
        boolean sawLong = false, sawDouble = false;
        for (PoolEntry pe : cp) {
            if (pe instanceof LongEntry) { twoSlot++; sawLong = true; check(pe.width() == 2, "long width 2"); }
            if (pe instanceof DoubleEntry) { twoSlot++; sawDouble = true; check(pe.width() == 2, "double width 2"); }
        }
        check(sawLong && sawDouble, "real pool contains LONG and DOUBLE constants (CONST_LONG/CONST_DOUBLE)");
        int iterCount = 0;
        for (PoolEntry pe : cp) iterCount++;
        check(cp.size() == iterCount + twoSlot + 1, "size == iterated entries + two-slot phantoms + slot 0 (实测: " + cp.size() + " == " + iterCount + "+" + twoSlot + "+1)");
        for (PoolEntry pe : cp) if (pe instanceof LongEntry || pe instanceof DoubleEntry) {
            try {
                cp.entryByIndex(pe.index() + 1);
                check(false, "phantom slot must throw");
            } catch (ConstantPoolException e) {
                check(e.getMessage() != null, "phantom slot CPE message non-null");
            }
        }
        // builder 侧同语义
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        LongEntry l = b.longEntry(5L);
        PoolEntry after = b.intEntry(6);
        check(after.index() == l.index() + 2, "builder: next entry skips long phantom slot");
        check(b.size() == after.index() + 1, "builder size == maxIndex + 1");
    }

    static void testIndexErrorMatrix() throws Exception {
        byte[] self = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int[] bad = {0, -1, cp.size(), cp.size() + 7};
        for (int idx : bad) {
            try {
                cp.entryByIndex(idx);
                check(false, "entryByIndex(" + idx + ") must throw");
            } catch (ConstantPoolException e) {
                check(e.getMessage() != null && e.getMessage().contains("Bad CP index"), "Bad CP index message: " + e.getMessage());
                check(e instanceof IllegalArgumentException, "ConstantPoolException is an IllegalArgumentException");
            }
        }
        // 类型不符的 typed 查找
        int classIdx = cm.thisClass().index();
        try {
            cp.entryByIndex(classIdx, Utf8Entry.class);
            check(false, "typed mismatch must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "typed mismatch message non-null");
        }
        // 超类型接口的 typed 查找合法
        boolean ok = false;
        for (PoolEntry pe : cp) if (pe instanceof IntegerEntry ie) {
            ok = cp.entryByIndex(ie.index(), LoadableConstantEntry.class) == ie;
            break;
        }
        check(ok, "typed lookup accepts supertype interfaces");
    }

    static void testIteratorContract() throws Exception {
        byte[] self = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        Iterator<PoolEntry> it1 = cp.iterator();
        Iterator<PoolEntry> it2 = cp.iterator();
        check(it1.hasNext() && it2.hasNext(), "fresh iterators have elements");
        check(it1.next() == it2.next(), "independent iterators start at same entry");
        try {
            it1.next();
            it1.remove();
            check(false, "iterator.remove must throw");
        } catch (UnsupportedOperationException e) {
            check(true, "iterator.remove unsupported");
        }
    }

    static void testBootstrapMethodsFace() throws Exception {
        byte[] self = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        check(cp.bootstrapMethodCount() > 0, "lambda-bearing class has bootstrap methods");
        boolean sawLmf = false;
        for (int i = 0; i < cp.bootstrapMethodCount(); i++) {
            BootstrapMethodEntry be = cp.bootstrapMethodEntry(i);
            check(be.bsmIndex() == i, "bsmIndex matches position");
            check(be.constantPool() == cp, "bsm entry bound to same pool");
            check(be.bootstrapMethod() instanceof MethodHandleEntry, "bootstrap method is a MethodHandleEntry");
            check(be.arguments() != null, "arguments list non-null");
            if (be.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/LambdaMetafactory")) sawLmf = true;
        }
        check(sawLmf, "a LambdaMetafactory bootstrap method is present (LAMBDA_R)");
        int n = cp.bootstrapMethodCount();
        try {
            cp.bootstrapMethodEntry(n);
            check(false, "bsm out of range must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "bsm oob CPE message non-null");
        }
        try {
            cp.bootstrapMethodEntry(-1);
            check(false, "bsm negative must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "bsm negative CPE message non-null");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testRealPoolFace();
        testDoubleSlotSemantics();
        testIndexErrorMatrix();
        testIteratorContract();
        testBootstrapMethodsFace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
