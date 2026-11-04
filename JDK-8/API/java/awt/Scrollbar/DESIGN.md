# java.awt.Scrollbar 行为测试设计

## 1. API 功能
构造：`Scrollbar()`/`(int orientation)`/五参 `(orientation,value,visible,minimum,maximum)`；值域：`getValue/setValue`、`getMinimum/setMinimum`、`getMaximum/setMaximum`、`getVisibleAmount/setVisibleAmount`（getVisible 别名）；增量：`getUnitIncrement/setUnitIncrement`（setLineIncrement 别名）、`getBlockIncrement/setBlockIncrement`（setPageIncrement 别名）；`getOrientation/setOrientation`、`setValueIsAdjusting`、AdjustmentListener。

## 2. Java 用户如何使用
```java
Scrollbar s = new Scrollbar(Scrollbar.VERTICAL, 0, 10, 0, 100);
s.addAdjustmentListener(e -> {});
```
构造矩阵 → clamp 规则 → 增量钳制 → 监听器。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Scrollbar.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractScrollbar()`）。
对应 `ScrollbarTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 非法 orientation（5/-1）在 3 个构造器与 setOrientation 均抛精确 IAE（illegal scrollbar orientation）。
- ctor clamp：value<min 抬到 min；value>max-visible 压回；min>max 时 max 抬到 min+1。
- 增量钳制：`setUnitIncrement(0/-5)`、`setBlockIncrement(0/-5)` 不抛、钳到 1；默认 unit=1、block=visible。
- `setValues` 一站式 clamp 矩阵（同上三条规则且 min 提升会连带移动 value）；`setValue` clamp 到 [min, max-visible]。
- 默认 value 0/min 0/max 100/visible 10；getVisible 与 getVisibleAmount 别名。
- 同一 AdjustmentListener 重复注册计数 2；程序化 setValue 不派发 AdjustmentEvent。
