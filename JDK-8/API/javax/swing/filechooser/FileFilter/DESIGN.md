# javax.swing.filechooser.FileFilter 测试设计（JDK-8 侧）

## 1. API 功能
抽象过滤器基类：`accept(File)` / `getDescription()` 两个抽象方法；唯一构造器 public（非 protected）。

## 2. Java 用户如何使用
匿名子类 `new FileFilter() { public boolean accept(File f) {...} public String getDescription() {...} }`
注册到 `JFileChooser.setFileFilter(...)`。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/filechooser/FileFilter.java`；FileNameExtensionFilter 是 JDK 提供的唯一具体子类。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF）
- 反射：abstract、恰好 2 个抽象方法、唯一构造器 public、accept/getDescription 在方法集。
- 子类语义：getDescription/accept 完全由子类决定（大小写不敏感逻辑属子类行为，非基类契约）；
  accept(null) 由子类防御（返回 false）；accept 按文件名判断与文件是否存在无关。
- 多态遍历文件列表命中计数；与 JFileChooser interop（setFileFilter/getFileFilter 委托/remove）；
  FileNameExtensionFilter instanceof FileFilter。

自动化实现位于同目录 `FileFilterTest.java`（headless，无 @key）。
