# LocatorImpl — 设计案

## 1. API 功能
Locator 可变实现：无参/(Locator) 拷贝两构造 + 四 setter。

## 2. Java 用户如何使用
手工定位器（单元测试、事件缓存回放）。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：新对象位置 0/0、id null（JDK 实现：字段未按文档初始化为 -1，两侧一致）；四 setter 往返；拷贝构造是快照非活视图。
