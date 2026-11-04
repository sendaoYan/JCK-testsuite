# javax.naming.NamingSecurityException — 测试设计

## 1. API 功能
命名安全异常抽象基类（extends NamingException）：AuthenticationException、
AuthenticationNotSupportedException、NoPermissionException 三个具体子类的共同父类。

## 2. Java 用户如何使用
客户端以 `catch (NamingSecurityException e)` 统一处理认证/权限失败，无需逐一列举子类。

## 3. 开源代码实际用法调研
- JDK `java.naming` 源码三个子类定义；LDAP 提供方在 bind 失败抛 AuthenticationException。
- Tomcat/Spring LDAP 客户端对安全异常的细分捕获。

## 4. 测试用例设计（NamingSecurityExceptionTest.java）
- 白盒：abstract、extends NamingException、双构造器、三个子类继承关系。
- 黑盒：三个具体子类实例均可被 `NamingSecurityException` 捕获（多态桶）；普通 NamingException 不是安全异常。
