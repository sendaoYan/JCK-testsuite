# NetPermission 设计文档

## 一、API 功能

`java.net.NetPermission` 网络 API 安全检查用的 BasicPermission：无动作、只有名字。9 个文档化名字（setDefaultAuthenticator、requestPasswordAuthentication、specifyStreamHandler、set/getProxySelector、set/getCookieHandler、set/getResponseCache），但**不做白名单校验**——任意名字可构造。

## 二、Java 用户如何使用

策略文件 `grant { permission java.net.NetPermission "setProxySelector"; }`；代码中由 SocketPermission 式 SecurityManager 检查（如 `ProxySelector.setDefault` 前 `checkPermission(new NetPermission("setProxySelector"))`）。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.security.BasicPermission`（init：null 名 NPE "name can't be null"、空名 IAE "name can't be empty"；implies 通配语义）；`java.net.NetPermission` 文档与源码；javap。

## 四、测试用例设计（94 断言）

- **白盒**：final、直接继承 BasicPermission、两个构造器、Serializable。
- **文档化名字矩阵**：9 名字 × getName/getActions("")；任意名（含 "get*"、"a*b"）可构造。
- **动作被忽略**：1 参/2 参、多动作串均 getActions()==""，equals/implies/hashCode 不受 actions 影响。
- **通配语义**："*" 蕴含一切；"a.*" 蕴含 a.b、a.b.c、a.b.*，不蕴含 a、ab；"set*" 是**字面名**非通配；大小写敏感。
- **负例**：null/空名（两构造器）精确消息；implies(null) false；implies 其他权限类型 false。
- **equals/hashCode/toString**。
- **序列化往返**：含通配。
- **PermissionCollection**：add/implies 联合/elements 计数/readonly 后 add 抛 SecurityException。

## 五、易错规范细节（探针实证）

- BasicPermission 通配只有两种形态：整体 `"*"` 或 `".*"` 结尾；`"set*"`、`"a*b"` 均按字面名处理。
- `"a.*"` **不蕴含** `"a"`（path 为 ".a" vs "a"）——常见误记。
- 名字大小写敏感、动作参数完全忽略。
- BasicPermissionCollection：setReadOnly 后 add 抛 SecurityException，elements 顺序不保证但数量确定。

## 六、版本差异（JDK-8 vs JDK-27）

- 语义一致；JDK-25 起 NetPermission 标记 deprecated for removal（仍在），JDK-27 侧测试注明并照常验证全部行为。
