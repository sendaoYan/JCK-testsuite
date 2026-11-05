# javax.xml.bind.JAXBPermission 契约覆盖设计（JDK-8）

## 1. API 功能
JAXB 专属安全权限，`final class extends BasicPermission`，用于 `DatatypeConverter.setDatatypeConverter` 的 SecurityManager 检查（权限名 `"setDatatypeConverter"`）。无安全管理器时全程不触发检查。JDK-8 仅有 `(String name)` 一个构造器（无 actions 形态）。

## 2. 使用方式
```java
SecurityManager sm = ...; // 需要时才检查
JAXBPermission p = new JAXBPermission("setDatatypeConverter");
p.implies(p);            // true（BasicPermission 语义）
p.getActions();          // ""（BasicPermission 忽略 actions）
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/JAXBPermission.html
- JSR 222 (JAXB 2.2) 规范安全章节
- OpenJDK 8u RI 源码 `javax/xml/bind/JAXBPermission.java`
- 本仓库测试：`JAXBPermissionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 结构：final 类、直接父类 BasicPermission、仅 1 个公共构造器、`newPermissionCollection()` 非 null。
- `getActions()` 精确返回空串 `""`（实测锁定）。
- `toString()` 精确格式 `("javax.xml.bind.JAXBPermission" "setDatatypeConverter")`（实测锁定）。
- implies 矩阵：自名 true / 异名 false（双向）/ `"*"` 蕴含具体名 true / 具体名不蕴含 `"*"` false。
- equals/hashCode 值语义：同名等价且 hash 相等、与 null/异名/异类型不等。
- 负例：`ctor(null)` → **NullPointerException**；`ctor("")` → **IllegalArgumentException**（BasicPermission 行为，实测锁定）；无 no-arg 构造器。
- 易错点：不要给 `new JAXBPermission("name","actions")`——JDK-8 无此构造器；getActions 永远是空串而非 null。
