# Permission — 设计案

## 1. API 功能
权限抽象基类：getName/getActions 抽象入口、implies(Permission) 抽象、newPermissionCollection() 工厂、checkGuard(Object)（Guard 接口）、equals/hashCode/toString 约定。

## 2. 使用方式
具名子类（如 NamedPerm）验证基类行为：`new NamedPerm("name")`；checkGuard 在无 SecurityManager 时为 no-op（JDK-8）。

## 3. 参考来源
- javadoc `java.security.Permission`。
- JCK runtime-8b `api/java_security/Permission/`。

## 4. 测试用例设计 + 易错规范细节
- 构造器：null name 合法（getName()==null）；空 name 合法。
- 子类必须覆写 implies/equals/hashCode/toString/getActions（本测试用 NamedPerm 提供全量实现验证基类委托）。
- toString 约定格式 `("class" "name" "actions")` —— 抽象基类未实现，由子类负责，测试断言子类按该格式输出。
- checkGuard：JDK-8 无 SecurityManager 时 no-op 不抛；JDK-27（JEP 486）恒抛 SecurityException "checking permissions is not supported" —— 分叉。
- newPermissionCollection() 默认 null；覆写后返回非空集合。
- serialVersionUID = -5636570222231596674L；Permission 可序列化（子类实现 Serializable 后往返保留 name/actions）。
- 易错点：getActions 由子类决定返回 "" 还是 null；equals 由子类决定恒等或按值。
