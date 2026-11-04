# HttpCookie 设计文档

## 一、API 功能

表示单个 HTTP Cookie 的键值及域/路径/有效期/安全等属性，可解析与序列化 Set-Cookie 头。

## 二、Java 用户如何使用

用户 new HttpCookie(name, value) 或用 HttpCookie.parse(header) 解析，设置 setDomain/setPath/setMaxAge/setSecure/setHttpOnly。

## 三、开源代码实际用法调研

Cookie 解析与生成在 Web 客户端/服务器与爬虫中广泛使用。

## 四、测试用例设计

构造与 getName/getValue/getDomain/getPath/getMaxAge/getSecure/isHttpOnly；parse 单/多头；domainMatches；toString 生成头格式。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（258 条）覆盖：**
- 默认值矩阵：version=0、maxAge=-1（负数=会话 cookie，非过期）、discard=false、secure=false、httpOnly=false、path=""、domain=null、comment/commentURL=null、portlist=null。
- 构造合法性矩阵：tspecials 仅 `,; ` 与空格、控制字符（<0x20 与 0x7F）非法为名；`a[b`/`a{b`/`x$y` 等合法；空名/空值区分。抛 `IllegalArgumentException("Cookie name is reserved" 仅 $Version)`、`IllegalArgumentException("Illegal cookie name")`。
- 版本猜测：parse 中 expires→v0；version=/max-age/set-cookie2 头→v1；被猜测的版本会覆盖显式 version 属性（v1 头+expires 属性 → 实际 version 0）。
- domainMatches 21 组矩阵：点前缀域仅匹配 apex 与**恰好一级**子域（深层子域拒绝）；TLD 形态拒绝；裸单标签拒绝；null 参数 false。
- equals（名不区分、域不区分、路径区分）与 hashCode 精确公式 `lc(name).hashCode()+lc(domain).hashCode()+path.hashCode()`（默认值时 =110）。
- toString 四形态：v0 有/无值、v1 附加 `$Path`/`$Domain`/`$Port`。
- setDomain 强制小写、setPath 大小写保留、clone 深拷贝等。

**易错细节（探针实证）：**
- `hasExpired()` 对负 maxAge 在 JDK-27 返回 false（JDK-8 返回 true）——负数=会话 cookie 语义。
- 过期属性冲突：`max-age` 优先于 `expires`（JDK-8 相反：先出现者胜）。
- 过去时间的 expires → maxAge=0（JDK-8 为负数）。
- 非法 max-age 属性值被**忽略**（version 保持 -1→猜测规则）；JDK-8 直接抛 IAE。
