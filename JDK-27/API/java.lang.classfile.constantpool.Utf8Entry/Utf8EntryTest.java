/*
 * @test
 * @summary java.lang.classfile.constantpool.Utf8Entry conformance: white-box structure + black-box behavior.
 * @run main Utf8EntryTest
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

public class Utf8EntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.Utf8Entry");
        check(c != null, "java.lang.classfile.constantpool.Utf8Entry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.Utf8Entry"), "binary name java.lang.classfile.constantpool.Utf8Entry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.Utf8Entry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.Utf8Entry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.Utf8Entry has no superclass");
        check(declares(c, "stringValue"), "declares stringValue");
        check(declares(c, "equalsString"), "declares equalsString");
        check(declares(c, "isFieldType"), "declares isFieldType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("hello");
        check(u.stringValue().equals("hello"), "stringValue");
        check(u.equalsString("hello") && !u.equalsString("bye"), "equalsString");
        check(u.length() == 5, "length 5");
        check(u.charAt(0) == 'h', "charAt");
        check(u.toString().equals("hello"), "toString");
        check(u.isFieldType(ConstantDescs.CD_int) == false, "isFieldType false for hello");
        Utf8Entry ui = cpb.utf8Entry(ClassDesc.of("java.lang.String"));
        check(ui.stringValue().equals("Ljava/lang/String;"), "utf8 from ClassDesc descriptor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
