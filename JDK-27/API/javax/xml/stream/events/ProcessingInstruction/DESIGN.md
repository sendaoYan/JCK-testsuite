# javax.xml.stream.events.ProcessingInstruction — 设计案

## 1. API 功能
javax.xml.stream.events.ProcessingInstruction 接口。实例/声明方法：getTarget, getData。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.ProcessingInstruction() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.ProcessingInstruction）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ProcessingInstructionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ProcessingInstructionTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 工厂：target/data 全等、码 3、编码 `<?t d?>`、data=null 容忍（编码 `<?t?>`）、target=null 容忍。
- 解析：序言/元素内 PI target+data 精确；无数据 `<?tgt?>` getData()=""（与工厂 null 不同）。
- 闭环：工厂 PI→writer→重解析 target/data 全等；asCharacters→CCE。接口无 getPITarget（那是 stream reader 方法）。
