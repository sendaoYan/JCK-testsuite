# jdk.jshell.spi.ExecutionControl.RunException 设计案

## 1. API 功能
运行期失败抽象基类：三个封闭子类 UserException/ResolutionException/StoppedException。

## 2. Java 用户如何使用
仅由引擎构造。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
构造器 private（反射断言）——用户不能扩展 RunException 家族；三个子类的父类边表逐一断言。

