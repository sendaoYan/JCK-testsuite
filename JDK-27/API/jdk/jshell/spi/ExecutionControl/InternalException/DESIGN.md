# jdk.jshell.spi.ExecutionControl.InternalException 设计案

## 1. API 功能
引擎内部失败（找不到类/方法/字段，ex.toString() 作消息）；NotImplementedException 是其唯一内置子类。

## 2. Java 用户如何使用
DirectExecutionControl.invoke/varValue 的反射失败通道。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
无 cause；序列化保消息；线协议状态 103（RESULT_INTERNAL_PROBLEM）。

