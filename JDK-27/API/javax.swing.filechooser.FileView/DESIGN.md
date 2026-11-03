# javax.swing.filechooser.FileView 设计案

## 1. API 功能
为 `JFileChooser` 提供文件的 UI 元信息（名称、描述、类型、图标、可否遍历）的抽象类；`JFileChooser` 先问用户 `FileView`，返回 `null` 时回退到 L&F 视图。

- **构造器**：`protected FileView()`（仅供子类）。
- **方法（默认均返回 `null`）**：`getName(File)`、`getDescription(File)`、`getTypeDescription(File)`、`getIcon(File)`、`isTraversable(File)`（返回 `Boolean`，`null` 表示交给文件系统判断）。

## 2. Java 用户如何使用
子类化重写所需方法，未重写的保持 `null` 表示「让 L&F 决定」：

```java
class NameOnlyView extends FileView {
    public String getName(File f) { return f == null ? null : f.getName(); }
}
chooser.setFileView(new NameOnlyView());
```

典型组合链：`new 子类 → getName(文件) → 未重写的 getIcon/getDescription 返回 null → JFileChooser 回退`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/filechooser/FileView.java`（类注释）：`FileView` 返回 `null` 时 `JFileChooser` 用 L&F 视图补齐；参考示例指向 `yourJDK/demo/jfc/FileChooserDemo/ExampleFileView.java`。
- `src/java.desktop/share/classes/javax/swing/JFileChooser.java`：`getFileView()`/`setFileView(FileView)` 保存用户视图。

## 4. 测试用例设计 + 自动化实现
`FileViewTest.testBlackBox()` 覆盖：
- 空子类的 5 个方法默认返回 `null`（`getName/getDescription/getTypeDescription/getIcon/isTraversable`）。
- 重写 `getName` 的子类：返回文件名、处理 `null`、未重写方法仍 `null`。
- 多态：`FileView` 引用跨文件调用。
- 白盒保留结构断言（父类 `Object`、抽象、5 个方法）。
