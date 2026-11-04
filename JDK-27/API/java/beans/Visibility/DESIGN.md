# java.beans.Visibility — 设计案

## 1. API 功能
GUI 可用性接口。needsGui/dontUseGui/okToUseGui/avoidingGui。

## 2. Java 用户如何使用
无 GUI 环境下 Bean 声明避免使用 GUI。

## 3. 开源代码实际用法调研
JDK 源码 `Visibility.java`；beancontext 实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现 needsGui/dontUseGui/okToUseGui/avoidingGui 状态切换。
