# javax.swing.JFileChooser 测试设计（JDK-8 侧）

## 1. API 功能
文件选择器：dialogType/fileSelectionMode/multiSelection/selectedFile(s)/fileFilter 族、
choosable filter 列表、currentDirectory 与 FileSystemView 装配、approve 按钮文本族；
headless 下 show* 抛 HeadlessException。

## 2. Java 用户如何使用
`JFileChooser fc = new JFileChooser(dir); fc.setFileSelectionMode(DIRECTORIES_ONLY);`
`fc.setFileFilter(new FileNameExtensionFilter("Docs","txt","md")); fc.showOpenDialog(parent)`。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JFileChooser.java`；JDK 测试 `test/javax/swing/JFileChooser/`；本仓库 probe。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF）
- **IAE 矩阵：setFileSelectionMode(99)→IllegalArgumentException（mode 不变）、
  setDialogType(99)→IllegalArgumentException（type 不变）；IAE 之后不再对该实例调用
  setCurrentDirectory——失败后的 UI 安装路径会 IOOBE，后续操作一律用新实例**。
- setCurrentDirectory(null) 回落到 home；默认 currentDirectory==home；ctor(File)/ctor(File,FSV)
  保留目录与 FSV 身份。
- setSelectedFiles(empty/null) → 长度 0；setFileFilter(null) → getFileFilter()==null。
- choosable 列表：默认含 accept-all；add 后包含；remove 返回 true，第二次 remove 返回 false；
  resetChoosableFileFilters 恢复单元素默认列表；setAcceptAllFileFilterUsed 往返。
- **headless：setDragEnabled(true) 抛 HeadlessException（需要 toolkit），dragEnabled 保持 false**；
  showOpenDialog/showSaveDialog/showDialog → HeadlessException。
- approve 文本族/dialogTitle/fileHiding/controlButtonsAreShown/multiSelection/fileView 往返。
- getUIClassID "FileChooserUI"；默认 fileView null。

自动化实现位于同目录 `JFileChooserTest.java`（headless，无 @key）。
