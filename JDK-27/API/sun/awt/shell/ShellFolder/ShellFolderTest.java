/*
 * @test
 * @summary sun.awt.shell.ShellFolder conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.shell:+open
 * @run main ShellFolderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ShellFolderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.shell.ShellFolder", false, ShellFolderTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.shell.ShellFolder".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.io.File".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("protected abstract#java.lang.Object#writeReplace();public abstract#boolean#isLink();public abstract#java.lang.String#getDisplayName();public abstract#java.lang.String#getExecutableType();public abstract#java.lang.String#getFolderType();public abstract#sun.awt.shell.ShellFolder#getLinkLocation();public static#[Lsun.awt.shell.ShellFolderColumnInfo;#getFolderColumns(java.io.File);public static#boolean#isComputerNode(java.io.File);public static#boolean#isFileSystemRoot(java.io.File);public static#java.io.File#getNormalizedFile(java.io.File);public static#java.lang.Object#get(java.lang.String);public static#java.lang.Object#getFolderColumnValue(java.io.File,int);public static#java.lang.Object#invoke(java.util.concurrent.Callable);public static#java.lang.Object#invoke(java.util.concurrent.Callable,java.lang.Class);public static#sun.awt.shell.ShellFolder#getShellFolder(java.io.File);public static#void#sort(java.util.List);public#[Ljava.io.File;#listFiles();public#[Ljava.io.File;#listFiles(boolean);public#[Lsun.awt.shell.ShellFolderColumnInfo;#getFolderColumns();public#boolean#canRead();public#boolean#canWrite();public#boolean#createNewFile();public#boolean#delete();public#boolean#exists();public#boolean#isAbsolute();public#boolean#isDirectory();public#boolean#isFile();public#boolean#isFileSystem();public#boolean#mkdir();public#boolean#mkdirs();public#boolean#renameTo(java.io.File);public#boolean#setLastModified(long);public#boolean#setReadOnly();public#int#compareTo(java.io.File);public#java.awt.Image#getIcon(boolean);public#java.awt.Image#getIcon(int,int);public#java.io.File#getAbsoluteFile();public#java.io.File#getParentFile();public#java.lang.Object#getFolderColumnValue(int);public#java.lang.String#getParent();public#java.lang.String#toString();public#long#lastModified();public#long#length();public#void#deleteOnExit();public#void#sortChildren(java.util.List)".equals(methods(c)), "methods");
        check("private static final#java.util.Comparator#DEFAULT_COMPARATOR;private static final#java.util.Comparator#FILE_COMPARATOR;private static final#sun.awt.shell.ShellFolder$Invoker#invoker;private static final#sun.awt.shell.ShellFolderManager#shellFolderManager;protected#sun.awt.shell.ShellFolder#parent;public static final#java.lang.String#COLUMN_DATE;public static final#java.lang.String#COLUMN_NAME;public static final#java.lang.String#COLUMN_SIZE".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
