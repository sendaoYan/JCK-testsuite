/*
 * @test
 * @summary java.lang.classfile.constantpool.PoolEntry conformance: white-box structure + black-box behavior.
 * @run main PoolEntryTest
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

public class PoolEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.PoolEntry");
        check(c != null, "java.lang.classfile.constantpool.PoolEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.PoolEntry"), "binary name java.lang.classfile.constantpool.PoolEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.PoolEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.PoolEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.PoolEntry has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "tag"), "declares tag");
        check(declares(c, "index"), "declares index");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        check(PoolEntry.TAG_UTF8 == 1 && PoolEntry.TAG_CLASS == 7 && PoolEntry.TAG_STRING == 8 && PoolEntry.TAG_INTEGER == 3 && PoolEntry.TAG_LONG == 5 && PoolEntry.TAG_DOUBLE == 6 && PoolEntry.TAG_FLOAT == 4, "TAG constants");
        PoolEntry u = cpb.utf8Entry("x");
        check(u.tag() == PoolEntry.TAG_UTF8, "utf8 tag");
        check(u.index() == 1, "index");
        check(u.width() == 1, "utf8 width 1");
        PoolEntry l = cpb.longEntry(5L);
        check(l.width() == 2, "long width 2");
        check(u.constantPool() != null, "constantPool backref");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
