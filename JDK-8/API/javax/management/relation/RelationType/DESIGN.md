# javax.management.relation.RelationType — 设计案

## 1. API 功能
- 接口：关系类型抽象，extends Serializable。
- 恰 3 个抽象方法：`getRelationTypeName()` → String、`getRoleInfos()` → `List<RoleInfo>`、`getRoleInfo(String)` → RoleInfo（throws RoleInfoNotFoundException/IllegalArgumentException）。
- 唯一参考实现：`RelationTypeSupport`。

## 2. Java 用户如何使用
```java
RelationType t = new RelationTypeSupport("myType", new RoleInfo[]{ new RoleInfo("r", "java.lang.String") });
String name = t.getRelationTypeName();
List<RoleInfo> roles = t.getRoleInfos();
RoleInfo r = t.getRoleInfo("r"); // 不存在 → RoleInfoNotFoundException
```
也可实现该接口后经 `RelationService.addRelationType` 注册为自定义关系类型。

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RelationType（方法契约与 throws 子句）。
- OpenJDK 源码：`~/git/jdk8u-dev` `src/share/classes/javax/management/relation/RelationType.java`（接口定义），只借鉴思路、代码自写。

## 4. 测试用例设计
- 白盒：接口、无父类、extends Serializable、恰 3 个声明方法、getRoleInfos 泛型返回类型精确为 `java.util.List<javax.management.relation.RoleInfo>`、getRoleInfo throws 子句含 RoleInfoNotFoundException。
- 黑盒：以 RelationTypeSupport 为实现走接口引用：getRelationTypeName/getRoleInfos/getRoleInfo 命中；未命中 → RoleInfoNotFoundException（消息精确）；
  序列化 round-trip（接口类型引用）保留实现类、类型名与角色契约。
