/*
 * @test
 * @summary java.util.jar.Pack200 conformance: newPacker/newUnpacker round-trip (JDK-8 only).
 * @run main Pack200Test
 */

import java.io.ByteArrayInputStream;
import java.util.jar.*;
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


    // ===================== 契约深化：属性面/CHANGEABLE/round-trip 双向/props 常量 =====================
    static void testContracts() throws Exception {
        Pack200.Packer packer = Pack200.newPacker();
        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        // 属性表非空且含已知键
        java.util.SortedMap<String, String> pp = packer.properties();
        check(pp.size() > 0, "packer properties non-empty");
        check(pp.containsKey(Pack200.Packer.SEGMENT_LIMIT), "SEGMENT_LIMIT present");
        check(pp.containsKey(Pack200.Packer.EFFORT), "EFFORT present");
        check(pp.containsKey(Pack200.Packer.DEFLATE_HINT), "DEFLATE_HINT present");
        check(pp.containsKey(Pack200.Packer.UNKNOWN_ATTRIBUTE), "UNKNOWN_ATTRIBUTE present");
        check(pp.containsKey(Pack200.Packer.KEEP_FILE_ORDER), "KEEP_FILE_ORDER present");
        java.util.SortedMap<String, String> up = unpacker.properties();
        check(up != null, "unpacker properties map present");
        check(pp.get(Pack200.Packer.EFFORT).equals("5"), "default EFFORT is 5: " + pp.get(Pack200.Packer.EFFORT));
        // CHANGEABLE 属性可改
        String prev = pp.put(Pack200.Packer.EFFORT, "3");
        check("5".equals(prev), "put returns previous default");
        check(pp.get(Pack200.Packer.EFFORT).equals("3"), "EFFORT now 3");
        pp.put(Pack200.Packer.EFFORT, prev);
        // PASS_FILE_PFX 逐文件排除
        String prevPass = pp.put(Pack200.Packer.PASS_FILE_PFX + "skipme", "true");
        check(prevPass == null, "PASS_FILE_PFX new key");
        pp.remove(Pack200.Packer.PASS_FILE_PFX + "skipme");
        // 造 jar -> pack -> unpack -> 内容相等
        java.io.ByteArrayOutputStream jarBytes = new java.io.ByteArrayOutputStream();
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Main-Class", "Main");
        JarOutputStream jo = new JarOutputStream(jarBytes, mf);
        jo.putNextEntry(new JarEntry("dir/hello.txt"));
        jo.write("hello pack".getBytes("UTF-8"));
        jo.closeEntry();
        jo.close();
        java.io.ByteArrayOutputStream packed = new java.io.ByteArrayOutputStream();
        packer.pack(new JarInputStream(new java.io.ByteArrayInputStream(jarBytes.toByteArray())), packed);
        check(packed.size() > 0, "packed output non-empty");
        java.io.ByteArrayOutputStream unpacked = new java.io.ByteArrayOutputStream();
        unpacker.unpack(new java.io.ByteArrayInputStream(packed.toByteArray()),
                new JarOutputStream(unpacked));
        JarInputStream jis = new JarInputStream(new java.io.ByteArrayInputStream(unpacked.toByteArray()));
        check(jis.getManifest() != null, "manifest survives pack/unpack");
        check("Main".equals(jis.getManifest().getMainAttributes().getValue("Main-Class")),
              "Main-Class survives pack/unpack");
        JarEntry oe = jis.getNextJarEntry();
        check(oe != null && "dir/hello.txt".equals(oe.getName()), "entry name survives");
        byte[] body = new byte[32];
        int n = jis.read(body);
        check(new String(body, 0, n, "UTF-8").equals("hello pack"), "content survives pack/unpack");
        jis.close();
        // Pack200 仅 JDK-8 存在（JDK-14 移除），本侧 API 面锁定
        check(Class.forName("java.util.jar.Pack200") != null, "Pack200 present in this JDK");
    }

    static java.io.File newTempJar(byte[] bytes) throws Exception {
        java.io.File f = java.io.File.createTempFile("jck-p200", ".jar");
        f.deleteOnExit();
        java.io.FileOutputStream fo = new java.io.FileOutputStream(f);
        fo.write(bytes);
        fo.close();
        return f;
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testPack200();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
