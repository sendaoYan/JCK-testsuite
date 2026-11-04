/*
 * @test
 * @summary java.lang.classfile.CustomAttribute conformance: white-box structure + black-box behavior.
 * @run main CustomAttributeTest
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

public class CustomAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CustomAttribute");
        check(c != null, "java.lang.classfile.CustomAttribute loads");
        check(c.getName().equals("java.lang.classfile.CustomAttribute"), "binary name java.lang.classfile.CustomAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CustomAttribute is public");
        check(!c.isInterface() && !c.isEnum(), "java.lang.classfile.CustomAttribute is a class");
        check(declares(c, "attributeMapper"), "declares attributeMapper");
        check(declares(c, "attributeName"), "declares attributeName");
        check(declares(c, "toString"), "declares toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA> mapper = new AttributeMapper<CA>() {
            public String name() { return "MyAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(a.attributeName()); bw.writeInt(3); bw.writeBytes(new byte[]{1,2,3}); }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        CA a = new CA(mapper);
        check(a.attributeName().stringValue().equals("MyAttr"), "attributeName");
        check(a.attributeMapper() == mapper, "attributeMapper");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(a));
        check(ClassFile.of(ClassFile.AttributeMapperOption.of(u -> { return u.equalsString("MyAttr") ? mapper : null; })).parse(b).findAttribute(mapper).isPresent(), "custom attr round-trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
