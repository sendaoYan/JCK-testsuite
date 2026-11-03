/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableTypeInfo conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTypeInfoTest
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

public class LocalVariableTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(c != null, "java.lang.classfile.attribute.LocalVariableTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableTypeInfo"), "binary name java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableTypeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LocalVariableTypeInfo has no superclass");
        check(declares(c, "startPc"), "declares startPc");
        check(declares(c, "length"), "declares length");
        check(declares(c, "name"), "declares name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariableType(2, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e).labelBinding(s).labelBinding(e).return_();
        }));
        LocalVariableTypeInfo i = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().get(0);
        check(i.name().stringValue().equals("x"), "name");
        check(i.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "signature");
        check(i.slot() == 2, "slot");
        check(i.startPc() >= 0 && i.length() >= 0, "startPc/length");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
