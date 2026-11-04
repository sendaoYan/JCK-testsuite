# java.awt.Font 数据属性测试设计

## 1. API 功能
字体数据模型（headless 安全部分）：name/family/fontName/PSName、style 位掩码与 float 尺寸、
deriveFont 全重载、decode("FAMILY-STYLE-SIZE") 解析、getFont(Map)、equals/hashCode、
属性映射 getAttributes/getAvailableAttributes、canDisplay、序列化、逻辑族常量。

## 2. Java 用户如何使用
`new Font("Dialog", Font.BOLD|Font.ITALIC, 14)`；`f.deriveFont(20f)`/`deriveFont(transform)`；
`Font.decode("Serif-BOLD-18")` 从字符串还原；`deriveFont(map)` 按属性微调（不触碰光栅化像素）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Font.java`；
`test/jdk/java/awt/Font/`（NullFont.java 等）。

## 4. 测试用例设计 + 易错细节
- 名称/样式/尺寸矩阵：getName/getFamily/getFontName/getPSName、getSize vs getSize2D、
  PLAIN/BOLD/ITALIC 精确值 0/1/2、逻辑族常量字符串。
- deriveFont 五重载往返：style/size/attr/transform/attr-map；
  **deriveFont(AffineTransform) 保留字号并存储变换**（getSize2D 不变——易错点），恒等变换 font.getTransform()==identity。
- decode 解析：family-style-size 三段、大小写不敏感、unknown family 回退但保留 style/size、
  decode(null)→Dialog plain 12、decode 结果确定性 equals。
- getFont(null Map) NPE；equals 语义（同 spec 等、size/style 差异不等、null/异类 false）+hashCode 一致。
- getAttributes 含 FAMILY/SIZE；canDisplay('a')/canDisplayUpTo("abc")==-1；
  序列化 round-trip equals 保持。
- 版本差异（javap/实测确认）：JDK-8 有 deprecated getPeer（反射断言存在），JDK-9+ 移除；
  **Font.textRequiresLayout(char[],int,int) 为 JDK-9+ 新增**（JDK-8 断言缺失；JDK-27 侧加行为断言：
  拉丁/空范围 false、阿拉伯文 true）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
