# SAXParseException — 设计案

## 1. API 功能
SAX 解析位置异常，SAXException 子类。(String,Locator)/(String,Locator,Exception)/(String,String,String,int,int)/(…,Exception) 构造；getPublicId/getSystemId/getLineNumber/getColumnNumber/toString。

## 2. Java 用户如何使用
解析失败时捕获 SAXParseException 读行号列号定位。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax 校验 Locator/字符串两套构造与位置访问器。

## 4. 测试用例设计 + 自动化实现
黑盒：继承链；四种构造；Locator 与字符串参数两套 publicId/systemId/line/column 访问器；嵌套异常透传；toString 含 message 与 systemId。

### 深化轮次2（行为契约）
深化（追加）：真解析失败（未闭合）line=1、列=12；良性 fatalError 后 parse 抛的是新实例（行号一致非同对象）；toString 含 'lineNumber: 1; columnNumber: 12'（冒号格式）；(String,pid,sid,line,col) 手工构造四元组往返；toString 拼接 systemId: 公共/系统 id。易错：toString 是冒号空格分隔、类名与 publicId 之间无分隔（JDK 实现 quirk，两侧一致）。
