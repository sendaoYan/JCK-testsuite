/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPoolBuilder conformance: white-box structure + black-box behavior.
 * @run main ConstantPoolBuilderTest
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

public class ConstantPoolBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantPoolBuilder");
        check(c != null, "java.lang.classfile.constantpool.ConstantPoolBuilder loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantPoolBuilder"), "binary name java.lang.classfile.constantpool.ConstantPoolBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantPoolBuilder is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantPoolBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantPoolBuilder has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "canWriteDirect"), "declares canWriteDirect");
        check(declares(c, "utf8Entry"), "declares utf8Entry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("hello");
        ClassEntry cl = cpb.classEntry(ClassDesc.of("java.lang.String"));
        IntegerEntry ie = cpb.intEntry(42);
        LongEntry le = cpb.longEntry(1L << 40);
        StringEntry se = cpb.stringEntry("hi");
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        MethodRefEntry mre = cpb.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.of(ConstantDescs.CD_String));
        check(u.index() == 1, "utf8 index 1");
        check(cl.asInternalName().equals("java/lang/String"), "class internal");
        check(ie.intValue() == 42, "int value");
        check(le.longValue() == (1L << 40), "long value");
        check(se.stringValue().equals("hi"), "string value");
        check(nat.name().stringValue().equals("m") && nat.type().stringValue().equals("()V"), "nameAndType");
        check(mre.name().stringValue().equals("toString"), "methodRef name");
        check(cpb.size() > 10, "size grows");
        check(cpb.canWriteDirect(ConstantPoolBuilder.of()) == false, "canWriteDirect false for fresh");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
