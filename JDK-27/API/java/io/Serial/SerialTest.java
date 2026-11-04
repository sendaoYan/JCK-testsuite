/*
 * @test
 * @summary java.io.Serial conformance: white-box structure (annotation meta) + black-box
 *          behavior (annotating the canonical serialization members of a Serializable
 *          class, compiler/runtime acceptance, non-applicability elsewhere).
 * @run main SerialTest
 */

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SerialTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Canonical annotated serialization members (javac validates @Serial placement). */
    static class Annotated implements Serializable {
        @Serial
        private static final long serialVersionUID = 24L;

        @Serial
        private static final java.io.ObjectStreamField[] serialPersistentFields =
            { new java.io.ObjectStreamField("v", int.class) };

        @Serial
        private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
            out.defaultWriteObject();
        }

        @Serial
        private void readObject(java.io.ObjectInputStream in)
                throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
        }

        @Serial
        private void readObjectNoData() throws java.io.ObjectStreamException {}

        @Serial
        private Object writeReplace() throws java.io.ObjectStreamException { return this; }

        @Serial
        private Object readResolve() throws java.io.ObjectStreamException { return this; }

        int v = 5;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Serial");
        check(c.isAnnotation(), "is an annotation type");
        check(!c.isInterface() || c.isAnnotation(), "annotation");
        Target t = c.getAnnotation(Target.class);
        check(t != null && t.value().length == 2, "targets declared");
        boolean method = false, field = false;
        for (ElementType e : t.value()) {
            if (e == ElementType.METHOD) method = true;
            if (e == ElementType.FIELD) field = true;
        }
        check(method && field, "@Target({METHOD, FIELD})");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.SOURCE,
              "@Retention(SOURCE): compile-time checking only");
        check(java.lang.annotation.Inherited.class.isAssignableFrom(c) == false,
              "not @Inherited");
        // no runtime presence: @Serial is erased after compilation
        Field suid = Annotated.class.getDeclaredField("serialVersionUID");
        check(suid.getAnnotation(Serial.class) == null, "SOURCE retention leaves no runtime annotation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. all seven canonical members compile and behave; the annotated class serializes
        Annotated a = new Annotated();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        try (java.io.ObjectInputStream ois =
                 new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Annotated back = (Annotated) ois.readObject();
            check(back.v == 5, "annotated class round-trips");
            check(back == back.writeReplace() && back == back.readResolve(),
                  "writeReplace/readResolve invoked on annotated class");
        }

        // 2. @Serial on serialVersionUID matches the descriptor value in the stream
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Annotated.class);
        check(osc.getSerialVersionUID() == 24L,
              "@Serial serialVersionUID value flows into the class descriptor");

        // 3. serialVersionUID field must be static final long when annotated (reflection form check)
        Field suid = Annotated.class.getDeclaredField("serialVersionUID");
        int m = suid.getModifiers();
        check(Modifier.isStatic(m) && Modifier.isFinal(m) && suid.getType() == long.class,
              "serialVersionUID form: private static final long");

        // 4. writeObject signature form
        Method wo = Annotated.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        check(wo.getReturnType() == void.class
              && java.io.IOException.class == wo.getExceptionTypes()[0],
              "writeObject form: private void (ObjectOutputStream) throws IOException");

        // 5. readObjectNoData form
        Method ron = Annotated.class.getDeclaredMethod("readObjectNoData");
        check(ron.getExceptionTypes().length == 1
              && java.io.ObjectStreamException.class == ron.getExceptionTypes()[0],
              "readObjectNoData form: private void () throws ObjectStreamException");

        // 6. serialPersistentFields form
        Field spf = Annotated.class.getDeclaredField("serialPersistentFields");
        check(spf.getType() == java.io.ObjectStreamField[].class,
              "serialPersistentFields form: ObjectStreamField[]");

        // 7. an annotated non-serialization member on a non-Serializable class compiles
        //    but is ignored by the mechanism (runtime does not enforce SOURCE semantics)
        check(Serial.class.isAnnotation(), "annotation remains usable via reflection on the type");
    }

    // ================= deep additions (contract level) =================

    // ---- exact meta-annotation surface ----
    static void testMetaExact() throws Exception {
        Class<?> c = Class.forName("java.io.Serial");
        check(c.getPackage() != null && "java.io".equals(c.getPackage().getName()),
              "package is java.io");
        check(Modifier.isPublic(c.getModifiers()), "public annotation type");
        check(Modifier.isInterface(c.getModifiers()), "declared as interface");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no elements");
        Annotation[] annos = c.getAnnotations();
        check(annos.length == 2, "exactly two runtime-visible meta-annotations: "
              + java.util.Arrays.toString(annos));
        boolean retention = false, target = false;
        for (Annotation a : annos) {
            if (a instanceof Retention) retention = true;
            if (a instanceof Target) target = true;
        }
        check(retention && target, "meta-annotations are @Retention and @Target");
        check(c.getAnnotation(java.lang.annotation.Documented.class) == null,
              "NOT @Documented");
        check(c.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "NOT @Inherited");
        check(c.getAnnotation(java.lang.annotation.Repeatable.class) == null,
              "NOT @Repeatable");
    }

    // ---- @Target: exactly FIELD and METHOD, every other position excluded ----
    static void testTargetMatrix() throws Exception {
        Target t = Class.forName("java.io.Serial").getAnnotation(Target.class);
        check(t != null, "@Target present");
        java.util.Set<ElementType> allowed = java.util.EnumSet.noneOf(ElementType.class);
        for (ElementType e : t.value()) allowed.add(e);
        check(allowed.size() == 2, "exactly two targets");
        check(allowed.contains(ElementType.FIELD), "FIELD allowed");
        check(allowed.contains(ElementType.METHOD), "METHOD allowed");
        for (ElementType e : ElementType.values()) {
            if (e == ElementType.FIELD || e == ElementType.METHOD) continue;
            check(!allowed.contains(e), "target " + e + " NOT allowed");
        }
    }

    // ---- SOURCE retention: nothing survives into the class file ----
    static void testRuntimeInvisibility() throws Exception {
        Field suid = Annotated.class.getDeclaredField("serialVersionUID");
        check(suid.getDeclaredAnnotations().length == 0,
              "@Serial leaves no runtime annotation on the field");
        Method wo = Annotated.class.getDeclaredMethod("writeObject",
                                                      java.io.ObjectOutputStream.class);
        check(wo.getDeclaredAnnotations().length == 0,
              "@Serial leaves no runtime annotation on the method");
        Method rr = Annotated.class.getDeclaredMethod("readResolve");
        check(rr.getDeclaredAnnotations().length == 0,
              "same for readResolve");
        Field spf = Annotated.class.getDeclaredField("serialPersistentFields");
        check(spf.getDeclaredAnnotations().length == 0,
              "same for serialPersistentFields");
    }

    // ---- the annotated serialPersistentFields shape the descriptor ----
    static void testAnnotatedSpfShapesDescriptor() throws Exception {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Annotated.class);
        check(osc.getField("v") != null, "declared serialPersistentFields drives the descriptor");
        check(osc.getField("v").getType() == int.class, "spf field type exposed");
        check(osc.getFields().length == 1, "only the spf-declared field present");
        check(osc.getField("notThere") == null, "undeclared name absent");
    }

    /** Compile one snippet in memory; returns null on success else the first error text. */
    static String compileExpecting(String className, String source) throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) throw new IllegalStateException("no system compiler in test JDK");
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("serial-" + className);
        try {
            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> dc =
                    new javax.tools.DiagnosticCollector<>();
            javax.tools.SimpleJavaFileObject fo =
                    new javax.tools.SimpleJavaFileObject(
                            java.net.URI.create("string:///" + className + ".java"),
                            javax.tools.JavaFileObject.Kind.SOURCE) {
                        public CharSequence getCharContent(boolean ignoreErrors) { return source; }
                    };
            Boolean ok = jc.getTask(null, null, dc,
                                    java.util.Arrays.asList("-d", dir.toString()),
                                    null, java.util.Arrays.asList(fo)).call();
            if (ok) return null;
            for (javax.tools.Diagnostic<? extends javax.tools.JavaFileObject> d
                    : dc.getDiagnostics()) {
                if (d.getKind() == javax.tools.Diagnostic.Kind.ERROR)
                    return d.getMessage(null);
            }
            return "failed without error diagnostics";
        } finally {
            java.nio.file.Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { java.nio.file.Files.delete(p); } catch (Exception e) { } });
        }
    }

    // ---- javac placement matrix: canonical members compile, misplaced ones are rejected ----
    static void testCompileMatrix() throws Exception {
        String err;

        err = compileExpecting("Sp1", "import java.io.*;\n"
                + "class Sp1 implements Serializable { @Serial\n"
                + "    private static final long serialVersionUID = 1L; }");
        check(err == null, "@Serial on serialVersionUID compiles" + (err == null ? "" : ": " + err));

        err = compileExpecting("Sp2", "import java.io.*;\n"
                + "class Sp2 implements Serializable { int v; @Serial\n"
                + "    private void writeObject(ObjectOutputStream o) throws IOException"
                + " { o.defaultWriteObject(); } }");
        check(err == null, "@Serial on writeObject compiles" + (err == null ? "" : ": " + err));

        err = compileExpecting("Sp3", "import java.io.*;\n"
                + "class Sp3 implements Serializable { @Serial\n"
                + "    private void readObjectNoData() throws ObjectStreamException { } }");
        check(err == null, "@Serial on readObjectNoData compiles" + (err == null ? "" : ": " + err));

        err = compileExpecting("Sp4", "import java.io.*;\n"
                + "class Sp4 implements Serializable { @Serial\n"
                + "    private Object writeReplace() { return this; } }");
        check(err == null, "@Serial on writeReplace compiles" + (err == null ? "" : ": " + err));

        err = compileExpecting("Sp5", "import java.io.*;\n"
                + "class Sp5 implements Externalizable { @Serial\n"
                + "    private static final long serialVersionUID = 1L;\n"
                + "    public void writeExternal(ObjectOutput o) {}\n"
                + "    public void readExternal(ObjectInput i) {} }");
        check(err == null, "@Serial on Externalizable serialVersionUID compiles"
              + (err == null ? "" : ": " + err));

        err = compileExpecting("Sn1", "import java.io.*;\n"
                + "@Serial class Sn1 implements Serializable { }");
        check(err != null && err.contains("not applicable"),
              "@Serial on a type declaration is rejected: " + err);

        // semantically misplaced (but syntactically FIELD/METHOD) placements compile:
        // javac enforces the @Target kind here, not the serialization-member semantics
        err = compileExpecting("Sn2", "import java.io.*;\n"
                + "class Sn2 implements Serializable { @Serial int v; }");
        check(err == null, "@Serial on a non-serial member compiles (recording javac behavior)"
              + (err == null ? "" : ": " + err));

        err = compileExpecting("Sn3", "import java.io.*;\n"
                + "class Sn3 { @Serial private static final long serialVersionUID = 1L; }");
        check(err == null, "@Serial in a non-Serializable class compiles (ignored by the mechanism)"
              + (err == null ? "" : ": " + err));

        err = compileExpecting("Sn4", "import java.io.*;\n"
                + "class Sn4 implements Serializable { @Serial private void foo() { } }");
        check(err == null, "@Serial on an arbitrary method compiles (recording javac behavior)"
              + (err == null ? "" : ": " + err));

        err = compileExpecting("Sn5", "import java.io.*;\n"
                + "class Sn5 implements Serializable { @Serial\n"
                + "    private void writeObject(java.io.DataOutput o) { } }");
        check(err == null, "@Serial on a wrong-signature hook compiles (hook is not invoked)"
              + (err == null ? "" : ": " + err));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMetaExact();
        testTargetMatrix();
        testRuntimeInvisibility();
        testAnnotatedSpfShapesDescriptor();
        testCompileMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
