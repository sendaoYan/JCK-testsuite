# java.util.concurrent.CountedCompleter 设计案

## 1. API 功能
计数完成器（抽象类，继承 ForkJoinTask）。`pending` 计数 + `tryComplete`/`propagateCompletion` 级联完成；`getCompleter`/`getRoot` 查父子。

## 2. Java 用户如何使用
分治任务里子任务完成时 `tryComplete` 递减父 pending，归零触发父完成；`getRoot` 定位根任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinTask` 框架；`test/jdk/java/util/concurrent/CountedCompleter/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（99 断言）：
1. 白盒：抽象类/继承 ForkJoinTask + 12 个核心方法存在性 + 3 个构造器 + compute 抽象 + 17 个方法名循环检查。
2. 黑盒：completer/pending 原语（setPendingCount/CAS/addTo/tryComplete 递减/complete(null) 完成）。
3. 计数原语：decrementPendingCountUnlessZero 返回减前值（3→2、1→0、0 恒 0）；CAS 成败。
4. tryComplete 状态机：2→1 不完成、1→0 不完成、0 → 完成。
5. getRoot 三级链；propagateCompletion（减计不调 onCompletion，第二次完成）；quietlyCompleteRoot（pending 保持 5）。
6. firstComplete/nextComplete 遍历原语（null/this/链到 root/根 quietlyComplete）。
7. complete("X") 注入：setRawResult 参与、compute 0 次、onCompletion 恰 1。
8. 经典树形求和 1..100==5050（sibling 惯用法）；onCompletion == 2*leaves-1 == 31。
9. 异常中继：onExceptionalCompletion 恰 1 次（ex==boom、caller==child）、root abnormal、invoke 抛 ISE；propagate=false 分支 completer 不完成。

易错规范细节：根任务 completer 接线错误会永久挂死；聚合在 onCompletion 而非 join；onCompletion 计数为精确断言（2*leaves-1）。

实现：`CountedCompleterTest.java`（99 断言）。优先级：tryComplete/传播状态机 > 树形聚合 > 异常传播分支。
