/*
 * @test
 * @summary java.lang.classfile.ClassReader conformance: white-box structure + black-box behavior.
 * @run main ClassReaderTest
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

public class ClassReaderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassReader");
        check(c != null, "java.lang.classfile.ClassReader loads");
        check(c.getName().equals("java.lang.classfile.ClassReader"), "binary name java.lang.classfile.ClassReader");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassReader is public");
        check(c.isInterface(), "java.lang.classfile.ClassReader is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassReader has no superclass");
        check(declares(c, "customAttributes"), "declares customAttributes");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "thisClassEntry"), "declares thisClassEntry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA2> mapper = new AttributeMapper<CA2>() {
            public String name() { return "MyAttr"; }
            public CA2 readAttribute(AttributedElement e, ClassReader cr, int p) { cap[0] = cr; return new CA2(this); }
            public void writeAttribute(BufWriter bw, CA2 a) { bw.writeIndex(a.attributeName()); bw.writeInt(3); bw.writeBytes(new byte[]{1,2,3}); }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA2(mapper)));
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> { return u.equalsString("MyAttr") ? mapper : null; }));
        ClassModel cm = cf.parse(b);
        cm.attributes();
        check(cap[0] != null, "ClassReader captured");
        ClassReader cr = cap[0];
        check(cr.classfileLength() > 0, "classfileLength");
        check(cr.thisClassEntry().asInternalName().equals("pkg/T"), "thisClassEntry");
        check(cr.superclassEntry().isPresent(), "superclassEntry present");
        check(cr.flags() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flags PUBLIC|SUPER");
        check(cr.customAttributes() != null, "customAttributes non-null");
        check(cr.readInt(0) == 0xCAFEBABE, "readInt magic");
        check(cr.readU1(0) == 0xCA, "readU1 magic");
        check(cr.readU2(4) == 0, "readU2 minor");
        check(cr.readU2(8) >= 1, "readU2 constant pool count");
        check(cr.readBytes(0, 4).length == 4, "readBytes magic");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
