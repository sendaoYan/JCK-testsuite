# java.util.concurrent.RecursiveAction 设计案（JDK-8）

## 1. API 功能
无返回值的 `ForkJoinTask` 子类（`RecursiveAction extends ForkJoinTask<Void>`）：抽象 `protected void compute()`；public 无参构造；`getRawResult()` 声明返回 `Void` 且恒 null。副作用型分治任务基类。

## 2. Java 用户如何使用
- 继承并实现 compute：阈值内直接处理，否则二分 `invokeAll(left, right)`。
- 结果通过共享可变状态（数组、AtomicLong）累积。
- `invoke()`/`fork()`/`join()`/`reinitialize()` 语义继承 FJT。

## 3. 开源代码实际用法调研
- 经典教程/框架示例：数组原地变换（sort/doubler）、图遍历、批量 IO 清理任务。
- JDK 自带测试：`tck/ForkJoinTaskTest.java` 以 RecursiveAction 驱动 fork/join/invokeAll 场景。

## 4. 测试用例设计 + 自动化实现
场景（38 断言）：
1. 白盒：abstract/extends ForkJoinTask/public 无参构造/compute abstract void/getRawResult 返回 Void。
2. getRawResult 生命周期：执行前/后/join/get/invoke 恒 null。
3. invokeAll(t1,t2) 计数 2 且返回前 isDone；空变体（数组/varargs/空集合）无异常。
4. Doubler 分治：数组 1..100 原地翻倍 → 2..200（固定结果）。
5. MaxFinder 分治：CAS AtomicLong 收敛到固定最大 1000。
6. 异常传播：同线程 invoke/join/getException == 原对象；跨线程 rebuilt + cause==原对象。
7. fork+reinitialize 复用：外部 fork+join 恰跑 1 次；reinit 后再跑 1 次共 2 次。
8. invokeAll 集合形式：5 个 action 全部执行且返回集大小 5。

易错规范细节：数组结果固定校验（禁随机）；CAS 收敛循环模式（AtomicLong max）；共用池 fork 的任务必须小且 join 收尾。

实现：`RecursiveActionTest.java`（38 断言）。优先级：分治副作用正确性 > rawResult 恒 null 契约 > 异常/复用。
