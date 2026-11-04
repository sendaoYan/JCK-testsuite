# Destroyable 设计案

## 1. API 功能
`javax.security.auth.Destroyable` 是标记/能力接口，对象（如凭据）可选实现以支持销毁敏感内容。
JDK-27 中两个方法均为 default：
- `default void destroy() throws DestroyFailedException`（默认抛 `DestroyFailedException`）。
- `default boolean isDestroyed()`（默认返回 false）。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/Destroyable.java`。

## 2. Java 用户如何使用
```java
class MyCred implements Destroyable {
    private boolean gone;
    public void destroy() { gone = true; }
    public boolean isDestroyed() { return gone; }
}
MyCred c = new MyCred();
c.destroy();
c.isDestroyed(); // true
```
典型链路：`new 实现类 → isDestroyed() 判断状态 → destroy() 清除 → isDestroyed() 再次确认`。

## 3. 开源代码实际用法调研
- JDK 源码：`Destroyable.destroy()` 默认实现直接 `throw new DestroyFailedException()`；`isDestroyed()` 默认 `return false`。
- JDK 自带测试 `test/jdk/javax/security/auth/Destroyable/KeyDestructionTest.java`：销毁实现把内部引用置空并翻转标志，
  供 `Subject` 的私有凭据集合使用（调研该目录确认存在该用例）。
- `javax.security.auth.x500.X500PrivateCredential` 是 JDK 内的典型实现（destroy 置空 cert/key/alias）。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`DestroyableTest.java` 的 `testBlackBox()`）：
- 默认实现：`isDestroyed()` false、`destroy()` 抛 `DestroyFailedException`。
- 自定义实现：初始未销毁 → `destroy()` 后 `isDestroyed()` true（组合状态翻转）。
白盒补齐：`public` 接口、无父类、含 `destroy`/`isDestroyed`；两方法为 `default`、
`destroy` 返回 void 且声明抛 `DestroyFailedException`、`isDestroyed` 返回 boolean。
