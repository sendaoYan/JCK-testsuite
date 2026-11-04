# java.io.UnsupportedEncodingException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；消息为不支持的字符集名。

## 2. Java 用户如何使用
- 使用 String.getBytes/构造、InputStreamReader/OutputStreamWriter 的按名重载时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/UnsupportedEncodingException.java`。

## 4. 测试用例设计 + 自动化实现（UnsupportedEncodingExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. String.getBytes(bogus) 抛且消息为字符集名。
3. new String(bytes, bogus) 抛。
4. InputStreamReader/OutputStreamWriter 未知字符集抛。
5. 合法名（UTF-8/US-ASCII）往返（对比）。
6. 作为 IOException 捕获 + 序列化保留消息。
- 健壮性: 固定 bogus 字符集名；无反射白盒。
