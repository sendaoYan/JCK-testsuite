# org.w3c.dom.UserDataHandler — 设计案

## 1. API 功能
org.w3c.dom.UserDataHandler 接口。常量：NODE_CLONED, NODE_IMPORTED, NODE_DELETED, NODE_RENAMED, NODE_ADOPTED（共 5 个）。 实例/声明方法：handle。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.UserDataHandler() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.UserDataHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 UserDataHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`UserDataHandlerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
五常量（CLONED=1/IMPORTED=2/DELETED=3/RENAMED=4/ADOPTED=5）+ 真实路由：cloneNode→1（src=原 dst=克隆）、importNode→2（dst=导入副本）、renameNode→4（src=改名后节点、**dst=null**）、adoptNode→5（**dst=null**，Xerces）；NODE_DELETED 在活树上不可观测（勿测）。userData **不会**自动拷贝到克隆（handler 是应用唯一的挂钩）；adoption 后原节点 userData 仍可读；多 key 各自独立回调；null handler 数据保留但无回调。
