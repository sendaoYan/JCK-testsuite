# javax.swing.plaf.synth.SynthConstants — DESIGN

## 1. API 功能
`SynthConstants` 定义 Synth 组件状态位（public interface，无父接口）。`ENABLED`/`MOUSE_OVER`/`PRESSED`/`DISABLED`/`FOCUSED`/`SELECTED`/`DEFAULT`，位域 `1<<0`…`1<<10`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthConstants.java`（ENABLED=43、MOUSE_OVER=47、PRESSED=52、DISABLED=56、FOCUSED=61、SELECTED=65、DEFAULT=70）。

## 2. Java 用户如何使用
作为 `SynthContext` 构造器的状态参数与状态组合：`new SynthContext(c, r, style, SynthConstants.ENABLED | SynthConstants.FOCUSED)`；`SynthContext.getComponentState()` 返回组合后的位值。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:142`：`getComponentState()` 返回构造时传入的 state。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java` 各 `get*ForState` 用 `(state & ...)` 位运算判断当前状态。

## 4. 测试用例设计（黑盒部分，`SynthConstantsTest.testBlackBox`）
1. 7 个常量精确值：1/2/4/8/256/512/1024。
2. 位或组合精确值（ENABLED|FOCUSED==257 等）。
3. 位与不相交（ENABLED&DISABLED==0、FOCUSED&SELECTED==0）。
