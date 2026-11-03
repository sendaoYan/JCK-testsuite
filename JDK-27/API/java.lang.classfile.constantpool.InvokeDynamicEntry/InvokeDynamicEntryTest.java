/*
 * @test
 * @summary java.lang.classfile.constantpool.InvokeDynamicEntry conformance: white-box structure + black-box behavior.
 * @run main InvokeDynamicEntryTest
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

public class InvokeDynamicEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.InvokeDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.InvokeDynamicEntry"), "binary name java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.InvokeDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.InvokeDynamicEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.InvokeDynamicEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
        check(declares(c, "asSymbol"), "declares asSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        InvokeDynamicEntry ide = cpb.invokeDynamicEntry(bsm, nat);
        check(ide.bootstrap() != null, "bootstrap");
        check(ide.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(ide.nameAndType().name().stringValue().equals("m"), "nameAndType name");
        check(ide.name().stringValue().equals("m"), "name default");
        check(ide.type().stringValue().equals("()V"), "type default");
        check(ide.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_void)), "typeSymbol");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
