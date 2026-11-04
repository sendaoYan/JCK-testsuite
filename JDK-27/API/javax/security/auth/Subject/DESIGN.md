# Subject 设计案

## 1. API 功能
`javax.security.auth.Subject`（final，实现 `Serializable`）表示单个实体的身份与安全属性集合：
- 构造器：`Subject()`、`Subject(boolean readOnly, Set<Principal>, Set<?> pubCredentials, Set<?> privCredentials)`。
- 身份/凭据集合：`getPrincipals()`、`getPrincipals(Class<T>)`、`getPublicCredentials()`、`getPublicCredentials(Class<T>)`、
  `getPrivateCredentials()`、`getPrivateCredentials(Class<T>)`；集合禁止 null 元素，`Class` 过滤返回副本（非 backed）。
- 只读：`setReadOnly()`、`isReadOnly()`。
- 身份传播（JDK-27）：`current()`（基于 ScopedValue，等价旧 `getSubject(AccessController.getContext())`）、
  `callAs(Subject, Callable<T>)`；废弃但仍可用：`doAs`（2 个重载）、`doAsPrivileged`（2 个重载）。
- `getSubject(AccessControlContext)` 在 JDK-27 恒抛 `UnsupportedOperationException`。
- `equals`/`hashCode`/`toString`。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/Subject.java`。

## 2. Java 用户如何使用
```java
Subject s = new Subject();
s.getPrincipals().add(new X500Principal("CN=Alice"));
s.getPublicCredentials().add(cert);
s.setReadOnly();
Subject current = Subject.callAs(s, () -> Subject.current()); // 在动作内取得 s
```
典型链路：`new Subject → getPrincipals().add/getPublicCredentials().add → getPrincipals(Class) 类型过滤 →
setReadOnly → callAs/doAs 绑定身份 → 动作内 Subject.current()`。

## 3. 开源代码实际用法调研
- JDK 自带测试（本测试调研过这些文件）：
  - `test/jdk/javax/security/auth/Subject/CurrentSubject.java`：递归验证 `current`/`callAs`/`doAs`/`doAsPrivileged`
    在动作内绑定指定 Subject、动作外复位为 null，null subject 亦绑定 null。
  - `.../CallAsWithScopedValue.java`：`Subject.current` 基于 ScopedValue；`callAs` 同线程可见、子线程继承语义。
  - `.../DoAsTest.java`：`callAs` 内 `Subject.current() == subject`（多次循环确定）。
  - `.../Generic.java`：`doAs`/`doAsPrivileged` 的泛型返回值传播。
- JDK 源码：`getSubject` 直接 `throw new UnsupportedOperationException`；`callAs` 用 `ScopedValue.where`，
  检查异常包装为 `CompletionException`；`doAs(PrivilegedExceptionAction)` 检查异常包装为 `PrivilegedActionException`；
  `SecureSet` 禁止 null（`Objects.requireNonNull`）、只读时 add/iterator.remove 抛 `IllegalStateException`，
  `remove` 仅在实际命中元素时才经 iterator 抛 `IllegalStateException`。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`SubjectTest.java` 的 `testBlackBox()`，54 处 check）：
- 空构造/集合 add/contains/remove/size 与 `Class` 类型过滤（principal、public/private credential）。
- null 元素拒绝（add/contains/remove → `NullPointerException`）。
- 只读：`setReadOnly` 后 add/remove（命中元素）→ `IllegalStateException`；只读构造器同。
- 类型过滤集合是副本（clear 不影响内部集合）。
- equals/hashCode（空相等、同主/凭据相等、异主不等、非 Subject 不等）；`toString` 含主名。
- 身份传播：`current()` 初始 null、`callAs`/`doAs`/`doAsPrivileged` 绑定与复位、null 绑定、
  `callAs` 检查异常→`CompletionException`、`doAs(PrivilegedExceptionAction)` 检查异常→`PrivilegedActionException`、
  null action→`NullPointerException`、`getSubject`→`UnsupportedOperationException`。
白盒补齐：`public final`、实现 `Serializable`、父类 `Object`。
