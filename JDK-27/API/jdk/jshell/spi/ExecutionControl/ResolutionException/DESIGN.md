# jdk.jshell.spi.ExecutionControl.ResolutionException 设计案

## 1. API 功能
未解析引用（RECOVERABLE 代码被调用）：id() 标识声明 snippet；消息固定 "resolution exception: <id>"。

## 2. Java 用户如何使用
远端 SPIResolutionException 转换而来；对应 JShell 层 UnresolvedReferenceException。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
消息含 id（含负数）；构造器 setStackTrace；id 0 合法；与 SPIResolutionException 无类型关系（各自独立层次）。

