# javax.swing.filechooser.FileSystemView 设计案

## 1. API 功能
`JFileChooser` 访问文件系统的抽象网关，提供根分区、显示名、父目录、文件列表等 OS 相关查询（Unix/Windows 各有子类，另有 generic 回退）。

- **工厂**：`static FileSystemView getFileSystemView()`（按 `File.separatorChar` 选 Unix/Windows/generic 单例）。
- **根/目录**：`getRoots()`、`isFileSystemRoot(File)`、`isRoot(File)`、`getParentDirectory(File)`、`getHomeDirectory()`、`getDefaultDirectory()`。
- **文件列表**：`getFiles(File, boolean useFileHiding)`、`getChild(File,String)`、`isParent(File,File)`、`isTraversable(File)`、`isHiddenFile(File)`。
- **显示**：`getSystemDisplayName(File)`、`getSystemTypeDescription(File)`、`getSystemIcon(File[,w,h])`。
- **文件对象**：`createFileObject(String)`、`createFileObject(File,String)`、抽象 `createNewFolder(File)`。
- 其余：`isDrive`/`isFloppyDrive`/`isComputerNode`/`isFileSystem`/`isLink`/`getLinkLocation`/`getChooserComboBoxFiles`/`getChooserShortcutPanelFiles`。

## 2. Java 用户如何使用
```java
FileSystemView fsv = FileSystemView.getFileSystemView();
File[] roots = fsv.getRoots();
File home = fsv.getHomeDirectory();
String name = fsv.getSystemDisplayName(file);
File parent = fsv.getParentDirectory(file);
File[] children = fsv.getFiles(dir, false);
```

典型组合链：`getFileSystemView → getRoots → isFileSystemRoot → getFiles → getSystemDisplayName/getParentDirectory`。

## 3. 开源代码实际用法调研
- `test/jdk/java/awt/FileDialog/FileSystemViewFilesTest.java`：`FileSystemView fsv = FileSystemView.getFileSystemView(); File[] roots = fsv.getRoots();` 后按名找 `Desktop`，再用 `fsv.getChooserComboBoxFiles()` 校验。
- `src/java.desktop/share/classes/javax/swing/filechooser/FileSystemView.java`：`UnixFileSystemView.isFileSystemRoot(dir)` 返回 `dir != null && dir.getAbsolutePath().equals("/")`，展示平台子类覆盖。

## 4. 测试用例设计 + 自动化实现
`FileSystemViewTest.testBlackBox()`（仅文件系统，无 GUI）覆盖：
- `getFileSystemView` 非空、`getRoots` 非空且 `isFileSystemRoot` 至少一个根为真。
- `isRoot(null)`/`isRoot(相对路径)` 为假；`getHomeDirectory`/`getDefaultDirectory` 非空绝对。
- `createFileObject` 等价 `new File(...)`。
- 临时目录：`getSystemDisplayName` 返回文件名、`isTraversable` 目录真/文件假、`getParentDirectory(file)==父目录`、`getFiles(dir,false)` 列出条目（排序后比对）、`isHiddenFile==File.isHidden`。
- 白盒保留结构断言（父类 `Object`、抽象、`getFileSystemView` 存在）。

## 5. 追加契约段（2026-11 深化）
getFileSystemView() 共享单例；getSystemIcon(不存在文件)==null；isComputerNode/isDrive/isFloppyDrive
对普通目录 false；isFileSystem(home) true。
**JDK-27 独有 API（JDK-8 不存在，仅本侧锁定）：isLink(tmp)==false、isLink(null)→NPE、
getLinkLocation(非链接)==null、getChooserComboBoxFiles() 非 null 数组、
getChooserShortcutPanelFiles() 非空（≥1）、getSystemIcon(f,16,16) 非 null、
getSystemIcon(f,0,0)→IllegalArgumentException、getSystemIcon(f,-1,16)→IllegalArgumentException**。
