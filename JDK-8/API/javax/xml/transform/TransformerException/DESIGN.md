# javax.xml.transform.TransformerException — 设计案

## 1. API 功能
转换过程异常(checked)。构造器:()、(String)、(String,SourceLocator)、(Throwable)、(String,Throwable)、(String,SourceLocator,Throwable);方法 getLocator/setLocator、getMessageAndLocation()、getLocationAsString()、initCause(可重复至设置一次)、getCause、getException(=旧版 cause 别名)。

## 2. Java 用户如何使用
在 transform()/newTransformer() 调用点捕获,读 getLocationAsString() 定位;自定义 ErrorListener 里包装再抛。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 断言异常消息与行号;XSLTC 错误路径以 SourceLocatorImpl 携带行列。

## 4. 测试用例设计 + 易错细节
黑盒:全部 6 构造器 round-trip;消息、cause、locator 各自独立;无参构造消息 null;initCause 二次抛 ISE;getException()==getCause()。
位置串精确锁定(带 locator):`"; SystemID: http://base/x.xml; Line#: 3; Column#: 5"`(前导 "; ");getMessageAndLocation()=="m1" + 同串;无 locator 时 getLocationAsString()==null、getMessageAndLocation()=="bare message"(裸消息)。
真实触发:样式表未闭合标签 → newTransformer 抛出、getMessage() 含 "unclosed"(XSLTC 编译诊断)。
易错:(1) 位置串是分号+空格开头,直接 equals 全串才稳;(2) (Throwable) 构造消息取 cause.toString();(3) JDK-8 编译诊断文本与 JDK-27 一致("unclosed" 关键词两版通用)。
