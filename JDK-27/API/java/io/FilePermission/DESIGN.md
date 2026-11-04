# java.io.FilePermission 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(String path, String actions)`，actions ∈ {read,write,execute,delete} 逗号组合（任意序/重复→规范化）；非法 action→IllegalArgumentException("invalid permission: ...")。
- 路径语法：目录+"/-"（递归）、目录+"/*"（平铺）、`<<ALL FILES>>`（一切）。
- `implies(Permission)`（路径包含 + action 子集）、`equals/hashCode`（name+mask）、`getActions()`（规范序 "read,write,execute,delete"）、`newPermissionCollection()`、Serializable。
- @Deprecated(forRemoval=true, since="25")。

## 2. Java 用户如何使用
- 安全策略文件 `grant { permission java.io.FilePermission "/tmp/-", "read,write"; }`；代码内构造做路径级授权检查（SecurityManager 移除后仍作为策略描述符存在）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 构造与 implies/getMask 实现 — `jdk25u-dev/src/java.base/share/classes/java/io/FilePermission.java:318,361,474`（<<ALL FILES>>、cpath 比较、构造器）。
- 官方策略测试 — `jdk25u-dev/test/jdk/java/io/FilePermission/`。

## 4. 测试用例设计 + 自动化实现（FilePermissionTest.testBlackBox，探针 P11 先行）
1. actions 规范化：乱序→"read,write"；全集→"read,write,execute,delete"；重复折叠。
2. 非法 action（read,swim）→精确 IAE 消息；空 actions→IAE。
3. "/tmp/-" implies 子/深层后代、不 implies 兄弟/目录自身/超集 action；"/*" 只含平铺。
4. "<<ALL FILES>>" implies 一切；超集 action implies 子集、反之不行。
5. 与 AllPermission/null 皆 false；名字中孤立 '*' 非通配（a* 只匹配字面 a*）。
6. equals/hashCode 契约（乱序输入相等同哈希）；PermissionCollection.add 后 implies；序列化往返相等。
- 健壮性: @SuppressWarnings("removal") 包裹；纯对象无文件系统访问。

产出：`FilePermissionTest.java`（白盒 + 黑盒共 36 断言）。

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测），新增 testCtorContracts 等 7 个方法，断言 38 → 88。
> @Deprecated(forRemoval=true)（本 JDK 契约一部分，白盒已断言）。

### 新增维度清单（与 JDK-8 侧同构）
1. 构造契约：path null NPE "name can't be null"；**actions null → IAE "invalid actions mask"（非 NPE）**；""/纯空白/未知动作/空 token 各 IAE（"invalid permission: …"）。
2. 动作串规范化：大小写不敏感、空白 tolerated、重复折叠。
3. implies 矩阵扩展："/-" 根递归、"<<ALL FILES>>" 含相对路径、"*" 平铺仅一层、"/tmp/-" 自身、平铺不授予递归形式、大小写敏感、动作子集、前缀不越界。
4. equals/hashCode："<<ALL FILES>>" 对、动作区分、同构造同哈希。
5. PermissionCollection：同路径并集 implies、递归聚合、implies(null) false、elements 枚举、空集合。
6. toString 含类名/路径/规范动作。
7. 序列化深测：equals/hashCode/getActions/implies 语义全保持。

### 负例精确异常表（双侧实测，消息一致）
（同 JDK-8 侧 DESIGN 的表。）

### JDK-8 vs JDK-27 版本差异
- **PermissionCollection.elements() 同路径合并**：JDK-27 合并为单元素组合动作（同路径 2 次加 1 次异路径 → 2 元素）；JDK-8 保持 3 元素。union implies 双侧一致。
- 其余行为双侧一致。
