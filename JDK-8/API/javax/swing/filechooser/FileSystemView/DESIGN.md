# javax.swing.filechooser.FileSystemView 测试设计（JDK-8 侧）

## 1. API 功能
文件系统抽象视图（abstract）：roots/home/default 目录、显示名/类型描述/系统图标、
createFileObject/getChild/getParentDirectory/isParent、isTraversable/isHiddenFile、
平台标志（isRoot/isComputerNode/isDrive/isFloppyDrive）。

## 2. Java 用户如何使用
`FileSystemView fsv = FileSystemView.getFileSystemView(); fsv.getSystemDisplayName(file)`；
`new JFileChooser(fsv)` 装配进选择器。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/filechooser/FileSystemView.java` + UnixFileSystemView（Linux 平台行为）；
本仓库 probe。

## 4. 测试用例设计 + 易错细节（Linux/headless 锁定，Metal LaF）
- **getFileSystemView() 返回共享单例（两次调用同一实例）**。
- roots：≥1 个、根是绝对路径目录、isRoot(root)==true、isRoot(tmp)==false、isRoot(null)==false。
- **getSystemIcon(nonexistent)==null（存在文件/目录非 null）**；getSystemTypeDescription 在
  Linux 返回 null；getSystemDisplayName(nonexistent) 返回裸文件名。
- isTraversable：目录 TRUE、普通文件 FALSE、不存在 FALSE（Boolean 装箱精确断言）。
- createFileObject(String)/(File,String)/getChild 与 new File 语义相等；isParent/getParentDirectory；
- 平台标志对 tmp 全 false；isHiddenFile：点前缀 true、普通名 false（Unix 语义）。
- **JDK-27 独有 API（本侧不存在，不测）：isLink/getLinkLocation/getChooserComboBoxFiles/
  getChooserShortcutPanelFiles/getSystemIcon(f,w,h)** —— 见 JDK-27 侧同目录设计。

自动化实现位于同目录 `FileSystemViewTest.java`（headless，无 @key）。
