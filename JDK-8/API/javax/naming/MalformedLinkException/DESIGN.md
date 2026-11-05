# javax.naming.MalformedLinkException — 测试设计

## 1. API 功能
链接名格式非法异常（extends LinkException）：`()`/`(String)` 构造器。

## 2. Java 用户如何使用
provider 无法从链接名解析出目标时抛出；客户端 catch LinkException 处理。

## 3. 开源代码实际用法调研
- `javax.naming.LinkRef.getLinkName()` 在 className 非 LinkRef 或无 LinkAddress 地址时抛 MalformedLinkException。
- JDK test/jdk/javax/naming/** 覆盖。

## 4. 测试用例设计（MalformedLinkExceptionTest.java）
- 白盒：extends LinkException、双构造器。
- 黑盒：message 保留、可被 LinkException 捕获、link 状态填充后 toString 可见、no-arg message null。
