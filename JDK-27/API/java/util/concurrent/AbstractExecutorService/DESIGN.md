# java.util.concurrent.AbstractExecutorService 设计案

## 1. API 功能
抽象执行器服务基类。实现 `submit`/`invokeAll`/`invokeAny` 模板方法，子类只需实现 `execute` 与生命周期方法。

## 2. Java 用户如何使用
自定义 Executor 时继承它：实现 `execute`，即免费获得 `submit(Callable)`/`invokeAll`/`invokeAny` 的 Future 语义。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor`/`ForkJoinPool` 均继承；`test/jdk/java/util/concurrent/AbstractExecutorService/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`AbstractExecutorServiceTest.java`（`testWhiteBox` + `testBlackBox`）。
