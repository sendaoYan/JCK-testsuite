/*
 * @test
 * @summary JVMS 4.4: the constant pool is an array of typed structures
 *          (CONSTANT_Utf8, Class, Fieldref, Methodref, Integer, NameAndType,
 *          etc.).  A class that uses a field and a method must contain the
 *          corresponding entry kinds.
 * @run main ConstantPoolTest
 */

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.Utf8Entry;

public class ConstantPoolTest {
    static int field = 42;

    public static int method(int x) { return x + field; }

    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = ConstantPoolTest.class.getResourceAsStream("ConstantPoolTest.class")) {
            b = in.readAllBytes();
        }

        ClassModel cm = ClassFile.of().parse(b);
        ConstantPool cp = cm.constantPool();

        boolean utf8 = false, cls = false, fieldref = false, methodref = false;
        boolean nameAndType = false;
        for (PoolEntry e : cp) {
            if (e instanceof Utf8Entry) utf8 = true;
            else if (e instanceof ClassEntry) cls = true;
            else if (e instanceof FieldRefEntry) fieldref = true;
            else if (e instanceof MethodRefEntry) methodref = true;
            else if (e instanceof NameAndTypeEntry) nameAndType = true;
        }

        if (!utf8) throw new AssertionError("missing CONSTANT_Utf8");
        if (!cls) throw new AssertionError("missing CONSTANT_Class");
        if (!fieldref) throw new AssertionError("missing CONSTANT_Fieldref");
        if (!methodref) throw new AssertionError("missing CONSTANT_Methodref");
        if (!nameAndType) throw new AssertionError("missing CONSTANT_NameAndType");

        System.out.println("PASS");
    }
}
