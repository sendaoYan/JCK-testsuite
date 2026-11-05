# jdk.jshell.spi.ExecutionControl.ExecutionControlException 设计案

## 1. API 功能
SPI 异常家族根（checked，extends Exception）；ExecutionControl 接口共 10 个嵌套类型（9 异常 + ClassBytecodes）。

## 2. Java 用户如何使用
子类封闭于四类通道：terminate（EngineTermination/ClassInstall）、internal（Internal/NotImplemented）、run（Run→User/Resolution/Stopped）。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
家族树边表断言须用外层接口 ExecutionControl.getDeclaredClasses()（在嵌套类上取 getDeclaredClasses 是 0）；全部 public static。

