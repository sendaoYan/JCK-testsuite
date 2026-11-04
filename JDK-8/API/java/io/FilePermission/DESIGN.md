# java.io.FilePermission 测试方案 (DESIGN)

## 1. API 功能
- final Permission 子类；`(String path, String actions)` 构造；action 规范化为 read,write,execute,delete。

## 2. Java 用户如何使用
- 安全管理器按路径与动作集授权文件访问；支持 "/-" 递归与 "/*" 平铺与 "<<ALL FILES>>"。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FilePermission.java`。

## 4. 测试用例设计 + 自动化实现（FilePermissionTest.java，纯黑盒）
黑盒场景:
1. action 规范顺序 + 重复折叠。
2. 非法/空 action 拒绝（消息 "invalid permission: ..."）。
3. "/-" 递归 implies 深路径、非兄弟、非目录自身、动作子集。
4. "/*" 仅平铺直接子项。
5. "<<ALL FILES>>" implies 任意路径。
6. 精确文件与动作超集/子集 implies。
7. 非相关类型与 null 不 implies。
8. equals/hashCode 按名+掩码。
9. 名字中孤立 "*" 作字面量。
10. newPermissionCollection 可用。
11. 序列化往返相等。
- 健壮性: 无反射白盒（JDK 8 无 @Deprecated(forRemoval)）。

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测，行为全同构），新增 testCtorContracts 等 7 个方法，
> 断言 30 → 80。

### 新增维度清单
1. **构造契约**：path null → NPE "name can't be null"；**actions null → IllegalArgumentException "invalid actions mask"（不是 NPE，双侧实测）**；"" → IAE 同消息；纯空白 "   " → IAE "invalid permission:    "；"readx" → IAE "invalid permission: readx"；"read,,write" / "read,"（空 token）→ IAE。
2. **动作串规范化**：大小写不敏感（"READ"/"Read"→"read"）；token 周围空白 tolerated（" read , WRITE "→"read,write"）；重复折叠 + 乱序恢复。
3. **implies 矩阵扩展**："/-"（根递归）implies 一切绝对路径与其它递归授权；"<<ALL FILES>>" implies 相对路径 "foo"；"*"（cwd 平铺）implies "foo" 不 implies "dir/foo"；"/tmp/-" implies 其自身；"/tmp/*" 不 implies "/tmp/-" 与 "/tmp/x/-"（平铺不授予递归形式）；Linux 路径大小写敏感（"/tmp/A" ⊄ "/tmp/a"）；动作全集 implies 每个单动作、execute 不 implies read；前缀不越界（"/tmp/x" ⊄ "/tmp/xy"）。
4. **equals/hashCode**："<<ALL FILES>>" 相互 equals；动作不同不等；同构造 hashCode 相等。
5. **PermissionCollection 聚合**：同路径 read+write 并集 implies "read,write"；聚合 "/b/-" 递归授权；无关路径/缺失动作不 implies；implies(null) false；elements() 枚举；空集合 implies 无。
6. **toString**：含类名、路径、规范动作串。
7. **序列化深测**：round-trip 后 equals/hashCode、getActions 规范化保持、implies 语义（子路径/深层/排除动作/排除路径）全部保持。

### 负例精确异常表（双侧实测，消息一致）
| 操作 | 异常 | 消息 |
|---|---|---|
| new FilePermission(null,"read") | NullPointerException | "name can't be null" |
| new FilePermission("/a",(String)null) | IllegalArgumentException | "invalid actions mask" |
| new FilePermission("/a","") | IllegalArgumentException | "invalid actions mask" |
| new FilePermission("/a","   ") | IllegalArgumentException | "invalid permission:    " |
| new FilePermission("/a","readx") | IllegalArgumentException | "invalid permission: readx" |
| new FilePermission("/a","read,,write") | IllegalArgumentException | "invalid permission: read,,write" |
| new FilePermission("/a","read,") | IllegalArgumentException | "invalid permission: read," |

### JDK-8 vs JDK-27 版本差异
- **PermissionCollection.elements() 同路径合并**：JDK-8 每次	add 独立成元素（同路径 2 次加 1 次异路径 → 3 元素）；JDK-27 将同路径授权合并为单元素（组合动作，→ 2 元素）。union implies 语义双侧一致。测试按版本各自断言元素数。
- 其余（构造消息/implies/equals/序列化）双侧一致。
