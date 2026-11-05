# jdk.jshell.JShell.Builder 设计案

## 1. API 功能
JShell 构建器：in/out/err/console/tempVariableNameGenerator/idGenerator/remoteVMOptions/compilerOptions/executionEngine(两种)/fileManager/build。

## 2. Java 用户如何使用
JShell.builder().executionEngine("local").build()；注入回调观察引擎行为。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 流式链每步返回 this。
- 注入闭环：tempVariableNameGenerator → 临时变量名/可跨 snippet 引用；idGenerator → 自定义 id（注意启动 snippet 已消耗 index 0，首个用户 snippet 是 1）。
- local 引擎下 snippet stdout 走进程 System.out（builder.out 不会重定向 local 引擎输出）；用 System.setOut 临时捕获（finally 恢复）。
- executionEngine(provider, params) 用 LocalExecutionControlProvider 实测；remoteVMOptions/fileManager 恒等函数不破坏编译。
