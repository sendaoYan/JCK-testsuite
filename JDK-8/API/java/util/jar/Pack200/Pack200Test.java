/*
 * @test
 * @summary java.util.jar.Pack200 conformance: newPacker/newUnpacker round-trip (JDK-8 only).
 * @run main Pack200Test
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public class Pack200Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPack200() throws Exception {
        check(Modifier.isAbstract(Pack200.class.getModifiers()), "abstract");
        check(Pack200.newPacker() != null, "newPacker");
        check(Pack200.newUnpacker() != null, "newUnpacker");

        // build a one-entry JAR
        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(jarBytes);
        jos.putNextEntry(new JarEntry("hello.txt"));
        jos.write("world".getBytes("UTF-8"));
        jos.closeEntry();
        jos.close();

        // pack
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        Pack200.newPacker().pack(
                new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray())), packed);

        // unpack
        ByteArrayOutputStream unpacked = new ByteArrayOutputStream();
        Pack200.newUnpacker().unpack(
                new ByteArrayInputStream(packed.toByteArray()), new JarOutputStream(unpacked));

        // verify round-trip
        JarInputStream jis = new JarInputStream(new ByteArrayInputStream(unpacked.toByteArray()));
        JarEntry entry = jis.getNextJarEntry();
        check("hello.txt".equals(entry.getName()), "entry name round-trip");
        byte[] content = new byte[5];
        jis.read(content);
        check("world".equals(new String(content, "UTF-8")), "entry content round-trip");
        jis.close();
    }

    public static void main(String[] args) throws Exception {
        testPack200();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
