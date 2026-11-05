/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPoolException conformance: white-box structure + black-box behavior.
 * @run main ConstantPoolExceptionTest
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

public class ConstantPoolExceptionTest {
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

    static final long CONST_LONG = 0x123456789ABCDEFL;
    static final java.lang.Runnable LAMBDA_R = () -> {};

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantPoolException");
        check(c != null, "java.lang.classfile.constantpool.ConstantPoolException loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantPoolException"), "binary name java.lang.classfile.constantpool.ConstantPoolException");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantPoolException is public");
        check(!c.isInterface() && !c.isEnum(), "java.lang.classfile.constantpool.ConstantPoolException is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // direct constructor invocations (all four public constructors)
        ConstantPoolException a = new ConstantPoolException();
        check(a.getMessage() == null, "no-arg constructor leaves message null");
        ConstantPoolException b = new ConstantPoolException("boom");
        check("boom".equals(b.getMessage()), "(String) constructor preserves message");
        Throwable cause = new IllegalStateException("cause");
        ConstantPoolException c = new ConstantPoolException(cause);
        check(cause == c.getCause(), "(Throwable) constructor sets cause");
        ConstantPoolException d = new ConstantPoolException("boom2", cause);
        check("boom2".equals(d.getMessage()) && cause == d.getCause(), "(String,Throwable) constructor sets message+cause");
        check(RuntimeException.class.isAssignableFrom(ConstantPoolException.class), "extends RuntimeException");

        // still reachable through the classfile API for an invalid constant-pool index
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")));
        ConstantPool cp = ClassFile.of().parse(bytes).constantPool();
        boolean thrown = false;
        try { cp.entryByIndex(0); } catch (ConstantPoolException e) { thrown = true; check(e.getMessage() != null, "message non-null"); }
        check(thrown, "thrown for invalid index");
    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ConstantPoolException 契约（精确消息族 + 触发面全覆盖） ----
    static void testExceptionFamilyMatrix() throws Exception {
        byte[] self = ConstantPoolExceptionTest.class.getResourceAsStream("ConstantPoolExceptionTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        // Bad CP index 家族
        for (int idx : new int[]{0, -1, cp.size()}) {
            try {
                cp.entryByIndex(idx);
                check(false, "entryByIndex(" + idx + ") must throw");
            } catch (ConstantPoolException e) {
                check(("Bad CP index: " + idx).equals(e.getMessage()), "exact message: " + e.getMessage());
            }
        }
        // Unusable CP index（双槽幽灵位）
        boolean phantomChecked = false;
        for (PoolEntry pe : cp) if (pe instanceof LongEntry && !phantomChecked) {
            try {
                cp.entryByIndex(pe.index() + 1);
                check(false, "phantom slot must throw");
            } catch (ConstantPoolException e) {
                check(e.getMessage() != null, "unusable slot message non-null");
                phantomChecked = true;
            }
        }
        check(phantomChecked, "CONST_LONG provides a phantom slot to probe");
        // Not a X at index 家族（typed 查找类型不符）
        int classIdx = cm.thisClass().index();
        try {
            cp.entryByIndex(classIdx, Utf8Entry.class);
            check(false, "typed mismatch must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Not a "), "typed mismatch message prefix: " + e.getMessage());
        }
        // Bad BSM index 家族
        int n = cp.bootstrapMethodCount();
        check(n > 0, "lambda-bearing class has bootstrap methods");
        try {
            cp.bootstrapMethodEntry(n);
            check(false, "bsm oob must throw");
        } catch (ConstantPoolException e) {
            check(("Bad BSM index: " + n).equals(e.getMessage()), "exact bsm message: " + e.getMessage());
        }
        // 类型 + 层次 + 可序列化
        ConstantPoolException probe = new ConstantPoolException("p");
        check(probe instanceof IllegalArgumentException, "is IllegalArgumentException");
        check(probe instanceof RuntimeException, "is RuntimeException");
        check(probe instanceof java.io.Serializable, "is Serializable");
        // cause/message 保真（四构造器形态由 testBlackBox 覆盖，这里补 suppressed 语义）
        check(probe.getSuppressed().length == 0, "no suppressed exceptions initially");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testExceptionFamilyMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
