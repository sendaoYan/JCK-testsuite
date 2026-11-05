# ParserConfigurationException — 设计案

## 1. API 功能
受检异常（extends Exception）：工厂无法提供请求的配置时由 newDocumentBuilder/newSAXParser 抛出。仅 () 与 (String) 两个公开构造器（无 cause 构造器）。

## 2. Java 用户如何使用
catch (ParserConfigurationException e) 处理工厂配置失败；e.getMessage() 获取原因。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的工厂测试声明 throws PCE；典型触发是 setFeature 后工厂无法满足。

## 4. 测试用例设计 + 自动化实现
黑盒：()/(String) 消息与 cause、null 消息、可序列化（Exception 通道）、公开构造器数量==2、initCause 仍可用、标准 validating 配置不抛。
