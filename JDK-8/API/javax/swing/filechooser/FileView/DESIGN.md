# javax.swing.filechooser.FileView 测试设计（JDK-8 侧）

## 1. API 功能
抽象 UI 元数据视图：getName/getDescription/getTypeDescription/getIcon/isTraversable 五个方法
**全部是具体方法且默认返回 null（"询问 LaF"语义）**；无抽象方法（空子类合法）。

## 2. Java 用户如何使用
部分覆写：`new FileView() { public String getName(File f) { return ...; } }` → `fc.setFileView(v)`；
未覆写的方法返回 null，FileChooser 回落到 LaF 提供的视图。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/filechooser/FileView.java`；MetalFileChooserUI 的 FileIOAction 等回退路径。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF）
- 反射：abstract、0 个抽象方法（与 FileFilter 的 2 个对照）、五方法在方法集。
- 空子类五方法全 null；全覆写子类各自返回常量（含 isTraversable Boolean.TRUE）；
  getName(null) 由子类自行处理。
- **部分覆写：未覆写方法保持 null 默认（不混入 LaF 值）**。
- interop：JFileChooser 默认 fileView==null；setFileView 往返；setFileView(null) 合法。

自动化实现位于同目录 `FileViewTest.java`（headless，无 @key）。
