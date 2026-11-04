# java.util.StringTokenizer 测试设计

## 1. API 功能
- 构造器：`StringTokenizer(str)`、`(str, delim)`、`(str, delim, returnDelims)`。
- 分词：`countTokens`、`hasMoreTokens`、`nextToken()`、`nextToken(delim)`。
- Enumeration 视图：`hasMoreElements`、`nextElement`。

## 2. Java 用户如何使用
- 分割：`new StringTokenizer("a b c", " ").nextToken()`。
- 计数：`st.countTokens()`。
- 返回分隔符：`new StringTokenizer(s, ",", true)`。
- 兼容老代码：`while (st.hasMoreTokens()) { ... }`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/SerializablePermission.java`：用 StringTokenizer 解析权限名。
- JDK 源码 `java/net/URLConnection.java`：解析 header 参数列表。
- JDK 源码 `java/net/SocketPermission.java`：解析权限字符串。
- 广泛用法：遗留 CSV/配置解析（现多被 `String.split` 取代）。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、实现 Enumeration、关键方法存在。
- 黑盒（固定字符串）：
  - countTokens/hasMoreTokens/nextToken/nextToken(delim) 组合与耗尽抛 NoSuchElementException。
  - 默认空白分隔符、returnDelims 返回分隔符。
  - hasMoreElements/nextElement 与耗尽异常。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：- | testContracts |
