# org.w3c.dom.ls.LSParser — 设计案

## 1. API 功能
org.w3c.dom.ls.LSParser 接口。常量：ACTION_APPEND_AS_CHILDREN, ACTION_REPLACE_CHILDREN, ACTION_INSERT_BEFORE, ACTION_INSERT_AFTER, ACTION_REPLACE（共 5 个）。 实例/声明方法：getDomConfig, getFilter, setFilter, getAsync, getBusy, parse, parseURI, parseWithContext, abort。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.LSParser() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.LSParser）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LSParserTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LSParserTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 常量：ACTION_APPEND_AS_CHILDREN=1、ACTION_REPLACE_CHILDREN=2、ACTION_INSERT_BEFORE=3、ACTION_INSERT_AFTER=4、ACTION_REPLACE=5。
- 行为：parse(stringData) 建全树且解析器可复用；同步模式 busy=false/async=false；parseWithContext → `NOT_SUPPORTED_ERR(9)` 且不动上下文节点；parse(null) → NullPointerException；getDomConfig 可设 comments=false 并在解析时剔除注释；setFilter/getFilter 往返（缺省 null）。
