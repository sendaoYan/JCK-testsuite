# FactoryConfigurationError — 设计案

## 1. API 功能
Error（非 Exception）：JAXP 工厂发现机制失败（系统属性/服务文件指向不可实例化的类）时抛出。四个构造器 ()/(String)/(Exception)/(Exception,String)；getException/getCause/getMessage。

## 2. Java 用户如何使用
newInstance(className, loader) 显式指定实现类时捕获；或捕获 Error 处理部署错误。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp/catalog/工厂测试用系统属性指向坏类断言 FactoryConfigurationError。

## 4. 测试用例设计 + 易错规范细节
黑盒：四构造器消息/异常/cause 矩阵（(Exception) 的 message == cause.toString()）；(Exception,String) 消息覆盖；() 双 null；newInstance("no.such.Factory") → 消息含类名。
