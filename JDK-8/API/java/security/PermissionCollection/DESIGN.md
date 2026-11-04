# PermissionCollection — 设计案

## 1. API 功能
权限集合抽象类：add(Permission)、implies(Permission)、elements()、setReadOnly()/isReadOnly()；Permission 静态域 newPermissionCollection 的配套协议。

## 2. 使用方式
自定义记录型子类（VecCollection）验证基类 add/implies/elements/setReadOnly 委托行为；配合真实 BasicPermissionCollection 验证隐含语义与只读协议。

## 3. 参考来源
- javadoc `java.security.PermissionCollection`。
- JCK runtime-8b `api/java_security/PermissionCollection/`。

## 4. 测试用例设计 + 易错规范细节
- 空集合 implies 任何权限 false；implies(null) false（不抛）。
- add(null) → IllegalArgumentException（确切异常类型）。
- setReadOnly 后 add → SecurityException（消息 "attempt to add a Permission to a readonly PermissionCollection"）；isReadOnly true；elements 仍可遍历。
- 真实 BasicPermissionCollection：BasicPermission 逐类配桶、implies 前缀/通配语义、只读后 add 抛。
- 序列化（子类可序列化时）保留 readOnly 状态与 implies 结果。
- 易错点：elements() 返回 Enumeration 顺序未规定 —— 只断言成员存在性；JDK-9+ 有 elementsAsStream，JDK-8 无。
