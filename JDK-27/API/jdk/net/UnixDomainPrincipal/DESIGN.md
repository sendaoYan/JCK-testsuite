# jdk.net.UnixDomainPrincipal 设计案

## 1. API 功能
Unix 域套接字主体（record）：user/group。

## 2. Java 用户如何使用
new UnixDomainPrincipal(user, group)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/UnixDomainPrincipal.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+user/group/equals/hashCode/toString。

---

## 深化(2026-11-05)

### API 功能（深化后全貌）
- `record UnixDomainPrincipal(UserPrincipal user, GroupPrincipal group)`（since 16）。
- compact 构造器对两参 `Objects.requireNonNull` → 任一为 null 抛 NPE。
- 组件序：user、group；生成 equals/hashCode/toString（`UnixDomainPrincipal[user=..., group=...]`）。
- 作为 `ExtendedSocketOptions.SO_PEERCRED` 的选项类型（unix-domain socket 对端凭证，只读）。

### 实测记录（/tmp/probe-dnet/ProbeUdp.java，jdk-27.0.1.0.2+1）
- lookupPrincipalByName(user.name) 两次 → equals true、hashCode 相等（sun.nio.fs.UnixUserPrincipals$User，toString 即名字）。
- lookupPrincipalByName("root") 与当前用户不等（防御：uname=="root" 时跳过该断言）。
- lookupPrincipalByGroupName("root") 成功（"root" 组）；ctor(up,null)/(null,gp)/(null,null) → NPE。
- 未知用户 → UserPrincipalNotFoundException；**getName() 返回 ""（空串，nss 路径不回填名字）**、getMessage() null；null 名 → NPE。
- toString 精确格式 `UnixDomainPrincipal[user=yansendao, group=root]`；getRecordComponents = [user:UserPrincipal, group:GroupPrincipal]。

### 测试设计（11→45 断言）
record 结构（isRecord/组件名序/类型/父类 Record）/ 构造器 NPE 三路 / UserPrincipal 真实查找契约（名字回读、重复查找等值、hashCode、toString、负例 null/String/异名用户）/ lookup 异常面（未知用户异常类型、null NPE）/ 值语义全表（user()/group() 同一性、toString 精确串、equals 自等对称、null/String 负例、hashCode 稳定与等值一致、换 user/换 group 不等）/ SO_PEERCRED.type() == UnixDomainPrincipal 联动 / 组查找防御分支（root→uname 逐个回退，全失败时仅测 user 侧）。

### 易错细节 + 8/27 差异
- **JDK-8 无 UnixDomainPrincipal（since 16）**，8 侧只有 SocketFlow 对偶差集。
- UserPrincipalNotFoundException.getName() 在 27 实测回 ""（不是所查名字）——断言只能查异常类型，别锁名字。
- 组查找防御：先 "root" 再当前用户名，再 daemon/bin/sys/adm 找异名组做不等断言；找不到就跳过，不能死。
- record 的 toString 格式是 `类名[user=.., group=..]`（无包名前缀），与 Arrays.toString 风格不同。
- equals 比较前构造对照实例也会先过 requireNonNull——null 负例必须 try/catch 包住构造器本身。
