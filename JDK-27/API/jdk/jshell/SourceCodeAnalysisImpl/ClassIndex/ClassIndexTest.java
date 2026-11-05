/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysisImpl.ClassIndex conformance: class index reflection.
 * @run main ClassIndexTest
 */

import java.lang.reflect.*;

public class ClassIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: structure of the internal class index ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysisImpl$ClassIndex");
        // public fields as documented: the index exposes its maps/sets directly
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getFields()) names.add(f.getName());
        check(names.size() >= 4, ">=4 public index fields, got " + names);
        check(c.getConstructors().length == 1, "single public ctor");
        check(c.getConstructor(long.class, java.nio.file.Path.class, java.util.Set.class, java.util.Map.class) != null,
              "ctor signature (long, Path, Set, Map)");
        // documented field set: timestamp, forPath, packages, classSimpleName2FQN
        java.util.Set<String> fset = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getFields()) fset.add(f.getName());
        check(fset.contains("timestamp") && fset.contains("forPath")
              && fset.contains("packages") && fset.contains("classSimpleName2FQN"), "four documented index fields");
        // index buckets are Collection/Map shaped; timestamp is a long
        check(c.getField("timestamp").getType() == long.class, "timestamp is long");
        check(java.util.Set.class.isAssignableFrom(c.getField("packages").getType()), "packages is a Set");
        check(java.util.Map.class.isAssignableFrom(c.getField("classSimpleName2FQN").getType()), "index is a Map");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysisImpl$ClassIndex");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getFields().length >= 4, "public fields");
        check(c.getConstructor(long.class, java.nio.file.Path.class, java.util.Set.class, java.util.Map.class) != null, "ctor");
        testStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
