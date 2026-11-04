# java.io.SerializablePermission 测试方案 (DESIGN)

## 1. API 功能
- final BasicPermission 子类；`(String)` 与 `(String, String)` 构造；actions 恒为空（被忽略）。

## 2. Java 用户如何使用
- 安全管理器授权序列化扩展（enableSubclassImplementation / enableSubstitution）。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/SerializablePermission.java`；BasicPermission 名校验。

## 4. 测试用例设计 + 自动化实现（SerializablePermissionTest.java，纯黑盒）
黑盒场景:
1. 两个文档化目标名构造、actions 空。
2. implies 精确同名匹配、不跨类型、非 null。
3. null/空名校验消息。
4. 两参构造忽略 actions。
5. equals/hashCode 契约。
6. "enable*" 不作通配。
7. 序列化往返相等。
- 健壮性: 无反射白盒（JDK 8 无 @Deprecated(forRemoval)）。

## 深化追加（契约级）

新增维度（`testWildcardMatrix`…`testDeprecation`，断言 21 → 48）：
- **通配矩阵**：`*` implies 一切；`a.*` implies `a.b`、`a.b.c`，**不** implies `a`；内部 `*` 为字面量（`a*b` 仅匹配自身）。
- **类层次**：extends BasicPermission → Permission → 无 checked 异常的 implies；类 final、Serializable。
- **集合**：newPermissionCollection().add / implies / setReadOnly 可用；PermissionCollection 自身不参与 implies 判定。
- **名字接受面**：`a/b`、`a..b`、`serialFilter` 均合法（BasicPermission 名规则）；大小写敏感（`A` 不 implies `a`）。
- **表面**：toString=类名+名字；getActions 恒 ""；null actions 构造器合法；actions 参数被忽略。
- **弃用标注**：JDK-8 侧**未**弃用。
- **负例矩阵**：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| new SerializablePermission(null) | NullPointerException | `name can't be null` |
| new SerializablePermission("") | IllegalArgumentException | `name can't be empty` |

版本差异：JDK-27 将 SerializablePermission 标记 @Deprecated(since="25", forRemoval=true)，JDK-8 无弃用；其余行为一致。
