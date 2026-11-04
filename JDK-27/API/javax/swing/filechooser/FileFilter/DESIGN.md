# javax.swing.filechooser.FileFilter 设计案

## 1. API 功能
文件过滤器抽象类，`JFileChooser` 用它判断哪些文件可显示/可选。

- **构造器**：`protected FileFilter()`（仅供子类）。
- **抽象方法**：`boolean accept(File f)`、`String getDescription()`。

## 2. Java 用户如何使用
子类化实现 `accept`/`getDescription`，交给 `JFileChooser`：

```java
FileFilter filter = new FileFilter() {
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(".txt");
    }
    public String getDescription() { return "Text files"; }
};
chooser.addChoosableFileFilter(filter);
```

典型组合链：`new 子类 → accept(候选文件) 循环过滤 → getDescription 展示`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/filechooser/FileNameExtensionFilter.java`：`public final class FileNameExtensionFilter extends FileFilter`，是 JDK 提供的具体实现。
- `test/jdk/javax/swing/JFileChooser/8152677/SelectAllFilesFilterTest.java`：通过 `new FileNameExtensionFilter("Text files", "txt")` 构造过滤器参与测试（间接消费 `FileFilter` 类型）。

## 4. 测试用例设计 + 自动化实现
`FileFilterTest.testBlackBox()` 覆盖：
- 匿名子类 `accept`/`getDescription` 的返回值。
- `accept` 大小写、其他扩展名、`null` 防御。
- 多态：对文件数组循环 `accept` 统计匹配数。
- 白盒保留结构断言（父类 `Object`、抽象、2 个方法 `accept`+`getDescription`）。
