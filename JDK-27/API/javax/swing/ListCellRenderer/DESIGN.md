# javax.swing.ListCellRenderer 设计案

## 1. API 功能
`ListCellRenderer` 接口为 `JList` 单元格提供渲染组件。方法分组：
- 渲染：`getListCellRendererComponent(JList, value, index, selected, focused)`。

## 2. Java 用户如何使用
用户实现它返回单元格组件：
```java
list.setCellRenderer(new ListCellRenderer<String>() {
    public Component getListCellRendererComponent(JList<? extends String> list,
            String value, int index, boolean selected, boolean focused) {
        return new JLabel(value);
    }
});
```
典型链路：`new ListCellRenderer(){...}` → `getListCellRendererComponent`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultListCellRenderer.java` 实现 `ListCellRenderer<Object>`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultListCellRenderer.java`）。
- `BasicListUI` 调用该接口渲染单元格。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 用匿名实现返回 `JLabel` 并校验文本传递。
