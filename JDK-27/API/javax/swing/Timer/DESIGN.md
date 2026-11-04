# javax.swing.Timer 设计案

## 1. API 功能
`javax.swing.Timer` 在 EDT 上周期触发动作。方法分组：
- 启动/停止：`start/stop/restart/isRunning`。
- 参数：`setDelay/getDelay/setInitialDelay/getInitialDelay/setRepeats/isRepeats/setCoalesce/isCoalesce/setActionCommand/getActionCommand`。
- 日志：`setLogTimers/getLogTimers`。

## 2. Java 用户如何使用
用户用它定时刷新 UI：
```java
Timer timer = new Timer(1000, e -> repaint());
timer.start();
```
典型链路：`new Timer(delay, listener)` → `start/isRunning` → `stop`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Timer.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Timer.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 用 `CountDownLatch` + 超时等待验证定时触发（不 sleep 断言精确值）、`start/stop/isRunning`、延迟/重复/合并/动作命令 get/set。
