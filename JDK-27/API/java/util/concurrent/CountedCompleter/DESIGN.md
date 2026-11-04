# java.util.concurrent.CountedCompleter 设计案

## 1. API 功能
计数完成器（抽象类，继承 ForkJoinTask）。`pending` 计数 + `tryComplete`/`propagateCompletion` 级联完成；`getCompleter`/`getRoot` 查父子。

## 2. Java 用户如何使用
分治任务里子任务完成时 `tryComplete` 递减父 pending，归零触发父完成；`getRoot` 定位根任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinTask` 框架；`test/jdk/java/util/concurrent/CountedCompleter/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`CountedCompleterTest.java`（`testWhiteBox` + `testBlackBox`）。
