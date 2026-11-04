/*
 * @test
 * @summary sun.swing.FilePane conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main FilePaneTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FilePaneTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.FilePane", false, FilePaneTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.FilePane".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check("java.beans.PropertyChangeListener".equals(interfaces(c)), "interfaces");
        check("#void#setFileSelected();private static#void#recursivelySetInheritsPopupMenu(java.awt.Container,boolean);private#int#getEditIndex();private#java.lang.Object#findChildComponent(java.awt.Container,java.lang.Class);private#sun.swing.FilePane$DetailsTableCellEditor#getDetailsTableCellEditor();private#sun.swing.FilePane$DetailsTableModel#getDetailsTableModel();private#sun.swing.FilePane$DetailsTableRowSorter#getRowSorter();private#void#applyEdit();private#void#cancelEdit();private#void#doDeselectFile(java.lang.Object);private#void#doDirectoryChanged(java.beans.PropertyChangeEvent);private#void#doFileSelectionModeChanged(java.beans.PropertyChangeEvent);private#void#doFilterChanged(java.beans.PropertyChangeEvent);private#void#doMultiSelectionChanged(java.beans.PropertyChangeEvent);private#void#doSelectFile(java.io.File);private#void#doSelectedFileChanged(java.beans.PropertyChangeEvent);private#void#doSelectedFilesChanged(java.beans.PropertyChangeEvent);private#void#editFileName(int);private#void#ensureIndexIsVisible(int);private#void#fixNameColumnWidth(int);private#void#resetEditIndex();private#void#setEditIndex(int);private#void#updateDetailsColumnModel(javax.swing.JTable);private#void#updateListRowCount(javax.swing.JList);private#void#updateViewMenu();protected#boolean#isDirectorySelected();protected#java.io.File#getDirectory();protected#javax.swing.JFileChooser#getFileChooser();protected#javax.swing.plaf.basic.BasicDirectoryModel#getModel();protected#sun.swing.FilePane$Handler#getMouseHandler();protected#void#createActionMap();protected#void#installDefaults();public static#boolean#usesShellFolder(javax.swing.JFileChooser);public static#void#addActionsToMap(javax.swing.ActionMap,[Ljavax.swing.Action;);public#[Ljavax.swing.Action;#getActions();public#boolean#canWrite(java.io.File);public#int#getViewType();public#javax.swing.Action#getNewFolderAction();public#javax.swing.Action#getViewTypeAction(int);public#javax.swing.JMenu#getViewMenu();public#javax.swing.JPanel#createDetailsView();public#javax.swing.JPanel#createList();public#javax.swing.JPopupMenu#getComponentPopupMenu();public#javax.swing.event.ListSelectionListener#createListSelectionListener();public#void#clearSelection();public#void#ensureFileIsVisible(javax.swing.JFileChooser,java.io.File);public#void#propertyChange(java.beans.PropertyChangeEvent);public#void#rescanCurrentDirectory();public#void#setViewType(int);public#void#uninstallUI()".equals(methods(c)), "methods");
        check("#int#lastIndex;#java.io.File#editFile;#javax.swing.JTextField#editCell;private final#java.awt.event.KeyListener#detailsKeyListener;private static final#int#COLUMN_FILENAME;private static final#int#VIEWTYPE_COUNT;private static final#java.awt.Cursor#waitCursor;private static#java.awt.event.FocusListener#repaintListener;private#[Ljava.lang.String;#viewTypeActionNames;private#[Ljavax.swing.Action;#actions;private#[Ljavax.swing.JPanel;#viewPanels;private#boolean#fullRowSelection;private#boolean#listViewWindowsStyle;private#boolean#readOnly;private#boolean#smallIconsView;private#int#viewType;private#java.awt.Color#listViewBackground;private#java.awt.event.FocusListener#editorFocusListener;private#java.io.File#newFolderFile;private#java.lang.String#filesDetailsAccessibleName;private#java.lang.String#filesListAccessibleName;private#java.lang.String#gigaByteString;private#java.lang.String#kiloByteString;private#java.lang.String#megaByteString;private#java.lang.String#newFolderActionLabelText;private#java.lang.String#refreshActionLabelText;private#java.lang.String#renameErrorFileExistsText;private#java.lang.String#renameErrorText;private#java.lang.String#renameErrorTitleText;private#java.lang.String#viewMenuLabelText;private#javax.swing.JList#list;private#javax.swing.JMenu#viewMenu;private#javax.swing.JPanel#currentViewPanel;private#javax.swing.JPopupMenu#contextMenu;private#javax.swing.JTable#detailsTable;private#javax.swing.ListSelectionModel#listSelectionModel;private#javax.swing.border.Border#listViewBorder;private#sun.swing.FilePane$DetailsTableCellEditor#tableCellEditor;private#sun.swing.FilePane$DetailsTableModel#detailsTableModel;private#sun.swing.FilePane$DetailsTableRowSorter#rowSorter;private#sun.swing.FilePane$FileChooserUIAccessor#fileChooserUIAccessor;private#sun.swing.FilePane$Handler#handler;protected#javax.swing.Action#newFolderAction;public static final#int#VIEWTYPE_DETAILS;public static final#int#VIEWTYPE_LIST;public static final#java.lang.String#ACTION_APPROVE_SELECTION;public static final#java.lang.String#ACTION_CANCEL;public static final#java.lang.String#ACTION_CHANGE_TO_PARENT_DIRECTORY;public static final#java.lang.String#ACTION_EDIT_FILE_NAME;public static final#java.lang.String#ACTION_NEW_FOLDER;public static final#java.lang.String#ACTION_REFRESH;public static final#java.lang.String#ACTION_VIEW_DETAILS;public static final#java.lang.String#ACTION_VIEW_LIST".equals(fields(c)), "fields");
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
