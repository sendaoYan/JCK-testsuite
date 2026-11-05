# jdk.jshell.JShell.Subscription 设计案

## 1. API 功能
onSnippetEvent/onShutdown 返回的不透明订阅句柄；unsubscribe 撤销。

## 2. Java 用户如何使用
订阅→eval→回调计数→unsubscribe→静默。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 多订阅独立投递；逐个 unsubscribe 后其余仍活；重复 unsubscribe 无害。
- 事件计数语义：普通 eval 1 次；方法覆写 eval 2 次（OVERWRITTEN+VALID）；一次输入含两条声明仍只算 1 个 snippet 事件。
- 易错：`eval("int a=1; int b=2;")` 是单个 snippet（1 事件），不是 2 个。
