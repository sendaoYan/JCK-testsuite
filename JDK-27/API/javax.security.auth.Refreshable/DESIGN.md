# Refreshable 设计案

## 1. API 功能
`javax.security.auth.Refreshable` 是标记/能力接口，对象（如有时效的凭据/token）可选实现以刷新有效期。
- `boolean isCurrent()`（抽象）：是否仍然有效。
- `void refresh() throws RefreshFailedException`（抽象）：刷新/延长有效期。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/Refreshable.java`。

## 2. Java 用户如何使用
```java
class MyToken implements Refreshable {
    private boolean current;
    public boolean isCurrent() { return current; }
    public void refresh() { current = true; }
}
MyToken t = new MyToken();
if (!t.isCurrent()) t.refresh();
```
典型链路：`isCurrent() 判断 → refresh() 刷新 → isCurrent() 再次确认`。

## 3. 开源代码实际用法调研
- JDK 源码：`isCurrent`/`refresh` 均为抽象方法；`refresh` 声明 `throws RefreshFailedException`。
- 该接口在 Kerberos 等场景由 `KerberosTicket`/`KerberosKey`（JDK 内部，`java.security` 之外）实现。
- JDK 自带测试目录下未发现独立的 `Refreshable` 结构测试；本测试以 JDK 源码契约为准（两个抽象方法签名）。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`RefreshableTest.java` 的 `testBlackBox()`）：
- 自定义实现：初始 `isCurrent()` false → `refresh()` 后置标志 → `isCurrent()` true。
- 失败路径：`refresh()` 抛 `RefreshFailedException` 时异常被正确传播。
白盒补齐：`public` 接口、无父类、含 `isCurrent`/`refresh`；两方法为 abstract、
`isCurrent` 返回 boolean、`refresh` 返回 void 且声明抛 `RefreshFailedException`。
