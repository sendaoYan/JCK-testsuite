# javax.swing.Scrollable 设计案

## 1. API 功能
`Scrollable` 接口让组件支持滚动。方法分组：
- 视口：`getPreferredScrollableViewportSize`。
- 增量：`getScrollableUnitIncrement/getScrollableBlockIncrement`。
- 跟踪：`getScrollableTracksViewportWidth/getScrollableTracksViewportHeight`。

## 2. Java 用户如何使用
组件实现 `Scrollable` 让 `JScrollPane` 滚动：
```java
public class MyPanel extends JPanel implements Scrollable {
    public Dimension getPreferredScrollableViewportSize() { return new Dimension(100, 200); }
    ...
}
```
典型链路：`getPreferredScrollableViewportSize` → `getScrollableUnitIncrement`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JTextArea.java` 实现 `Scrollable`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTextArea.java`）。
- `JList`/`JTable` 等滚动组件实现该接口。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 匿名实现覆盖全部 5 个方法并校验返回值。
