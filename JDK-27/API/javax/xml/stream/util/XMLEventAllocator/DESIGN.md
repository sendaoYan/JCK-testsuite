# javax.xml.stream.util.XMLEventAllocator — 设计案

## 1. API 功能
javax.xml.stream.util.XMLEventAllocator 接口。实例/声明方法：newInstance, allocate。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.util.XMLEventAllocator() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.XMLEventAllocator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventAllocatorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventAllocatorTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 发现机制闭环（JDK-27 实测生效）：setProperty("javax.xml.stream.allocator",alloc) 不抛且 getProperty 同一实例；
  createXMLEventReader 后 7 事件全部为分配器 marker（== 判定）、allocate 调用 7 次。
- 直接 allocate(定位 reader) 返回自定义事件；allocate(reader,consumer) 喂 consumer；newInstance 独立副本工作。
- 负例：setProperty 传 String 不抛，createXMLEventReader 惰性抛 ClassCastException（消息含 XMLEventAllocator）。
