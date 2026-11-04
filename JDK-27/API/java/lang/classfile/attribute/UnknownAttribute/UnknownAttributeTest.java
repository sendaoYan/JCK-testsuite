/*
 * @test
 * @summary java.lang.classfile.attribute.UnknownAttribute conformance: white-box structure + black-box behavior.
 * @run main UnknownAttributeTest
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

public class UnknownAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.UnknownAttribute");
        check(c != null, "java.lang.classfile.attribute.UnknownAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.UnknownAttribute"), "binary name java.lang.classfile.attribute.UnknownAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.UnknownAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.UnknownAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.UnknownAttribute has no superclass");
        check(declares(c, "contents"), "declares contents");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA> mapper = new AttributeMapper<CA>() {
            public String name() { return "MyAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(a.attributeName()); bw.writeInt(2); bw.writeU2(0xCAFE); }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA(mapper)));
        UnknownAttribute ua = null;
        for (Attribute<?> a : ClassFile.of().parse(b).attributes())
            if (a instanceof UnknownAttribute x && x.attributeName().stringValue().equals("MyAttr")) ua = x;
        check(ua != null, "unknown attribute parsed");
        check(ua.contents().length == 2, "contents length 2");
        check((ua.contents()[0] & 0xff) == 0xCA && (ua.contents()[1] & 0xff) == 0xFE, "contents CAFE");
        check(ua.attributeName().stringValue().equals("MyAttr"), "attributeName");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
