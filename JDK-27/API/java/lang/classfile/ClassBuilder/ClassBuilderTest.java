/*
 * @test
 * @summary java.lang.classfile.ClassBuilder conformance: white-box structure + black-box behavior.
 * @run main ClassBuilderTest
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

public class ClassBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassBuilder");
        check(c != null, "java.lang.classfile.ClassBuilder loads");
        check(c.getName().equals("java.lang.classfile.ClassBuilder"), "binary name java.lang.classfile.ClassBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassBuilder is public");
        check(c.isInterface(), "java.lang.classfile.ClassBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassBuilder has no superclass");
        check(declares(c, "withVersion"), "declares withVersion");
        check(declares(c, "withFlags"), "declares withFlags");
        check(declares(c, "withSuperclass"), "declares withSuperclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL).withField("f", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.return_()).withInterfaceSymbols(ClassDesc.of("java.io.Serializable")));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL), "flags");
        check(cm.interfaces().size() == 1 && cm.interfaces().get(0).asSymbol().equals(ClassDesc.of("java.io.Serializable")), "interface");
        check(cm.fields().get(0).fieldType().stringValue().equals("I"), "field type");
        check(cm.methods().get(0).code().isPresent(), "method code");
        check(cm.constantPool().size() > 0, "constant pool");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
