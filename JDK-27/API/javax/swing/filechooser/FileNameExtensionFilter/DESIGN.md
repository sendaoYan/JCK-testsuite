# javax.swing.filechooser.FileNameExtensionFilter 设计案

## 1. API 功能
`FileFilter` 的具体子类，按文件名扩展名（大小写不敏感）过滤，目录一律接受。

- **构造器**：`FileNameExtensionFilter(String description, String... extensions)`（扩展名为 `null`/空数组/含 `null`/空串抛 `IllegalArgumentException`）。
- **方法**：`accept(File)`、`getDescription()`、`getExtensions()`（防御性拷贝）、`toString()`。

## 2. Java 用户如何使用
```java
FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
chooser.setFileFilter(filter);
filter.accept(new File("photo.jpg"));   // true（目录亦 true）
filter.accept(new File("photo.gif"));   // false
String[] exts = filter.getExtensions(); // {"jpg","jpeg"}
```

典型组合链：`new(描述, 扩展名…) → getDescription → accept(文件) 循环 → getExtensions`。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/JFileChooser/8152677/SelectAllFilesFilterTest.java`：`FileFilter txtFilter = new FileNameExtensionFilter("Text files", "txt");`。
- `src/java.desktop/share/classes/javax/swing/filechooser/FileNameExtensionFilter.java`（类注释）：`FileFilter filter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"); fileChooser.addChoosableFileFilter(filter);`。

## 4. 测试用例设计 + 自动化实现
`FileNameExtensionFilterTest.testBlackBox()` 覆盖：
- `getDescription`/`getExtensions`（顺序 + 防御性拷贝）。
- `accept`：匹配/大小写不敏感/其他扩展名/无扩展名/点开头文件/`null`/真实目录。
- 单扩展名过滤器。
- `toString` 含描述。
- 非法参数：`null` 数组、空数组、`null` 元素、空串元素均抛 `IllegalArgumentException`。
- 白盒保留结构断言（父类 `FileFilter`、`final`）。
