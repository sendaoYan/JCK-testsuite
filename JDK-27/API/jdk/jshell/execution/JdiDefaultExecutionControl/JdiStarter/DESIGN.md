# jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter（嵌套接口） 设计案

## 1. API 功能
debuggee 启动策略 SPI：start(ExecutionEnv, Map<String,String>, int port) 返回 TargetDescription(vm, process)。默认实现用 JdiInitiator（SocketListen 模式）。

## 2. Java 用户如何使用
new JdiExecutionControlProvider(starter) 注入自定义启动器。

## 3. 开源代码实际用法调研
来源：JdiExecutionControlProvider.java / JdiDefaultExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
白盒：单抽象方法；toGenericString 含声明类前缀（jdk...$JdiStarter.start）——startsWith 断言要含返回类型而非方法名直连；JdiExecutionControlProvider(JdiStarter) 公共构造器存在。

