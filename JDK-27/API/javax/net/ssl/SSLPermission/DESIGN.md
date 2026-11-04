# javax.net.ssl.SSLPermission 黑盒行为测试设计

## 1. API 功能
final 类，extends BasicPermission（已弃用并标记移除）：`SSLPermission(String name)`、`(String,String actions)`；actions 被忽略。

## 2. Java 用户如何使用
权限名如 setHostnameVerifier/getSSLSessionContext；SecurityManager 时代用于授权。

## 3. 开源代码实际用法调研
JDK 文档列出合法 permission 名；现代 JDK 不再依赖 SecurityManager。

## 4. 测试用例设计 + 自动化实现
黑盒：getName、implies 自反、wildcard implies、getActions 空。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
