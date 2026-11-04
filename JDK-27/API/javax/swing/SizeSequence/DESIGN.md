# javax.swing.SizeSequence 设计案

## 1. API 功能
`SizeSequence` 维护一组大小并快速计算位置/索引。方法分组：
- 构造：空/计数+初值/int[]。
- 查询：`getSize/getSizes/getPosition/getIndex`。
- 修改：`setSize/setSizes/insertEntries/removeEntries`。

## 2. Java 用户如何使用
布局管理器用它追踪行/列大小与位置：
```java
SizeSequence seq = new SizeSequence(new int[] { 50, 100, 50 });
int y = seq.getPosition(row);
```
典型链路：构造 → `getSize/getPosition/getIndex` → `setSize`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SizeSequence.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SizeSequence.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验普通类。
- 行为（黑盒）：`testBlackBox()` 覆盖四种构造、`insertEntries/setSize/removeEntries/setSizes`、`getSize/getPosition/getIndex/getSizes` 的确定性结果。
