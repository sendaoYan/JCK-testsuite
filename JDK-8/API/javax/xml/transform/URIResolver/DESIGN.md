# javax.xml.transform.URIResolver — 设计案

## 1. API 功能
URI 解析回调接口:唯一方法 `Source resolve(String href, String base)`。由 TransformerFactory/Transformer.setURIResolver 注册,拦截 xsl:include/xsl:import 与 document() 的资源获取。

## 2. Java 用户如何使用
实现 resolve 返回替代 Source(或 null 走默认解析),实现从 classpath/内存/加密源加载样式表模块。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 include 定制用例;XSLTC 在编译期(include/import)与运行期(document())都回调。

## 4. 测试用例设计 + 易错细节
黑盒+真实闭环:(1) xsl:include 采纳——主样式表 include "inc.xsl"(base http://base/main.xsl),resolver 返回内嵌 StreamSource(子模板),回调参数 href/base 精确断言,最终输出 `<inc><got id="3">third</got></inc>` 精确;(2) document() 采纳——样式表 document('doc.xml') 走 resolver 返回内嵌 XML,输出 `<doc>docval</doc>`;(3) 返回 null 回退默认解析——base 指向不存在路径 → TransformerException(包含文件打不开),不触网。
易错:(1) include 在 newTemplates/newTransformer 编译时就回调,不是 transform 时;(2) resolver 为 null 表示恢复默认,不抛异常;(3) 回调里返回的 Source 不必带 systemId,但再次 include 相对路径时以其为 base。
