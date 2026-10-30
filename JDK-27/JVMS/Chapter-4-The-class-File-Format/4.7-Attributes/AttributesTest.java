/*
 * @test
 * @summary JVMS 4.7: attributes carry per-class, per-field, per-method, and
 *          per-code information.  A method body is stored in a Code attribute,
 *          and a method with branches carries a StackMapTable attribute.
 * @run main AttributesTest
 */

import java.io.InputStream;
import java.lang.classfile.Attribute;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;

public class AttributesTest {
    public static int compute(int a, int b) {
        if (a > b) return a; else return b;
    }

    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = AttributesTest.class.getResourceAsStream("AttributesTest.class")) {
            b = in.readAllBytes();
        }

        ClassModel cm = ClassFile.of().parse(b);
        for (MethodModel mm : cm.methods()) {
            if (!mm.methodName().stringValue().equals("compute")) continue;

            CodeModel code = mm.code().orElseThrow(() -> new AssertionError("no Code attribute"));

            boolean hasStackMap = false;
            for (Attribute<?> attr : code.attributes()) {
                if (attr.attributeName().stringValue().equals("StackMapTable"))
                    hasStackMap = true;
            }
            if (!hasStackMap) throw new AssertionError("no StackMapTable attribute");

            System.out.println("PASS");
            return;
        }
        throw new AssertionError("compute method not found");
    }
}
