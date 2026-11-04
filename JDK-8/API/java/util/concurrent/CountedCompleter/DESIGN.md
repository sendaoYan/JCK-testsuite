# java.util.concurrent.CountedCompleter 设计案（JDK-8）

## 1. API 功能
按 pending 计数触发的完成传播型 `ForkJoinTask`。构造器 `(completer)`/`(completer, initialPendingCount)`/无参（根）；计数原语 `getPendingCount`/`setPendingCount`/`addToPendingCount`/`compareAndSetPendingCount`/`decrementPendingCountUnlessZero`；完成传播 `tryComplete`/`propagateCompletion`/`firstComplete`/`nextComplete`/`quietlyCompleteRoot`/`getRoot`；钩子 `onCompletion(caller)`/`onExceptionalCompletion(ex, caller)`；`getCompleter`/`complete(T)`（经 setRawResult）/`helpComplete`。

## 2. Java 用户如何使用
- MapReducer sibling 惯用法：internal 节点 `setPendingCount(1); right.fork(); left.compute();`；leaf 计算后 `tryComplete()`；onCompletion 中 `caller != this` 时聚合 `child.result + child.sibling.result`。
- **tryComplete 语义：pending 非零 → 减一返回不完成；pending 已为零 → 触发 onCompletion 并向 completer 传播**。
- `onExceptionalCompletion` 返回 false 可阻断向 completer 的异常传播（completer 保持未完成）。
- `propagateCompletion` 只减计数/传播，不调用 onCompletion。

## 3. 开源代码实际用法调研
- `java.util.stream.AbstractTask`（并行流 reduce/collect 的任务树）就是 CountedCompleter 惯用法。
- JDK 自带测试：`tck/CountedCompleterTest.java` 覆盖 pending 矩阵、传播与异常路径。

## 4. 测试用例设计 + 自动化实现
场景（83 断言）：
1. 白盒：abstract/extends FJT/3 个构造器/17 个方法名循环检查/compute abstract。
2. 计数原语：initialPendingCount 反映；decrementPendingCountUnlessZero 返回减前值（3→2、1→0、0 恒 0 不为负）；CAS 成败；set/addTo。
3. tryComplete 状态机：2→1 不完成、1→0 不完成、0 → 完成（源码级语义逐档断言）。
4. getRoot 三级链 + completer 链。
5. propagateCompletion：root pending=1 + 两子；第一次减到 0 不完成、第二次完成；onCompletion 恒 0 次。
6. quietlyCompleteRoot：root pending=5 保持 5、root isDone、grand 不完成。
7. firstComplete/nextComplete：pending=2 → null 且减 1；pending=0 → this；nextComplete 链到 root；无 completer → null + quietlyComplete。
8. complete("X") 注入：经 setRawResult、compute 0 次、onCompletion 恰 1 次、invoke 返回注入值。
9. 经典树形求和：1..100 == 5050（sibling 惯用法）；onCompletion 调用数 == 2*leaves-1 == 31（每节点恰一次）。
10. 异常中继：root pending=1 fork 子任务；子抛 boom → onExceptionalCompletion 恰 1 次（ex==boom、caller==child）、root abnormal、invoke 抛 ISE；propagate=false 分支 completer 不完成。

易错规范细节：根任务的 completer 必须正确接线（child 构造传 root），否则 pending 永不归零挂死；聚合在 onCompletion 而非 join；onCompletion 计数 = 2*leaves-1 是精确断言（30 次重复稳定）。

实现：`CountedCompleterTest.java`（83 断言）。优先级：tryComplete/传播状态机 > 树形聚合正确性 > 异常传播分支。
