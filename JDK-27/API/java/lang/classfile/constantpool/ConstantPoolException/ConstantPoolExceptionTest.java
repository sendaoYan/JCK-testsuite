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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
