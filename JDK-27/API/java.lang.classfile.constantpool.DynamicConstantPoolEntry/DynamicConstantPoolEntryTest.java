/*
 * @test
 * @summary java.lang.classfile.constantpool.DynamicConstantPoolEntry conformance: white-box structure + black-box behavior.
 * @run main DynamicConstantPoolEntryTest
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

public class DynamicConstantPoolEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(c != null, "java.lang.classfile.constantpool.DynamicConstantPoolEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.DynamicConstantPoolEntry"), "binary name java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.DynamicConstantPoolEntry has no superclass");
        check(declares(c, "bootstrap"), "declares bootstrap");
        check(declares(c, "bootstrapMethodIndex"), "declares bootstrapMethodIndex");
        check(declares(c, "nameAndType"), "declares nameAndType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        DynamicConstantPoolEntry d = cpb.invokeDynamicEntry(bsm, nat);
        check(d.bootstrap() != null, "bootstrap");
        check(d.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(d.nameAndType().name().stringValue().equals("m"), "nameAndType");
        check(d.name().stringValue().equals("m") && d.type().stringValue().equals("()V"), "name+type defaults");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
