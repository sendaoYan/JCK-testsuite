/*
 * @test
 * @summary java.lang.classfile.attribute.BootstrapMethodsAttribute conformance: white-box structure + black-box behavior.
 * @run main BootstrapMethodsAttributeTest
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

public class BootstrapMethodsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(c != null, "java.lang.classfile.attribute.BootstrapMethodsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.BootstrapMethodsAttribute"), "binary name java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.BootstrapMethodsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.BootstrapMethodsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.BootstrapMethodsAttribute has no superclass");
        check(declares(c, "bootstrapMethods"), "declares bootstrapMethods");
        check(declares(c, "bootstrapMethodsSize"), "declares bootstrapMethodsSize");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
            NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_int));
            InvokeDynamicEntry ide = cpb.invokeDynamicEntry(bsm, nat);
            cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.invokedynamic(ide).ireturn());
        });
        BootstrapMethodsAttribute a = ClassFile.of().parse(b).findAttribute(Attributes.bootstrapMethods()).get();
        check(a.bootstrapMethodsSize() == 1, "bootstrapMethodsSize");
        check(a.bootstrapMethods().size() == 1, "bootstrapMethods size");
        check(a.bootstrapMethods().get(0).bootstrapMethod().reference().name().stringValue().equals("valueOf"), "bsm ref name");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
