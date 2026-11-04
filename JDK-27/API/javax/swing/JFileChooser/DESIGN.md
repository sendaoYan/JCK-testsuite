# javax.swing.JFileChooser 设计案

## 1. API 功能
`JFileChooser` 是文件选择组件。方法分组：
- 选择文件：`getSelectedFile/setSelectedFile/getSelectedFiles/setSelectedFiles/getCurrentDirectory/setCurrentDirectory`。
- 对话框：`getDialogType/setDialogType/setDialogTitle/getDialogTitle/setApproveButtonText/getApproveButtonText`。
- 过滤器：`setFileFilter/getFileFilter/addChoosableFileFilter/removeChoosableFileFilter`。
- 模式：`setFileSelectionMode/getFileSelectionMode/setMultiSelectionEnabled/isMultiSelectionEnabled`。

## 2. Java 用户如何使用
用户嵌入或弹出文件选择器：
```java
JFileChooser chooser = new JFileChooser();
chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
    File file = chooser.getSelectedFile();
}
```
典型链路：`new JFileChooser` → `setSelectedFile` → `getDialogType/setFileFilter`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JFileChooser.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JFileChooser.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JComponent` + `Accessible`。
- 行为（黑盒，headful）：`testBlackBox()` 覆盖选择文件、对话框类型/标题/按钮、文件过滤器 add/remove、模式开关、常量，并在 EDT 上 `new JFrame + setVisible(true) + dispose()` 真实显示。
