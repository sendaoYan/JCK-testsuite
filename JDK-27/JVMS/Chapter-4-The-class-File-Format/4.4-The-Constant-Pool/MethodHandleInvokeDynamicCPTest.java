/*
 * @test
 * @summary JVMS 4.4: code that uses lambda expressions / method references /
 *          string concatenation must carry CONSTANT_MethodHandle,
 *          CONSTANT_MethodType and CONSTANT_InvokeDynamic entries in its
 *          constant pool, and those entries must drive working indy calls.
 *          (Compiles with JDK 24+ where java.lang.classfile is final; the
 *          JDK-27 suite always satisfies this.)
 * @run main MethodHandleInvokeDynamicCPTest
 */

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.MethodTypeEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.util.function.Function;

public class MethodHandleInvokeDynamicCPTest {
    // A lambda and a method reference force invokedynamic (LambdaMetafactory)
    // plus CONSTANT_MethodHandle / CONSTANT_MethodType entries into the pool.
    static final Function<String, Integer> LEN = s -> s.length();
    static final Function<String, Integer> LEN_REF = String::length;

    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = MethodHandleInvokeDynamicCPTest.class
                .getResourceAsStream("MethodHandleInvokeDynamicCPTest.class")) {
            b = in.readAllBytes();
        }

        ClassModel cm = ClassFile.of().parse(b);
        var cp = cm.constantPool();

        boolean mh = false, mt = false, indy = false;
        for (PoolEntry e : cp) {
            if (e instanceof MethodHandleEntry) mh = true;
            else if (e instanceof MethodTypeEntry) mt = true;
            else if (e instanceof InvokeDynamicEntry) indy = true;
        }

        if (!mh) throw new AssertionError("missing CONSTANT_MethodHandle");
        if (!mt) throw new AssertionError("missing CONSTANT_MethodType");
        if (!indy) throw new AssertionError("missing CONSTANT_InvokeDynamic");

        // The entries must actually work: both invokedynamic call sites resolve.
        if (LEN.apply("hello") != 5) throw new AssertionError("lambda broken");
        if (LEN_REF.apply("hello") != 5) throw new AssertionError("method reference broken");

        System.out.println("PASS");
    }
}
