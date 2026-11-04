# javax.swing.Timer 测试设计

## 1. API 功能
Swing 计时器：delay/initialDelay/repeats/coalesce 配置，事件经共享定时线程派发到 EDT。

## 2. Java 用户如何使用
`new Timer(200, e -> tick()).start()`；一次性任务 setRepeats(false)。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Timer.java`；
TimerQueue.java；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 构造器不校验：new Timer(0/-1,null) 均不抛（**易错**：负 delay 可构造，setDelay(-5) 才抛 IAE "Invalid delay"）。
- 事件语义：when>0、source 是 Timer、actionCommand 往返、**在 EDT 派发**。
- start 幂等；stop 后 isRunning false；restart=stop+start；移除监听器后不再投递（有界等待验证）。
- **易错**：首跳按 initialDelay 而非 delay（10ms initialDelay vs 10s delay 用 5s 有界等待区分）。
- getLogTimers/setLogTimers 静态往返。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

