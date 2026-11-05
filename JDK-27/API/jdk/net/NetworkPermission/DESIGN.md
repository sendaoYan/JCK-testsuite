# jdk.net.NetworkPermission 设计案

## 1. API 功能
网络权限（extends BasicPermission）。

## 2. Java 用户如何使用
new NetworkPermission(name) / (name, actions)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/NetworkPermission.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+getName/getActions/equals。

---

## 深化(2026-11-05)

### API 功能（深化后全貌）
- `@Deprecated(since="25", forRemoval=true)`（Security Manager 移除后该权限失去控制意义）。
- 构造器两个：`(String name)` / `(String name, String actions)`；name null → NPE、空串 → IllegalArgumentException；actions 恒被忽略。
- JDK-8 文档化 target（setOption.SO_FLOW_SLA / getOption.SO_FLOW_SLA）在 27 已删表，但 BasicPermission 接受任意非空名。
- serialVersionUID 两侧同为 -2012939586906722291L（ObjectStreamClass.lookup 可读，不受模块封装影响）。

### 实测记录（/tmp/probe-dnet/ProbePerm.java，jdk-27.0.1.0.2+1）
- null/空串 × 两构造器 → NPE/IllegalArgumentException（与 8 侧一致）。
- implies：自/同名 true、异名/null false；`*` 蕴含任意具体名、`setOption.*` 蕴含 `setOption.SO_FLOW_SLA`；与 RuntimePermission 同名互不蕴含（BasicPermission 检查 getClass 相等）。
- 序列化 round-trip 保留 equals/hashCode/getName/getActions。
- newPermissionCollection → BasicPermissionCollection，add 后 implies 生效。
- @Deprecated 注解 since="25"、forRemoval=true。
- 反射读 private serialVersionUID 字段被模块系统拦（InaccessibleObjectException），必须走 ObjectStreamClass。

### 测试设计（11→56 断言）
非法 target 矩阵 / 历史 target 回读 / implies 全矩阵（含通配符与跨类） / equals+hashCode 契约 / actions 忽略 / 序列化 round-trip / svUID 直锁 / newPermissionCollection / toString / @Deprecated(since,forRemoval) 精确断言 / 父类与 public final 修饰。

### 易错细节 + 8/27 差异
- getActions() 恒 ""；不同 actions 字符串构造的两个实例 equals true。
- equals("同名字符串") false 是易漏负例。
- 差异：8 无 @Deprecated 且文档化 target 表存在；27 反之。行为面（NPE/IAE/implies/通配/序列化/svUID）完全一致。
