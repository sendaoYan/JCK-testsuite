/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantDynamicEntry conformance: white-box structure + black-box behavior.
 * @run main ConstantDynamicEntryTest
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

public class ConstantDynamicEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.ConstantDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantDynamicEntry"), "binary name java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantDynamicEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantDynamicEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "asSymbol"), "declares asSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("x", ConstantDescs.CD_int);
        ConstantDynamicEntry cde = cpb.constantDynamicEntry(bsm, nat);
        check(cde.bootstrap() != null, "bootstrap");
        check(cde.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(cde.name().stringValue().equals("x"), "name");
        check(cde.type().stringValue().equals("I"), "type");
        check(cde.typeSymbol().equals(ConstantDescs.CD_int), "typeSymbol");
        check(cde.typeKind() == TypeKind.INT, "typeKind INT");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
