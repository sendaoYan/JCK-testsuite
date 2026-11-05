/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo conformance: white-box structure + black-box behavior.
 * @run main VerificationTypeInfoTest
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

public class VerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo has no superclass");
        check(declares(c, "tag"), "declares tag");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_TOP == 0, "ITEM_TOP 0");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_INTEGER == 1, "ITEM_INTEGER 1");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_OBJECT == 7, "ITEM_OBJECT 7");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_UNINITIALIZED == 8, "ITEM_UNINITIALIZED 8");

    }


    // ---- real-product member（混合 simple/object/uninit 的方法）----
    static class VTReal {
        VTReal(int i) {}
        static Object g(boolean c, String s) { Object o = s; if (c) o = new VTReal(c ? 1 : 2); return o; }
        static int h(boolean c, int a) { int r = a; if (c) r++; return r; }
    }

    // ---- Part 3: ITEM_* 常量表（JVMS 4.7.4，0..8 顺序）----
    static void testItemConstants() {
        var V = StackMapFrameInfo.VerificationTypeInfo.class;
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_TOP == 0, "ITEM_TOP 0");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_INTEGER == 1, "ITEM_INTEGER 1");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_FLOAT == 2, "ITEM_FLOAT 2");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_DOUBLE == 3, "ITEM_DOUBLE 3");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_LONG == 4, "ITEM_LONG 4");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_NULL == 5, "ITEM_NULL 5");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_UNINITIALIZED_THIS == 6, "ITEM_UNINITIALIZED_THIS 6");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_OBJECT == 7, "ITEM_OBJECT 7");
        check(StackMapFrameInfo.VerificationTypeInfo.ITEM_UNINITIALIZED == 8, "ITEM_UNINITIALIZED 8");
        int cnt = 0;
        for (java.lang.reflect.Field fld : V.getFields())
            if (fld.getName().startsWith("ITEM_")) cnt++;
        check(cnt == 9, "9 ITEM constants: " + cnt);
    }

    // ---- Part 4: tag() 多态 + 三子类型 instanceof 链（javac 真产物）----
    static void testPolymorphicTags() throws Exception {
        byte[] rb = VerificationTypeInfoTest.class.getResourceAsStream("VerificationTypeInfoTest$VTReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        boolean sawSimple = false, sawObject = false, sawUninit = false;
        for (MethodModel mm : m.methods()) {
            if (mm.code().isEmpty()) continue;
            CodeAttribute ca = (CodeAttribute) mm.code().get();
            var smt = ca.findAttribute(Attributes.stackMapTable());
            if (smt.isEmpty()) continue;
            for (StackMapFrameInfo f : smt.get().entries()) {
                for (StackMapFrameInfo.VerificationTypeInfo v : f.locals()) {
                    checkTag(v);
                    if (v instanceof StackMapFrameInfo.SimpleVerificationTypeInfo) sawSimple = true;
                    if (v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo) sawObject = true;
                }
                for (StackMapFrameInfo.VerificationTypeInfo v : f.stack()) {
                    checkTag(v);
                    if (v instanceof StackMapFrameInfo.SimpleVerificationTypeInfo) sawSimple = true;
                    if (v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo) sawObject = true;
                    if (v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo) sawUninit = true;
                }
            }
        }
        check(sawSimple, "simple vti in real frames");
        check(sawObject, "object vti in real frames");
        check(sawUninit, "uninit vti in real frames");
    }
    static void checkTag(StackMapFrameInfo.VerificationTypeInfo v) {
        int t = v.tag();
        check(t >= 0 && t <= 8, "tag in 0..8: " + t);
        if (t <= 6) check(v instanceof StackMapFrameInfo.SimpleVerificationTypeInfo, "tag<=6 simple");
        else if (t == 7) check(v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo, "tag 7 object");
        else check(v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo, "tag 8 uninit");
    }

    // ---- Part 5: 密封层次（3 个 permits 子接口）----
    static void testSealedHierarchy() {
        Class<?>[] perm = StackMapFrameInfo.VerificationTypeInfo.class.getPermittedSubclasses();
        check(perm != null && perm.length == 3, "sealed permits 3: " + (perm == null ? -1 : perm.length));
        if (perm != null) {
            Set<String> names = new HashSet<>();
            for (Class<?> c : perm) names.add(c.getSimpleName());
            check(names.equals(Set.of("SimpleVerificationTypeInfo", "ObjectVerificationTypeInfo", "UninitializedVerificationTypeInfo")),
                    "permits names: " + names);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testItemConstants();
        testPolymorphicTags();
        testSealedHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
