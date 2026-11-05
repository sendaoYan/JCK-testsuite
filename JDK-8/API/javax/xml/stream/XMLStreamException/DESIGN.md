# javax.xml.stream.XMLStreamException — 设计案（JDK-8）

## 1. API 功能
受检异常（extends Exception；5 个构造重载；getLocation/getNestedException；消息带 Location 装饰）。

## 2. 使用方式
解析错误时抛出/捕获；可手工构造携带 Location/Nested。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 构造链 5 形态
(String)/(String,Location)/(String,Location,Throwable)/(String,Throwable)/(Throwable)/()。
- 带 Location 时 getMessage() 被**装饰**为 "ParseError at [row,col]:[l,c]\nMessage: 原文"（两版本一致）；getLocation() 保持实例同一性。
- getNestedException() 是 JDK 特有链（非 cause）；(Throwable) 单参的 getMessage 取 cause.toString()。
- **JDK-8 vs JDK-27 差异（关键）**：`new XMLStreamException(String,Location,Throwable)` 的 getCause()——JDK-8 为 **null**（nested 只经 getNestedException()），JDK-27 为 **该 Throwable**。双侧各自锁定。
- initCause 在未设 ctor 时可用。
### 解析器实际抛出
格式错 → XMLStreamException 携带 Location（行号=1）+ 非空消息。

### 断言计数
见 XMLStreamExceptionTest.java 末尾 PASS 输出（本轮深化后 21 断言，双侧只增不减）。
