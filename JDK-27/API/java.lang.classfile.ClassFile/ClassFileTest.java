/*
 * @test
 * @summary java.lang.classfile.ClassFile conformance: white-box structure + black-box behavior.
 * @run main ClassFileTest
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

public class ClassFileTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile");
        check(c != null, "java.lang.classfile.ClassFile loads");
        check(c.getName().equals("java.lang.classfile.ClassFile"), "binary name java.lang.classfile.ClassFile");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFile has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "withOptions"), "declares withOptions");
        check(declares(c, "parse"), "declares parse");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile cf = ClassFile.of();
        check(ClassFile.MAGIC_NUMBER == 0xCAFEBABE, "MAGIC_NUMBER");
        check(ClassFile.ACC_PUBLIC == 0x0001 && ClassFile.ACC_PRIVATE == 0x0002 && ClassFile.ACC_STATIC == 0x0008 && ClassFile.ACC_FINAL == 0x0010 && ClassFile.ACC_INTERFACE == 0x0200, "ACC_* constants");
        check(ClassFile.JAVA_1_VERSION == 45, "JAVA_1_VERSION 45");
        check(ClassFile.JAVA_27_VERSION == 71, "JAVA_27_VERSION 71");
        check(ClassFile.PREVIEW_MINOR_VERSION == 0xFFFF, "PREVIEW_MINOR 0xFFFF");
        check(ClassFile.latestMajorVersion() == 71, "latestMajorVersion");
        check(ClassFile.latestMinorVersion() == 0, "latestMinorVersion");
        byte[] b = cf.build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_1().ireturn()));
        check(b.length > 8, "build produces bytes");
        int magic = ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff);
        check(magic == 0xCAFEBABE, "magic in bytes");
        ClassModel cm = cf.parse(b);
        check("pkg/T".equals(cm.thisClass().asInternalName()), "parse thisClass");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "parse superclass");
        check(cm.fields().size() == 1 && cm.methods().size() == 1, "field+method count");
        check(cf.verify(cm).isEmpty(), "verify clean");
        ClassFile cf2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG);
        check(cf2 != cf, "withOptions new instance");
        byte[] b2 = cf2.build(ClassDesc.of("pkg.U"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")));
        check(cf2.parse(b2).majorVersion() == 71, "cf2 still parses");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
