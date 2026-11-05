# javax.management.relation.RelationTypeSupport — 设计案

## 1. API 功能
- RelationType 的标准实现（Serializable，serialVersionUID=4611072955724144607）。
- 构造器：public `(String typeName, RoleInfo[])`；protected `(String typeName)`（子类路径，配合 protected `addRoleInfo(RoleInfo)`）。
- 查询：`getRelationTypeName()`、`getRoleInfos()`（**每次返回新 ArrayList，元素亦为 RoleInfo 拷贝**）、`getRoleInfo(String)`。
- 声明锁：经 `RelationService.addRelationType` 声明后不可再 addRoleInfo（RuntimeException）。

## 2. Java 用户如何使用
```java
RelationTypeSupport t = new RelationTypeSupport("myType",
        new RoleInfo[]{ new RoleInfo("owner", MyClass.class.getName(), true, true, 1, 1, "d") });
List<RoleInfo> roles = t.getRoleInfos();  // 可任意改动，不影响 t
relationService.addRelationType(t);       // 声明后 t 被锁定
```
子类化路径：`class MyType extends RelationTypeSupport { MyType(String n){ super(n); addRoleInfo(...); } }`。

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RelationTypeSupport（构造异常表与 addRoleInfo 的 RuntimeException 说明）。
- OpenJDK 源码：`~/git/jdk8u-dev` `src/share/classes/javax/management/relation/RelationTypeSupport.java`（防御拷贝/声明锁实现），只借鉴思路、代码自写。

## 4. 测试用例设计（易错规范细节）
- 构造校验（双侧消息实测）：null 类型名/null 数组 → IAE "Invalid parameter."；空数组 → InvalidRelationTypeException "No role info provided."；
  null 元素 → IRE "Null role info provided."；同名角色 → IRE "Two role infos provided for role <name>"。
- **防御拷贝双通道**：getRoleInfos() 每次返回新 List（clear 返回列表后二次 get 不受影响）；getRoleInfo(name) 返回的 RoleInfo 不是构造传入的实例（值全等：name/readable/writable/min/max/refClass/description 七属性）。
- protected 路径：子类暴露 addRoleInfo——新名 size+1、重名 IRE、null IAE、失败后 size 不变；protected (null) 构造 → IAE。
- 声明锁：addRelationType 后 addRoleInfo → RuntimeException "Relation type cannot be updated as it is declared in the Relation Service."；未声明类型不受限。
- 序列化：round-trip 保留 typeName、全部 roleInfos、getRoleInfo 契约（未知名仍 RoleInfoNotFoundException）；SUID 数值锁定。
