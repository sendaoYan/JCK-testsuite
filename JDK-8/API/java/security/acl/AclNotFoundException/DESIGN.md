# java.security.acl.AclNotFoundException — 设计案

## 1. API 功能
查找 Acl 失败时抛出的受检异常（已弃用包，JDK-17 起移除）。直接 extends Exception，**只有无参构造器**（无 (String)/(Throwable) 变体），message 恒 null。

## 2. 使用方式
`throw new AclNotFoundException()`；调用方 catch 后无消息可读，只能凭类型判断。

## 3. 参考来源
- javadoc `java.security.acl.AclNotFoundException`。
- `javap`：唯一公共构造器为无参；`getConstructors().length == 1`。

## 4. 测试用例设计 + 易错规范细节
- 构造器矩阵：恰 1 个公共构造器且无参（与多数 security 异常有 (String) 变体不同——这是本类契约点）。
- message 恒 null、cause 默认 null、initCause 一次成功/二次 IllegalStateException、getSuppressed 初始空、fillInStackTrace 返回 this、toString 以全类名开头。
- 层级：Exception 子类但**不在** GeneralSecurityException/IOException 树内（`isInstance` 断言，编译期不可 instanceof 的跨树关系用 Class.isInstance）。
- 序列化：round-trip 类保持、message null；私有静态 serialVersionUID 字段与 `ObjectStreamClass.lookup` 值一致（反射白盒读）。
- 易错点：异常间跨继承树 instanceof 在 javac 里可能直接编译失败（可证明不相交类型），断言要用 `Class.isInstance`。
