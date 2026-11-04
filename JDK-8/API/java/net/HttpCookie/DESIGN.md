# HttpCookie 设计文档（JDK-8 侧）

## 一、API 功能

表示单个 HTTP Cookie 的键值及域/路径/有效期/安全等属性，可解析与序列化 Set-Cookie 头。

## 二、Java 用户如何使用

用户 new HttpCookie(name, value) 或用 HttpCookie.parse(header) 解析，设置 setDomain/setPath/setMaxAge/setSecure/setHttpOnly。

## 三、开源代码实际用法调研

Cookie 解析与生成在 Web 客户端/服务器与爬虫中广泛使用。

## 四、测试用例设计

构造与 getName/getValue/getDomain/getPath/getMaxAge/getSecure/isHttpOnly；parse 单/多头；domainMatches；toString 生成头格式；equals/hashCode；clone。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（228 条）覆盖：** 默认值矩阵；构造合法性矩阵（tspecials 仅 `,; `，控制字符非法）；版本猜测（expires→v0、version/max-age/set-cookie2→v1，猜测值覆盖显式 version）；domainMatches 21 组矩阵（单级子域匹配、深层拒绝）；equals/hashCode 精确公式；toString 四形态（v0/v1 × 有/无值，v1 附加 $Path/$Domain/$Port）；setDomain 小写化；clone；原始 9 条断言原样保留于 testLegacy。

**易错细节（探针实证，JDK-8 与 JDK-27 存在差异）：**
- `hasExpired()` 对负 maxAge 在 JDK-8 返回 **true**（JDK-27 false）。
- 过期属性冲突：JDK-8 **先出现者胜**（maxage;expires→100，expires;maxage→负数）；JDK-27 恒 max-age 胜。
- 过去时间 expires → maxAge 为**负数**（JDK-27 为 0）。
- 非法 max-age 属性值抛 `IllegalArgumentException("Illegal cookie max-age attribute")`（JDK-27 忽略）。
