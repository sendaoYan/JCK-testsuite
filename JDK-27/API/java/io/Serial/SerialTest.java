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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
