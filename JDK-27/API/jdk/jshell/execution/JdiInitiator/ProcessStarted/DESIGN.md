# jdk.jshell.execution.JdiInitiator.ProcessStarted（受保护嵌套接口） 设计案

## 1. API 功能
受保护嵌套回调：debuggee 进程启动后、JDI 连接建立前被调用，processStarted(Process)。

## 2. Java 用户如何使用
JdiInitiator.runListenProcess 内部回调（等待进程退出以诊断启动失败）。

## 3. 开源代码实际用法调研
来源：JdiInitiator.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
白盒：isInterface + protected 修饰 + 恰好一个抽象方法 processStarted(Process)。

