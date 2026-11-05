# SAXException — 设计案

## 1. API 功能
SAX 异常，Exception 子类。()/(String)/(Exception)/(String,Exception) 构造；getMessage/getException/getCause/toString。

## 2. Java 用户如何使用
包装底层异常并读 getException/getCause。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax 校验嵌套异常透传与消息。

## 4. 测试用例设计 + 自动化实现
黑盒：继承链（checked）；四构造；getMessage/getException/getCause/toString；嵌套异常对象透传 getException==cause 且 getCause==cause；null message。

### 深化轮次2（行为契约）
深化（追加）：四构造矩阵补 null 分支；getCause==getException 同源；包装异常消息含内层消息；handler 抛出的同实例 SAXException 从 parse 原样穿透（sentinel 同一性）。易错：no-arg 的 getMessage 为 null、toString 不含位置。
