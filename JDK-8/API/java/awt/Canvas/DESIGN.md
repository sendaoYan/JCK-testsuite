# java.awt.Canvas 行为测试设计

## 1. API 功能
构造：`Canvas()`/`Canvas(GraphicsConfiguration)`；绘制：`paint`/`update`；缓冲：`createBufferStrategy`/`getBufferStrategy`；继承 Component 属性与 AccessibleAWTCanvas。

## 2. Java 用户如何使用
```java
Canvas c = new Canvas();
c.setBackground(Color.BLUE);
frame.add(c);
```
构造 → 设属性 → add 到显示 Frame → paint/buffer 断言。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Canvas.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractCanvas()`）。
对应 `CanvasTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 无 peer 前 `getBufferStrategy()` 返回 null（不抛）。
- `paint(null)`/`update(null)` 抛精确 NullPointerException（JDK-27 消息指向 Graphics.clearRect）。
- getAccessibleContext 返回 `Canvas$AccessibleAWTCanvas` 类型。
- 序列化往返：副本是不同对象（identity 区分），name 与 background 保留。
