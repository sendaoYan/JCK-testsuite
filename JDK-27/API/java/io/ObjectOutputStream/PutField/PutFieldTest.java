/*
 * @test
 * @summary java.io.ObjectOutputStream.PutField conformance: white-box structure
 *          (abstract buffer of named field values with nine put overloads and a
 *          deprecated-for-removal write(ObjectOutput)) + black-box behavior
 *          (all typed puts round-trip through writeFields/readFields, unknown
 *          names and signature mismatches raise IllegalArgumentException with
 *          the exact message, writing to a foreign stream is refused).
 * @run main PutFieldTest
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

public class PutFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final boolean ZZ = true;
    static final byte BB = -5;
    static final char CC = 'Q';
    static final short SS = 1234;
    static final int II = 987654;
    static final long JJ = 0xDEADBEEFCAFEL;
    static final float FF = 3.5f;
    static final double DD = -1.25;
    static final String OO = "payload";

    // capture of what readFields observed on the other end
    static String report = "";
    static boolean okZ, okO;
    static byte okB; static char okC; static short okS; static int okI; static long okJ;
    static float okF; static double okD;

    static class Sample implements Serializable {
        private static final long serialVersionUID = 42L;
        boolean z; byte b; char c; short s; int i; long j; float f; double d; Object o;
        @SuppressWarnings("removal")
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            try {
                pf.put("nope", 1);
                check(false, "unknown field put must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field nope with type int".equals(iae.getMessage()),
                      "unknown message: " + iae.getMessage());
            }
            try {
                pf.put("i", 1.0);
                check(false, "signature mismatch must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field i with type double".equals(iae.getMessage()),
                      "mismatch message: " + iae.getMessage());
            }
            try {
                ByteArrayOutputStream foreign = new ByteArrayOutputStream();
                ObjectOutputStream other = new ObjectOutputStream(foreign);
                pf.write(other);
                check(false, "writing to a foreign stream must be refused");
            } catch (IllegalArgumentException iae) {
                check("wrong stream".equals(iae.getMessage()), "foreign message");
            }
            pf.put("z", ZZ);
            pf.put("b", BB);
            pf.put("c", CC);
            pf.put("s", SS);
            pf.put("i", II);
            pf.put("j", JJ);
            pf.put("f", FF);
            pf.put("d", DD);
            pf.put("o", OO);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            okZ = gf.get("z", false); okB = gf.get("b", (byte) 0); okC = gf.get("c", 'x');
            okS = gf.get("s", (short) 0); okI = gf.get("i", 0); okJ = gf.get("j", 0L);
            okF = gf.get("f", 0f); okD = gf.get("d", 0.0);
            okO = OO.equals(gf.get("o", null));
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectOutputStream$PutField");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct Object subclass");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "public no-arg constructor");
        int putCount = 0;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("put") && m.getParameterCount() == 2) putCount++;
        }
        check(putCount == 9, "nine typed put() overloads: " + putCount);
        Method write = c.getMethod("write", java.io.ObjectOutput.class);
        check(write.getExceptionTypes().length == 1
                  && write.getExceptionTypes()[0] == IOException.class,
              "write(ObjectOutput) declares IOException");
        Deprecated dep = write.getAnnotation(Deprecated.class);
        check(dep != null && dep.forRemoval(), "write(ObjectOutput) deprecated for removal");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Sample());                  // runs the put() assertions
        }
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();                                   // runs the read-back captures

        // every typed put survived the round-trip exactly
        check(passed > 0, "writeObject callback ran");
        check(okZ == ZZ, "boolean put survived");
        check(okB == BB, "byte put survived");
        check(okC == CC, "char put survived");
        check(okS == SS, "short put survived");
        check(okI == II, "int put survived");
        check(okJ == JJ, "long put survived");
        check(okF == FF, "float put survived");
        check(okD == DD, "double put survived");
        check(okO, "Object put survived");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
