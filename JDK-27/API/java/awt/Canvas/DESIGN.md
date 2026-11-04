# java.awt.Canvas 黑盒行为测试设计

## ① API 功能分组
- 构造：`Canvas()`、`Canvas(GraphicsConfiguration)`。
- 绘制：`paint(Graphics)`、`update(Graphics)`。
- 缓冲：`createBufferStrategy/getBufferStrategy`。
- 继承 Component：`setBounds/setBackground`。

## ② Java 用户用法
```java
Canvas c = new Canvas();
c.setSize(100, 80);
frame.add(c);
c.getBufferStrategy();
```
组合链路：构造 → paint/update（内存 Graphics）→ add 到显示 Frame → 几何/背景断言。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Canvas.paint` 默认清屏（`Canvas.java`）。
- `Canvas.getBufferStrategy` 在无 peer 时返回 null（`Canvas.java`）。
- 游戏/图形程序常继承 Canvas 重写 `paint` 绘制。

## ④ 测试用例设计 + 自动化实现
- 无 peer 时 `getBufferStrategy` 为 null → `testBlackBox`。
- `paint/update` 用内存 Graphics 调用不抛异常 → `testBlackBox`。
- add 到显示 Frame 后 displayable/showing + setBounds/setBackground 往返 → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- 无 peer 前 `getBufferStrategy()` 返回 null（不抛）。
- `paint(null)`/`update(null)` 抛精确 NullPointerException（JDK-27 消息指向 Graphics.clearRect）。
- getAccessibleContext 返回 `Canvas$AccessibleAWTCanvas` 类型。
- 序列化往返：副本是不同对象（identity 区分），name 与 background 保留。
