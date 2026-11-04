# java.io.SerializablePermission 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(String name)`、`(String name, String actions)`（actions 被忽略，getActions() 恒 ""）。
- 目标名：`enableSubclassImplementation`、`enableSubstitution`（历史校验已移除，任意名可构造，但 implies 为全名等价比较）。
- 继承 BasicPermission：name null→NPE("name can't be null")、空→IAE("name can't be empty")。
- @Deprecated(forRemoval=true, since="25")。

## 2. Java 用户如何使用
- SecurityManager 时代对 ObjectOutputStream 子类化/替换的授权检查（现已随 SecurityManager 移除而废弃，仅作策略描述符）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 构造器（无名字白名单校验，BasicPermission 委托）— `jdk25u-dev/src/java.base/share/classes/java/io/SerializablePermission.java:75,92`。

## 4. 测试用例设计 + 自动化实现（SerializablePermissionTest.testBlackBox，探针 P11 先行）
1. 两个文档目标名构造、getName 精确、getActions()==""。
2. implies：同名 true、异名 false、AllPermission/null false；"enable*" 不通配（探针实证，与 BasicPermission 通配名语义不同）。
3. null 名→NPE 精确消息、空名→IAE 精确消息。
4. 两参构造忽略 actions（"" 且与同名单参 equals）。
5. equals/hashCode 契约；序列化往返相等。
- 健壮性: @SuppressWarnings("removal")；纯对象。

产出：`SerializablePermissionTest.java`（白盒 + 黑盒共 23 断言）。
