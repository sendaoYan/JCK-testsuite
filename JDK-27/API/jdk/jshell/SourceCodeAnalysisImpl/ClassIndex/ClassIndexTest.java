/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysisImpl.ClassIndex conformance: class index reflection.
 * @run main ClassIndexTest
 */

import java.lang.reflect.*;

public class ClassIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysisImpl$ClassIndex");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getFields().length >= 4, "public fields");
        check(c.getConstructor(long.class, java.nio.file.Path.class, java.util.Set.class, java.util.Map.class) != null, "ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
