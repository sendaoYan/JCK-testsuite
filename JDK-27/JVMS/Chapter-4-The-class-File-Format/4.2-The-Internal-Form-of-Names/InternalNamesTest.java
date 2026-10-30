/*
 * @test
 * @summary JVMS 4.2: class and interface names are stored in internal form,
 *          using '/' as the package separator rather than '.'.  The this_class
 *          entry of java.lang.String must be "java/lang/String".
 * @run main InternalNamesTest
 */

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

public class InternalNamesTest {
    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = String.class.getResourceAsStream("String.class")) {
            b = in.readAllBytes();
        }

        ClassModel cm = ClassFile.of().parse(b);
        String internal = cm.thisClass().asInternalName();
        if (!"java/lang/String".equals(internal))
            throw new AssertionError("internal name == " + internal);

        System.out.println("PASS");
    }
}
