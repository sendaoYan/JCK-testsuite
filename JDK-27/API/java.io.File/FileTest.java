/*
 * @test
 * @summary java.io.File conformance: white-box structure (reflection) + black-box behavior
 *          (path components, filesystem queries, create/rename/delete lifecycle).
 * @run main FileTest
 *
 * 黑盒设计案:
 * 1. 契约: File 是文件/目录的路径抽象，提供名称/路径分量、存在性/类型/长度查询，以及创建、删除、重命名、列目录等文件系统操作。
 * 2. 场景: createNewFile/createTempFile 建文件 -> exists/isFile/isDirectory/length/getName/getAbsolutePath；mkdirs 建目录；list 列子项。
 * 3. 组合: 建目录 -> 放文件 -> 写内容 -> length -> list 排序断言 -> renameTo -> 非空目录 delete 失败 -> 逐级 delete 清理。
 * 4. 不变量: getCanonicalFile 规范化路径; equals/hashCode 按路径一致; compareTo 一致; toPath/toFile 往返等价。
 * 5. 边界: 不存在 -> exists/isFile==false 且 length==0；非空目录 delete()==false。
 */

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;

public class FileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    static void hasField(Class<?> c, String name) {
        try { c.getField(name); }
        catch (NoSuchFieldException e) { throw new AssertionError("missing field " + name); }
        passed++;
    }

    static void deleteRecursively(File f) {
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) deleteRecursively(k);
        f.delete();
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.File");
        check(!c.isInterface() && !c.isAnnotation() && !c.isEnum(), "is concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasInterface(c, "java.lang.Comparable"), "implements Comparable");
        hasField(c, "separatorChar");
        hasField(c, "separator");
        hasField(c, "pathSeparatorChar");
        hasField(c, "pathSeparator");
        hasMethod(c, "getName");
        hasMethod(c, "exists");
        hasMethod(c, "isDirectory");
        hasMethod(c, "mkdir");
        hasMethod(c, "list");
        hasMethod(c, "createTempFile", String.class, String.class);
        hasMethod(c, "toPath");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        File d = Files.createTempDirectory("jck-file").toFile();
        try {
            File nested = new File(d, "a/b");
            check(nested.mkdirs() && nested.isDirectory(), "mkdirs nested dir");
            File a = new File(d, "a");
            check(a.isDirectory(), "parent dir isDirectory");
            File canon = a.getCanonicalFile();
            check(canon.isDirectory() && canon.getName().equals("a"), "getCanonicalFile");

            File f = new File(d, "f.txt");
            check(f.createNewFile(), "createNewFile");
            check(f.exists() && f.isFile() && !f.isDirectory(), "exists/isFile/isDirectory");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(new byte[]{1, 2, 3, 4, 5});
            }
            check(f.length() == 5, "length after write");
            check(f.getName().equals("f.txt"), "getName");
            check(f.getAbsoluteFile().isAbsolute(), "getAbsoluteFile isAbsolute");
            check(f.getParentFile() != null && f.getParentFile().equals(d), "getParentFile");

            String[] lst = d.list();
            Arrays.sort(lst);
            check(Arrays.equals(lst, new String[]{"a", "f.txt"}), "list sorted");
            File[] files = d.listFiles();
            String[] fnames = new String[files.length];
            for (int i = 0; i < files.length; i++) fnames[i] = files[i].getName();
            Arrays.sort(fnames);
            check(Arrays.equals(fnames, new String[]{"a", "f.txt"}), "listFiles sorted");

            File renamed = new File(d, "g.txt");
            check(f.renameTo(renamed) && renamed.exists() && !f.exists(), "renameTo");

            check(renamed.toPath().toFile().equals(renamed), "toPath/toFile roundtrip");

            File other = new File(d, "g.txt");
            check(renamed.equals(other) && renamed.hashCode() == other.hashCode(), "equals/hashCode");
            check(renamed.compareTo(other) == 0, "compareTo");

            check(!d.delete(), "non-empty dir delete == false");
            check(renamed.delete() && !renamed.exists(), "delete file");
            check(nested.delete(), "delete nested dir");
            check(a.delete(), "delete parent dir");

            File missing = new File(d, "nope");
            check(!missing.exists() && !missing.isFile() && missing.length() == 0, "nonexistent queries");

            File tmp = File.createTempFile("jck", ".tmp", d);
            check(tmp.exists() && tmp.isFile(), "createTempFile in dir");
            check(tmp.getName().startsWith("jck") && tmp.getName().endsWith(".tmp"), "createTempFile name");
            check(tmp.delete(), "delete temp file");
        } finally {
            deleteRecursively(d);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
