# java.beans.beancontext.BeanContextServicesSupport.BCSSChild — 设计案

## 1. API 功能
BCSSChild 嵌套类（extends BeanContextSupport.BCSChild），记录子 Bean 的服务请求（包私有构造器）。

## 2. Java 用户如何使用
不直接使用；BeanContextServicesSupport 内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：extends BCSChild + public。黑盒（`testBlackBox`）：子类暴露 createBCSChild 间接取得 BCSSChild 实例。
