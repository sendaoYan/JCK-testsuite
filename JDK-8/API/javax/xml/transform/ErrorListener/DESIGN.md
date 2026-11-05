# javax.xml.transform.ErrorListener — 设计案

## 1. API 功能
转换错误回调接口,三个方法:warning(TransformerException)、error(TransformerException)、fatalError(TransformerException)。由 TransformerFactory/Transformer(setErrorListener)注册。

## 2. Java 用户如何使用
实现接口并 setErrorListener,在回调里记录/吞掉/重抛;不设置时错误走默认行为(致命错误抛出、错误抛出、警告忽略)。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 Bug 系列(如 5073292 等)用自定义 ErrorListener 断言回调到达;com.sun.org.apache.xalan.internal 调用点决定哪些走 warning/error/fatalError。

## 4. 测试用例设计 + 易错细节
黑盒:匿名实现三个回调各自被调用并记录消息。
真实触发闭环:(1) 编译期错误——样式表含未闭合标签 → error()+fatalError() 均收到,消息含 "unclosed";非法 XPath "!!!" 同样进监听器;(2) 运行期——xsl:message terminate="yes" → fatalError 收到含 "xsl:message" 的异常;document() 打不开文件 → warning/error 含 "boom" 或 "nonexistent";无效数字格式 → error() 消息含 "1gamma"。
传播语义:监听器回调里抛 RuntimeException("listener-boom") → 原样穿透到 transform() 调用者。
易错:error() 与 fatalError() 的分工是实现自由度,断言"收到了什么消息"比断言"走哪个回调"稳;terminate 的 fatalError 在 XSLTC 里是先监听器后抛出。
