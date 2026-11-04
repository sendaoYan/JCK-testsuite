# java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory 设计案（JDK-8）

## 1. API 功能
函数式接口，唯一抽象方法 `ForkJoinWorkerThread newThread(ForkJoinPool pool)`；无 default 方法；静态常量 `ForkJoinPool.defaultForkJoinWorkerThreadFactory`（字段声明在 ForkJoinPool 上）。用于定制 worker 的命名、优先级、UEH、Instrumentation 埋点。

## 2. Java 用户如何使用
- `new ForkJoinPool(par, factory, handler, asyncMode)` 注入；`pool.getFactory()` 回读。
- 默认工厂产出 daemon、`ForkJoinPool-` 前缀命名的 worker，构造时绑定池；`newThread(null)` → NPE（文档要求）。
- 工厂返回 null：池无法创建 worker，任务不执行但池仍可终止。

## 3. 开源代码实际用法调研
- 监控框架（如 instrumentation agent）经工厂给 FJP worker 打标签/前缀以便线程 dump 定位。
- JDK 自带测试：`tck/ForkJoinPoolTest.java` 断言 defaultForkJoinWorkerThreadFactory 非空与 custom factory 集成。

## 4. 测试用例设计 + 自动化实现
场景（25 断言）：
1. 白盒：接口/恰一个抽象方法（函数式）/newThread 返回类型/非 default；`ForkJoinPool.class.getField("defaultForkJoinWorkerThreadFactory")` 反射读取非空 + 接口实例 + 直接常量访问同一。
2. 默认工厂：创建线程绑定池、daemon、ForkJoinPool- 前缀、getPoolIndex>=0、newThread(null) → NPE。
3. RecordingWorker 自定义工厂：RecWorker- 前缀贯穿、getPool==pool、daemon、UEH 安装、子类实例。
4. null 返回工厂：任务不执行（ran=false）、shutdownNow 后 awaitTermination true（确定性）。

易错规范细节：工厂字段用 `ForkJoinPool.class.getField` 而非接口 Class（字段声明在池上）；null 工厂场景任务永不出结果，断言用布尔标志而非等待。

实现：`ForkJoinWorkerThreadFactoryTest.java`（25 断言）。优先级：默认工厂契约 > 自定义工厂集成 > null 边界。
