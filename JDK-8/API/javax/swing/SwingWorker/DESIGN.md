# javax.swing.SwingWorker 测试设计

## 1. API 功能
后台任务框架：EDT 外 doInBackground、EDT 上 process/done、publish 中间结果、
state(PENDING/STARTED/DONE)、progress、取消语义。

## 2. Java 用户如何使用
`new SwingWorker<Result,Progress>() {...}.execute()`；done() 里 get() 取结果更新 UI。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingWorker.java`；
SwingWorkerTest 同目录探针；java.util.concurrent.FutureTask 对照；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 生命周期：初始 PENDING/progress 0；execute() 后立刻仍 PENDING；get 超时版取值；DONE；
  done 恰一次且在 EDT；doInBackground 不在 EDT。
- publish(1)/publish(2,3) 的 process 累计收 [1,2,3]。
- 属性事件："state" PENDING→STARTED→STARTED→DONE、"progress" 0→42。
- 取消：cancel(true) 返回 true、中断穿透进阻塞的 doInBackground、state 仍走 DONE、
  get() 抛 CancellationException、再次 cancel 返回 false。
- setProgress 边界：-1/101 IAE，0/100 合法；二次 execute 是 no-op。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

