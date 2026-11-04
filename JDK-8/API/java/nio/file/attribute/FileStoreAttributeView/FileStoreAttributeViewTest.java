/*
 * @test
 * @summary java.nio.file.attribute.FileStoreAttributeView conformance: hierarchy and methods.
 * @run main FileStoreAttributeViewTest
 */

import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.AttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileStoreAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileStoreAttributeView() {
        check(FileStoreAttributeView.class.isInterface(), "is interface");
        check(AttributeView.class.isAssignableFrom(FileStoreAttributeView.class), "extends AttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : FileStoreAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "name" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileStoreAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
