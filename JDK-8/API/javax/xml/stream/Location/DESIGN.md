# javax.xml.stream.Location — 设计案（JDK-8）

## 1. API 功能
位置接口（getLineNumber/getColumnNumber/getCharacterOffset/getPublicId/getSystemId）。

## 2. 使用方式
由 XMLStreamReader.getLocation()/异常 getLocation() 获得；也可自定义实现。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 接口面
getLineNumber/getColumnNumber/getCharacterOffset/getPublicId/getSystemId 五方法；自定义实现 -1/null 哨兵值透传。
### 实现者实测
固定多行文档（"<r>\n  <a/>\n</r>"）：root SE 在 (1,4)；a 的 SE 在 line 2、offset 10（**精确值锁定**）；Reader 输入 publicId/systemId 为 null；toString 多行含 "Line number = 2"/"Column number ="/"CharacterOffset" 字段。
### 错误定位
未闭合元素错位 → 异常 Location 精确指到第 3 行。

### 断言计数
见 LocationTest.java 末尾 PASS 输出（本轮深化后 21 断言，双侧只增不减）。
