/*
 * @test
 * @summary java.io.ObjectOutputStream.PutField conformance: put() overloads and write().
 * @run main PutFieldTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class PutFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPutField() throws IOException {
        final int[] calls = {0};
        ObjectOutputStream.PutField pf = new ObjectOutputStream.PutField() {
            public void put(String name, boolean val) { calls[0]++; }
            public void put(String name, byte val) { calls[0]++; }
            public void put(String name, char val) { calls[0]++; }
            public void put(String name, short val) { calls[0]++; }
            public void put(String name, int val) { calls[0]++; }
            public void put(String name, long val) { calls[0]++; }
            public void put(String name, float val) { calls[0]++; }
            public void put(String name, double val) { calls[0]++; }
            public void put(String name, Object val) { calls[0]++; }
            public void write(ObjectOutput out) { calls[0]++; }
        };
        pf.put("a", true);
        pf.put("a", (byte) 1);
        pf.put("a", 'c');
        pf.put("a", (short) 2);
        pf.put("a", 3);
        pf.put("a", 4L);
        pf.put("a", 5.0f);
        pf.put("a", 6.0);
        pf.put("a", "obj");
        check(calls[0] == 9, "all 9 put overloads callable");
        ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream());
        pf.write(out);
        check(calls[0] == 10, "write callable");
        out.close();
    }

    public static void main(String[] args) throws Exception {
        testPutField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
