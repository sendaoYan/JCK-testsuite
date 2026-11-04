# java.beans.beancontext.BeanContextSupport.BCSChild — 设计案

## 1. API 功能
BCSChild 嵌套类（implements Serializable），内部保存子 Bean 与代理 peer（包私有构造器）。

## 2. Java 用户如何使用
不直接使用；BeanContextSupport.add 时内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：public + Serializable + 非 public 构造器。黑盒（`testBlackBox`）：通过子类暴露 bcsChildren() 间接取得 BCSChild 实例。
