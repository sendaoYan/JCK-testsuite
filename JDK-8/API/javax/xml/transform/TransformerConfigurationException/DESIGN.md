# javax.xml.transform.TransformerConfigurationException — 设计案

## 1. API 功能
工厂配置/编译失败异常,TransformerException 子类。6 个构造器:()、(String)、(Throwable)、(String,Throwable)、(String,SourceLocator)、(String,SourceLocator,Throwable)。

## 2. Java 用户如何使用
newTransformer()/newTemplates()/setFeature() 的失败信号;诊断系统属性指向不存在工厂类等场景。

## 3. 开源代码实际用法调研
JDK test/jdk/jaxp 的工厂发现失败用例;XSLTC 编译错误统一包装为该类型。

## 4. 测试用例设计 + 易错细节
黑盒:全 6 构造器 round-trip;无参消息锁定 "Configuration Error";(Exception e) 构造 getMessage()==e.toString()(与 (String,Throwable) 的裸消息不同);locator 变体 getLocationAsString() 拼接同父类规则。
真实触发:(1) 样式表未闭合标签 → newTemplates/newTransformer 抛 TCE 含 "unclosed";(2) 系统属性 javax.xml.transform.TransformerFactory 指向 no.such.Clazz → TCE 消息含 "no.such"(finally 清属性);(3) setFeature(任意常量,false) 在 XSLTC 也抛 TCE(实现不支持关闭)。
易错:(1) newTransformer(null)/newTemplates(null) 是 NPE 不是 TCE(探针锁定);(2) JDK-8 与 JDK-27 对 FEATURE_SECURE_PROCESSING setFeature(true) 行为不同(8 支持、27 亦支持但报错形态不同),负例断言用消息关键词不写死整句。
