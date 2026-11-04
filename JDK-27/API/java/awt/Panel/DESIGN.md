# java.awt.Panel 黑盒行为测试设计

## ① API 功能分组
- 构造：`Panel()`、`Panel(LayoutManager)`。
- 布局：默认 `FlowLayout`，可指定其它布局。
- 继承 Container：`add/getComponent/getComponentCount`、`setSize`。

## ② Java 用户用法
```java
Panel p = new Panel(new BorderLayout());
p.add(new Button("b"), BorderLayout.CENTER);
frame.add(p);
```
组合链路：构造（含布局）→ add 子组件 → add 到显示 Frame → 几何断言。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Panel()` 调用 `this(new FlowLayout())`（`Panel.java`）。
- `Panel(LayoutManager)` 调用 `setLayout(layout)`（`Panel.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认布局 FlowLayout + 指定布局 BorderLayout → `testBlackBox`。
- add 子组件后 getComponentCount/getComponent → `testBlackBox`。
- add 到显示 Frame 后 displayable/showing + setSize 往返 → `testBlackBox`。
