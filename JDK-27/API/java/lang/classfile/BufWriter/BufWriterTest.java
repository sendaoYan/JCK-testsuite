/*
 * @test
 * @summary java.lang.classfile.BufWriter conformance: white-box structure + black-box behavior.
 * @run main BufWriterTest
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

public class BufWriterTest {
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
        Class<?> c = Class.forName("java.lang.classfile.BufWriter");
        check(c != null, "java.lang.classfile.BufWriter loads");
        check(c.getName().equals("java.lang.classfile.BufWriter"), "binary name java.lang.classfile.BufWriter");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.BufWriter is public");
        check(c.isInterface(), "java.lang.classfile.BufWriter is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.BufWriter has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "canWriteDirect"), "declares canWriteDirect");
        check(declares(c, "reserveSpace"), "declares reserveSpace");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA3> mapper = new AttributeMapper<CA3>() {
            public String name() { return "MyAttr"; }
            public CA3 readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA3(this); }
            public void writeAttribute(BufWriter bw, CA3 a) {
                cap2[0] = bw;
                check(bw.constantPool() != null, "constantPool");
                check(bw.canWriteDirect(bw.constantPool()), "canWriteDirect");
                int before = bw.size();
                bw.writeIndex(a.attributeName());
                bw.writeInt(2);
                bw.writeBytes(new byte[]{1,2});
                check(bw.size() == before + 8, "attribute name+length+content");
                bw.writeU1(7);
                check(bw.size() == before + 9, "writeU1 +1");
                bw.writeU2(300);
                check(bw.size() == before + 11, "writeU2 +2");
                bw.writeInt(70000);
                check(bw.size() == before + 15, "writeInt +4");
                bw.writeBytes(new byte[]{9,9});
                check(bw.size() == before + 17, "writeBytes +2");
            }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA3(mapper)));
        check(cap2[0] != null, "BufWriter captured");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
