# javax.swing.filechooser.FileNameExtensionFilter 测试设计（JDK-8 侧）

## 1. API 功能
JDK 内置扩展名过滤器（final，extends FileFilter）：varargs 扩展名构造、accept 后缀匹配、
getExtensions/getDescription/toString。

## 2. Java 用户如何使用
`new FileNameExtensionFilter("Docs", "txt", "md")` → `fc.setFileFilter(filter)`。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/filechooser/FileNameExtensionFilter.java`；本仓库 probe（大小写/空白/IAE 矩阵）。

## 4. 测试用例设计 + 易错细节（headless 锁定）
- **ctor IAE 矩阵：null 数组 / 空数组 / null 元素 / 空串元素 → IllegalArgumentException**；
  扩展名内容不校验（"a b"、"x/y" 合法构造）；description 可为 null（getDescription()==null）。
- **getExtensions 原样返回（不 trim、不小写化）**：ctor(" csv ") 的 accept("a.csv")==false
  （匹配用未修剪的精确后缀）；大小写双向不敏感（filter "TXT" 匹配 "a.txt"/"a.TXT"）。
- getExtensions 返回防御拷贝（突变外部数组不影响过滤器）。
- 目录一律 accept；accept(null)==false；无扩展名拒绝；"atxt"（无点）拒绝；部分后缀不匹配。
- toString 包含 description 与扩展名；可用作 JFileChooser filter。

自动化实现位于同目录 `FileNameExtensionFilterTest.java`（headless，无 @key）。
