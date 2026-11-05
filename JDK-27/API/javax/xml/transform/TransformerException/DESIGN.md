# javax.xml.transform.TransformerException — 设计案

## 1. API 功能
javax.xml.transform.TransformerException 类。实例/声明方法：getLocator, setLocator, getException, getCause, initCause, getMessageAndLocation, getLocationAsString, printStackTrace。

## 2. Java 用户如何使用
new(...) → getException/getCause/getLocator/getLocationAsString。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.TransformerException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerExceptionTest.testBlackBox()`）覆盖：三种构造 + locator/cause。

## 5. 深化（本轮新增）
- 全 6 构造器链 round-trip；initCause 单次、getException==getCause。
- 位置串精确：`"; SystemID: http://base/x.xml; Line#: 3; Column#: 5"`；无 locator 时 getLocationAsString()==null、getMessageAndLocation()==裸消息。
- 真实触发：未闭合标签编译失败消息含 "unclosed"（与 JDK-8 同词）。
