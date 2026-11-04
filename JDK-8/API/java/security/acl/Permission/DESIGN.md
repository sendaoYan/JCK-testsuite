# java.security.acl.Permission — 设计案

## 1. API 功能
ACL 权限标记接口（已弃用包，JDK-17 起移除）。仅重声明 `equals(Object)` 与 `toString()` 两个方法，无其它成员、无父接口。

## 2. 使用方式
自定义实现（如按名比较的 Record 型权限）放入 AclEntry；AclEntry.checkPermission 以 equals 判定。**与 java.security.Permission（类）完全无关**——不同种类、互不可赋值。

## 3. 参考来源
- javadoc `java.security.acl.Permission`。
- `javap java.security.acl.Permission`（2 方法）与 `java.security.Permission`（抽象类）对照。

## 4. 测试用例设计 + 易错规范细节
- 白盒：声明方法集恰为 {equals, toString}；无父接口；equals(Object)→boolean、toString()→String、均无参（equals 的 Object 参数除外）无受检异常。
- 独立性断言：`java.security.Permission.class.isAssignableFrom(acl.Permission)` 与反向均 false；一个是接口一个是类；记录型实现 `instanceof java.security.Permission` 为 false（javac 对可证明不相交类型拒绝 instanceof 字面量，须用 Class.isInstance）。
- 值语义（记录型 RecPerm）：equals 同名 true/异名 false/null false/异类型 false/自反；hashCode 与 equals 配套；toString 携带权限名。
- 互操作：RecPerm 作为 AclEntry 的权限通货——add/check/remove 按值匹配（等值不同实例亦命中）。
- 易错点：接口未重声明 hashCode，但实现必须自带（否则 Set 去重失效）。
