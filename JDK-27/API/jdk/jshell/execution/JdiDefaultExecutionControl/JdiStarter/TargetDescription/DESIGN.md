# jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter.TargetDescription（record） 设计案

## 1. API 功能
启动结果 record：vm(com.sun.jdi.VirtualMachine) + process(java.lang.Process)。

## 2. Java 用户如何使用
JdiStarter.start 的返回值；JdiDefaultExecutionControl 用 vm 建立 JDI 控制。

## 3. 开源代码实际用法调研
来源：JdiDefaultExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
白盒：isRecord、两个 record 组件的名称/类型/访问器、规范构造器 (VirtualMachine, Process)。

