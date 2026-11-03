/*
 * @test
 * @summary java.io.ObjectInputStream.GetField conformance: white-box structure
 *          (abstract accessor class over a stream's field values) + black-box
 *          behavior (getObjectStreamClass identity, defaulted for present
 *          fields, all nine typed get() overloads routing correctly, unknown
 *          field names and signature mismatches raise IllegalArgumentException
 *          with the exact message).
 * @run main GetFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GetFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample implements Serializable {
        private static final long serialVersionUID = 42L;
        boolean z = true;
        byte b = -5;
        char c = 'Q';
        short s = 1234;
        int i = 987654;
        long j = 0xDEADBEEFCAFEL;
        float f = 3.5f;
        double d = -1.25;
        Object o = "payload";
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            exercise(in.readFields());
        }
    }

    // behavioral assertions run inside the readObject callback
    static void exercise(ObjectInputStream.GetField gf) {
        try {
            // 1. descriptor identity
            check(gf.getObjectStreamClass().getName().equals(Sample.class.getName()),
                  "descriptor class");
            check(gf.getObjectStreamClass().getSerialVersionUID() == 42L, "descriptor SUID");

            // 2. fields present in the stream are not defaulted
            check(!gf.defaulted("i"), "i came from the stream");
            check(!gf.defaulted("o"), "o came from the stream");

            // 3. unknown field name -> IllegalArgumentException
            try {
                gf.defaulted("nope");
                check(false, "defaulted(unknown) must throw");
            } catch (IllegalArgumentException iae) {
                check(iae.getMessage() != null && iae.getMessage().contains("no such field"),
                      "defaulted message: " + iae.getMessage());
            }

            // 4. every typed get() returns the serialized value
            check(gf.get("z", false), "boolean get");
            check(gf.get("b", (byte) 0) == -5, "byte get");
            check(gf.get("c", 'x') == 'Q', "char get");
            check(gf.get("s", (short) 0) == 1234, "short get");
            check(gf.get("i", 0) == 987654, "int get");
            check(gf.get("j", 0L) == 0xDEADBEEFCAFEL, "long get");
            check(gf.get("f", 0f) == 3.5f, "float get");
            check(gf.get("d", 0.0) == -1.25, "double get");
            check("payload".equals(gf.get("o", null)), "Object get");

            // 5. signature mismatch is reported as "no such field ... with type ..."
            try {
                gf.get("i", 1.0);
                check(false, "type mismatch must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field i with type double".equals(iae.getMessage()),
                      "mismatch message: " + iae.getMessage());
            }

            // 6. unknown field with any type
            try {
                gf.get("missing", 5);
                check(false, "unknown field get must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field missing with type int".equals(iae.getMessage()),
                      "unknown message: " + iae.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputStream$GetField");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct Object subclass");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "public no-arg constructor");
        check(c.getMethod("getObjectStreamClass").getReturnType()
                  .getName().equals("java.io.ObjectStreamClass"), "getObjectStreamClass");
        check(c.getMethod("defaulted", String.class).getReturnType() == boolean.class,
              "defaulted");
        int getCount = 0;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("get") && m.getParameterCount() == 2) getCount++;
        }
        check(getCount == 9, "nine typed get() overloads: " + getCount);
        // defaulted and every get declare IOException; the Object overload also
        // declares ClassNotFoundException
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("get") && !m.getName().equals("defaulted")) continue;
            boolean declaresIO = false;
            for (Class<?> ex : m.getExceptionTypes()) declaresIO |= ex == IOException.class;
            check(declaresIO, m.getName() + " declares IOException");
        }
        Method objGet = c.getMethod("get", String.class, Object.class);
        boolean declaresCNFE = false;
        for (Class<?> ex : objGet.getExceptionTypes())
            declaresCNFE |= ex == ClassNotFoundException.class;
        check(declaresCNFE, "Object get declares ClassNotFoundException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Sample());
        }
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        int before = passed;
        ois.readObject();                                    // triggers the callback
        check(passed > before, "readFields callback executed its assertions");

        // the stream is exhausted after the single object
        check(ois.read() == -1, "stream exhausted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
