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


    // ---- Part 3: 类型标记层次 —— 四个模型都是 CompoundElement ----
    @SuppressWarnings("rawtypes")
    static void testHierarchy() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.HE"), cb -> cb
                .withField("f", ConstantDescs.CD_int, 0)
                .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> c.nop().return_()));
        ClassModel cm = cf.parse(b);
        MethodModel mm = cm.methods().get(0);
        FieldModel fm = cm.fields().get(0);
        CodeModel code = mm.code().get();
        check(CompoundElement.class.isInstance(cm), "ClassModel is CompoundElement");
        check(CompoundElement.class.isInstance(mm), "MethodModel is CompoundElement");
        check(CompoundElement.class.isInstance(fm), "FieldModel is CompoundElement");
        check(CompoundElement.class.isInstance(code), "CodeModel is CompoundElement");
        check(CompoundElement.class.isAssignableFrom(ClassModel.class), "CompoundElement <- ClassModel assignability");
        check(Iterable.class.isAssignableFrom(CompoundElement.class), "CompoundElement extends Iterable");
        check(ClassFileElement.class.isAssignableFrom(CompoundElement.class), "CompoundElement extends ClassFileElement");
        check(cm instanceof java.lang.classfile.CompoundElement<?>, "raw CompoundElement wildcard");
    }

    // ---- Part 4: 迭代/流/列表三方一致 + forEach + toDebugString ----
    static void testIterationConsistency() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.IT"), cb -> cb
                .withField("a", ConstantDescs.CD_int, 0)
                .withField("bb", ConstantDescs.CD_int, 0)
                .withField("ccc", ConstantDescs.CD_int, 0)
                .withMethodBody("m1", ConstantDescs.MTD_void, 0, c -> c.return_())
                .withMethodBody("m2", ConstantDescs.MTD_void, 0, c -> c.nop().return_()));
        ClassModel cm = cf.parse(b);
        List<ClassElement> list = cm.elementList();
        check(list.size() >= 7, "elementList size >= 7");
        int byForEach = 0;
        for (ClassElement e : cm) byForEach++;                      // Iterable for-each
        check(byForEach == list.size(), "for-each == elementList size");
        final int[] cnt = {0};
        cm.forEach(e -> cnt[0]++);                                  // forEach(Consumer)
        check(cnt[0] == list.size(), "forEach count == elementList size");
        long byStream = cm.elementStream().count();                 // elementStream
        check(byStream == list.size(), "elementStream count == elementList size");
        java.util.stream.Stream<ClassElement> s2 = cm.elementStream();
        long fieldsInStream = s2.filter(e -> e instanceof FieldModel).count();
        check(fieldsInStream == 3, "elementStream 3 FieldModel");
        // 两个独立 iterator 各自完整迭代
        Iterator<ClassElement> it1 = cm.iterator(), it2 = cm.iterator();
        check(it1 != it2, "iterator() returns fresh iterators");
        int c1 = 0, c2 = 0;
        while (it1.hasNext()) { it1.next(); c1++; }
        while (it2.hasNext()) { it2.next(); c2++; }
        check(c1 == c2 && c1 == list.size(), "two iterators both full (" + c1 + "," + c2 + ")");
        check(it1.hasNext() == false, "exhausted iterator hasNext false");
        // elementList 顺序稳定：两次获取同序列
        List<ClassElement> again = cm.elementList();
        check(again.size() == list.size(), "elementList stable size");
        for (int i = 0; i < list.size(); i++) check(again.get(i).getClass() == list.get(i).getClass(), "elementList stable element type at " + i);
        // toDebugString
        String dbg = cm.toDebugString();
        check(dbg != null && !dbg.isEmpty(), "toDebugString nonempty");
        check(dbg.contains("IT"), "toDebugString contains class name");
        // CodeModel 迭代计 Instruction
        CodeModel code = cm.methods().get(0).code().get();
        int instr = 0;
        for (CodeElement e : code) if (Instruction.class.isInstance(e)) instr++;
        check(code.elementList().size() == instr, "CodeModel iteration all-Instruction here (" + instr + ")");
        check(cf.verify(cm).isEmpty(), "verify clean");
    }

    // ---- Part 5: defineClass 运行闭环 ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynCompoundElement" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withField("F", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC)
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iconst_3().iconst_4().iadd().ireturn()));
        ClassModel rm = cf.parse(b);
        int f = 0, m = 0;
        for (ClassElement e : rm) { if (e instanceof FieldModel) f++; if (e instanceof MethodModel) m++; }
        check(f == 1 && m == 1, "dyn element counts via for-each (1 field,1 method)");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("go").invoke(null).equals(7), "dyn go()==7 运行闭环");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchy();
        testIterationConsistency();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
