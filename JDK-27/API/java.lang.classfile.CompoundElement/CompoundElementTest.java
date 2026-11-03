/*
 * @test
 * @summary java.lang.classfile.CompoundElement conformance: white-box structure + black-box behavior.
 * @run main CompoundElementTest
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

public class CompoundElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CompoundElement");
        check(c != null, "java.lang.classfile.CompoundElement loads");
        check(c.getName().equals("java.lang.classfile.CompoundElement"), "binary name java.lang.classfile.CompoundElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CompoundElement is public");
        check(c.isInterface(), "java.lang.classfile.CompoundElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CompoundElement has no superclass");
        check(declares(c, "forEach"), "declares forEach");
        check(declares(c, "iterator"), "declares iterator");
        check(declares(c, "elementStream"), "declares elementStream");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("a", ConstantDescs.CD_int, 0).withField("b", ConstantDescs.CD_int, 0).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()));
        ClassModel cm = ClassFile.of().parse(b);
        java.util.List<FieldModel> fl = cm.elementList().stream().filter(e -> e instanceof FieldModel).map(e -> (FieldModel) e).toList();
        check(fl.size() == 2, "two fields via elementList");
        java.util.stream.Stream<ClassElement> st = cm.elementStream();
        check(st.count() >= 4, "elementStream count");
        check(cm.toDebugString().length() > 0, "toDebugString nonempty");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
