# WebServicePermission 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.WebServicePermission 是 JAX-WS 发布权限（JSR 224 §7.11.2）：final 类 extends java.security.BasicPermission，目标名 "publishEndpoint"（安全管理器装启时 Endpoint.publish 需持有）。无动作语义——BasicPermission 的 actions 恒空串。

## 2. 使用方式
典型路径：`Endpoint.publish(url, impl)` 内部 `securityManager.checkPermission(new WebServicePermission("publishEndpoint"))`。本测试：结构面 + 名字语义 + actions 恒空 + implies 全矩阵（自身/异名/通配/层级/null/跨类）+ equals/hashCode + 构造负例。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServicePermission.html）、JSR 224 (JAX-WS 2.2)、java.security.BasicPermission 语义、本仓库 WebServicePermissionTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：final、extends BasicPermission（→Permission→Serializable/Guard）；恰 2 public 构造器 (String)/(String,String)；**无参构造不存在**（getConstructor() → NoSuchMethodException，实测）。
- 名字语义："publishEndpoint" 回读；任意名/通配 "*" 合法；**空名 "" → IllegalArgumentException "name can't be empty"**（实测，BasicPermission 拒绝）。
- **actions 恒空串（P0）**：单参与双参构造 getActions() 均返回 ""（动作串被规范化丢弃）；名字不受 actions 参数影响。
- implies 矩阵（P0）：自身 true；异名 false；前缀相似名（"publishOther"）false；"*" 蕴含一切（含 publishEndpoint 与任意名）而具体名不蕴含 "*"；层级 "a.*" 蕴含 "a.b" 不蕴含 "b.a"；implies(null) false；**跨权限类同名不蕴含**（RuntimePermission("publishEndpoint") 不被蕴含——类须匹配）；actions 差异不影响 implies。
- equals/hashCode：同名等（actions 忽略）+hashCode 相等；异名不等；null/异类不等；toString 含类名与名字。
- 构造负例：null 名（单参/双参）→ NPE；尾点名 "a." → IAE（或该实现接受，均记录）。
- 坑：BasicPermission 是抽象类不能直接 new（跨类对比用 RuntimePermission）；权限相等与 actions 串完全无关。
