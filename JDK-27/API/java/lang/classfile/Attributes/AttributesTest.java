/*
 * @test
 * @summary java.lang.classfile.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
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

public class AttributesTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Attributes");
        check(c != null, "java.lang.classfile.Attributes loads");
        check(c.getName().equals("java.lang.classfile.Attributes"), "binary name java.lang.classfile.Attributes");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Attributes is public");
        check(!c.isInterface() && !c.isEnum(), "java.lang.classfile.Attributes is a class");
        check(declares(c, "annotationDefault"), "declares annotationDefault");
        check(declares(c, "bootstrapMethods"), "declares bootstrapMethods");
        check(declares(c, "characterRangeTable"), "declares characterRangeTable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(Attributes.NAME_CODE.equals("Code"), "NAME_CODE");
        check(Attributes.NAME_SOURCE_FILE.equals("SourceFile"), "NAME_SOURCE_FILE");
        check(Attributes.NAME_SIGNATURE.equals("Signature"), "NAME_SIGNATURE");
        check(Attributes.NAME_CONSTANT_VALUE.equals("ConstantValue"), "NAME_CONSTANT_VALUE");
        check(Attributes.NAME_STACK_MAP_TABLE.equals("StackMapTable"), "NAME_STACK_MAP_TABLE");
        check(Attributes.code().name().equals("Code"), "code mapper name");
        check(Attributes.sourceFile().name().equals("SourceFile"), "sourceFile mapper");
        check(Attributes.signature().name().equals("Signature"), "signature mapper");
        check(Attributes.constantValue().name().equals("ConstantValue"), "constantValue mapper");
        check(Attributes.synthetic().name().equals("Synthetic"), "synthetic mapper");
        check(Attributes.stackMapTable().name().equals("StackMapTable"), "stackMapTable mapper");
        check(Attributes.sourceId().name().equals("SourceID"), "sourceId mapper");
        check(Attributes.compilationId().name().equals("CompilationID"), "compilationId mapper");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
