/*
 * @test
 * @summary org.w3c.dom.css.CSS2Properties conformance: CSS2 property accessors.
 * @run main CSS2PropertiesTest
 */

public class CSS2PropertiesTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.CSS2Properties");
        check(k.isInterface(), "org.w3c.dom.css.CSS2Properties is an interface");
        check(k.getInterfaces().length == 0, "CSS2Properties declares no superinterface");
        check(k.getMethod("getAzimuth") != null, "getAzimuth()");
        check(k.getMethod("getBackground") != null, "getBackground()");
        check(k.getMethod("getColor") != null, "getColor()");
        check(k.getMethod("getCssFloat") != null, "getCssFloat()");
        check(k.getMethod("getDisplay") != null, "getDisplay()");
        check(k.getMethod("getZIndex") != null, "getZIndex()");
        check(k.getMethod("setZIndex", String.class) != null, "setZIndex(String)");
        check(k.getMethod("setBackground", String.class) != null, "setBackground(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSS2Properties a = new org.w3c.dom.css.CSS2Properties(){
            public java.lang.String getAzimuth() { return "s"; }
            public void setAzimuth(java.lang.String p0) { }
            public java.lang.String getBackground() { return "s"; }
            public void setBackground(java.lang.String p0) { }
            public java.lang.String getBackgroundAttachment() { return "s"; }
            public void setBackgroundAttachment(java.lang.String p0) { }
            public java.lang.String getBackgroundColor() { return "s"; }
            public void setBackgroundColor(java.lang.String p0) { }
            public java.lang.String getBackgroundImage() { return "s"; }
            public void setBackgroundImage(java.lang.String p0) { }
            public java.lang.String getBackgroundPosition() { return "s"; }
            public void setBackgroundPosition(java.lang.String p0) { }
            public java.lang.String getBackgroundRepeat() { return "s"; }
            public void setBackgroundRepeat(java.lang.String p0) { }
            public java.lang.String getBorder() { return "s"; }
            public void setBorder(java.lang.String p0) { }
            public java.lang.String getBorderCollapse() { return "s"; }
            public void setBorderCollapse(java.lang.String p0) { }
            public java.lang.String getBorderColor() { return "s"; }
            public void setBorderColor(java.lang.String p0) { }
            public java.lang.String getBorderSpacing() { return "s"; }
            public void setBorderSpacing(java.lang.String p0) { }
            public java.lang.String getBorderStyle() { return "s"; }
            public void setBorderStyle(java.lang.String p0) { }
            public java.lang.String getBorderTop() { return "s"; }
            public void setBorderTop(java.lang.String p0) { }
            public java.lang.String getBorderRight() { return "s"; }
            public void setBorderRight(java.lang.String p0) { }
            public java.lang.String getBorderBottom() { return "s"; }
            public void setBorderBottom(java.lang.String p0) { }
            public java.lang.String getBorderLeft() { return "s"; }
            public void setBorderLeft(java.lang.String p0) { }
            public java.lang.String getBorderTopColor() { return "s"; }
            public void setBorderTopColor(java.lang.String p0) { }
            public java.lang.String getBorderRightColor() { return "s"; }
            public void setBorderRightColor(java.lang.String p0) { }
            public java.lang.String getBorderBottomColor() { return "s"; }
            public void setBorderBottomColor(java.lang.String p0) { }
            public java.lang.String getBorderLeftColor() { return "s"; }
            public void setBorderLeftColor(java.lang.String p0) { }
            public java.lang.String getBorderTopStyle() { return "s"; }
            public void setBorderTopStyle(java.lang.String p0) { }
            public java.lang.String getBorderRightStyle() { return "s"; }
            public void setBorderRightStyle(java.lang.String p0) { }
            public java.lang.String getBorderBottomStyle() { return "s"; }
            public void setBorderBottomStyle(java.lang.String p0) { }
            public java.lang.String getBorderLeftStyle() { return "s"; }
            public void setBorderLeftStyle(java.lang.String p0) { }
            public java.lang.String getBorderTopWidth() { return "s"; }
            public void setBorderTopWidth(java.lang.String p0) { }
            public java.lang.String getBorderRightWidth() { return "s"; }
            public void setBorderRightWidth(java.lang.String p0) { }
            public java.lang.String getBorderBottomWidth() { return "s"; }
            public void setBorderBottomWidth(java.lang.String p0) { }
            public java.lang.String getBorderLeftWidth() { return "s"; }
            public void setBorderLeftWidth(java.lang.String p0) { }
            public java.lang.String getBorderWidth() { return "s"; }
            public void setBorderWidth(java.lang.String p0) { }
            public java.lang.String getBottom() { return "s"; }
            public void setBottom(java.lang.String p0) { }
            public java.lang.String getCaptionSide() { return "s"; }
            public void setCaptionSide(java.lang.String p0) { }
            public java.lang.String getClear() { return "s"; }
            public void setClear(java.lang.String p0) { }
            public java.lang.String getClip() { return "s"; }
            public void setClip(java.lang.String p0) { }
            public java.lang.String getColor() { return "s"; }
            public void setColor(java.lang.String p0) { }
            public java.lang.String getContent() { return "s"; }
            public void setContent(java.lang.String p0) { }
            public java.lang.String getCounterIncrement() { return "s"; }
            public void setCounterIncrement(java.lang.String p0) { }
            public java.lang.String getCounterReset() { return "s"; }
            public void setCounterReset(java.lang.String p0) { }
            public java.lang.String getCue() { return "s"; }
            public void setCue(java.lang.String p0) { }
            public java.lang.String getCueAfter() { return "s"; }
            public void setCueAfter(java.lang.String p0) { }
            public java.lang.String getCueBefore() { return "s"; }
            public void setCueBefore(java.lang.String p0) { }
            public java.lang.String getCursor() { return "s"; }
            public void setCursor(java.lang.String p0) { }
            public java.lang.String getDirection() { return "s"; }
            public void setDirection(java.lang.String p0) { }
            public java.lang.String getDisplay() { return "s"; }
            public void setDisplay(java.lang.String p0) { }
            public java.lang.String getElevation() { return "s"; }
            public void setElevation(java.lang.String p0) { }
            public java.lang.String getEmptyCells() { return "s"; }
            public void setEmptyCells(java.lang.String p0) { }
            public java.lang.String getCssFloat() { return "s"; }
            public void setCssFloat(java.lang.String p0) { }
            public java.lang.String getFont() { return "s"; }
            public void setFont(java.lang.String p0) { }
            public java.lang.String getFontFamily() { return "s"; }
            public void setFontFamily(java.lang.String p0) { }
            public java.lang.String getFontSize() { return "s"; }
            public void setFontSize(java.lang.String p0) { }
            public java.lang.String getFontSizeAdjust() { return "s"; }
            public void setFontSizeAdjust(java.lang.String p0) { }
            public java.lang.String getFontStretch() { return "s"; }
            public void setFontStretch(java.lang.String p0) { }
            public java.lang.String getFontStyle() { return "s"; }
            public void setFontStyle(java.lang.String p0) { }
            public java.lang.String getFontVariant() { return "s"; }
            public void setFontVariant(java.lang.String p0) { }
            public java.lang.String getFontWeight() { return "s"; }
            public void setFontWeight(java.lang.String p0) { }
            public java.lang.String getHeight() { return "s"; }
            public void setHeight(java.lang.String p0) { }
            public java.lang.String getLeft() { return "s"; }
            public void setLeft(java.lang.String p0) { }
            public java.lang.String getLetterSpacing() { return "s"; }
            public void setLetterSpacing(java.lang.String p0) { }
            public java.lang.String getLineHeight() { return "s"; }
            public void setLineHeight(java.lang.String p0) { }
            public java.lang.String getListStyle() { return "s"; }
            public void setListStyle(java.lang.String p0) { }
            public java.lang.String getListStyleImage() { return "s"; }
            public void setListStyleImage(java.lang.String p0) { }
            public java.lang.String getListStylePosition() { return "s"; }
            public void setListStylePosition(java.lang.String p0) { }
            public java.lang.String getListStyleType() { return "s"; }
            public void setListStyleType(java.lang.String p0) { }
            public java.lang.String getMargin() { return "s"; }
            public void setMargin(java.lang.String p0) { }
            public java.lang.String getMarginTop() { return "s"; }
            public void setMarginTop(java.lang.String p0) { }
            public java.lang.String getMarginRight() { return "s"; }
            public void setMarginRight(java.lang.String p0) { }
            public java.lang.String getMarginBottom() { return "s"; }
            public void setMarginBottom(java.lang.String p0) { }
            public java.lang.String getMarginLeft() { return "s"; }
            public void setMarginLeft(java.lang.String p0) { }
            public java.lang.String getMarkerOffset() { return "s"; }
            public void setMarkerOffset(java.lang.String p0) { }
            public java.lang.String getMarks() { return "s"; }
            public void setMarks(java.lang.String p0) { }
            public java.lang.String getMaxHeight() { return "s"; }
            public void setMaxHeight(java.lang.String p0) { }
            public java.lang.String getMaxWidth() { return "s"; }
            public void setMaxWidth(java.lang.String p0) { }
            public java.lang.String getMinHeight() { return "s"; }
            public void setMinHeight(java.lang.String p0) { }
            public java.lang.String getMinWidth() { return "s"; }
            public void setMinWidth(java.lang.String p0) { }
            public java.lang.String getOrphans() { return "s"; }
            public void setOrphans(java.lang.String p0) { }
            public java.lang.String getOutline() { return "s"; }
            public void setOutline(java.lang.String p0) { }
            public java.lang.String getOutlineColor() { return "s"; }
            public void setOutlineColor(java.lang.String p0) { }
            public java.lang.String getOutlineStyle() { return "s"; }
            public void setOutlineStyle(java.lang.String p0) { }
            public java.lang.String getOutlineWidth() { return "s"; }
            public void setOutlineWidth(java.lang.String p0) { }
            public java.lang.String getOverflow() { return "s"; }
            public void setOverflow(java.lang.String p0) { }
            public java.lang.String getPadding() { return "s"; }
            public void setPadding(java.lang.String p0) { }
            public java.lang.String getPaddingTop() { return "s"; }
            public void setPaddingTop(java.lang.String p0) { }
            public java.lang.String getPaddingRight() { return "s"; }
            public void setPaddingRight(java.lang.String p0) { }
            public java.lang.String getPaddingBottom() { return "s"; }
            public void setPaddingBottom(java.lang.String p0) { }
            public java.lang.String getPaddingLeft() { return "s"; }
            public void setPaddingLeft(java.lang.String p0) { }
            public java.lang.String getPage() { return "s"; }
            public void setPage(java.lang.String p0) { }
            public java.lang.String getPageBreakAfter() { return "s"; }
            public void setPageBreakAfter(java.lang.String p0) { }
            public java.lang.String getPageBreakBefore() { return "s"; }
            public void setPageBreakBefore(java.lang.String p0) { }
            public java.lang.String getPageBreakInside() { return "s"; }
            public void setPageBreakInside(java.lang.String p0) { }
            public java.lang.String getPause() { return "s"; }
            public void setPause(java.lang.String p0) { }
            public java.lang.String getPauseAfter() { return "s"; }
            public void setPauseAfter(java.lang.String p0) { }
            public java.lang.String getPauseBefore() { return "s"; }
            public void setPauseBefore(java.lang.String p0) { }
            public java.lang.String getPitch() { return "s"; }
            public void setPitch(java.lang.String p0) { }
            public java.lang.String getPitchRange() { return "s"; }
            public void setPitchRange(java.lang.String p0) { }
            public java.lang.String getPlayDuring() { return "s"; }
            public void setPlayDuring(java.lang.String p0) { }
            public java.lang.String getPosition() { return "s"; }
            public void setPosition(java.lang.String p0) { }
            public java.lang.String getQuotes() { return "s"; }
            public void setQuotes(java.lang.String p0) { }
            public java.lang.String getRichness() { return "s"; }
            public void setRichness(java.lang.String p0) { }
            public java.lang.String getRight() { return "s"; }
            public void setRight(java.lang.String p0) { }
            public java.lang.String getSize() { return "s"; }
            public void setSize(java.lang.String p0) { }
            public java.lang.String getSpeak() { return "s"; }
            public void setSpeak(java.lang.String p0) { }
            public java.lang.String getSpeakHeader() { return "s"; }
            public void setSpeakHeader(java.lang.String p0) { }
            public java.lang.String getSpeakNumeral() { return "s"; }
            public void setSpeakNumeral(java.lang.String p0) { }
            public java.lang.String getSpeakPunctuation() { return "s"; }
            public void setSpeakPunctuation(java.lang.String p0) { }
            public java.lang.String getSpeechRate() { return "s"; }
            public void setSpeechRate(java.lang.String p0) { }
            public java.lang.String getStress() { return "s"; }
            public void setStress(java.lang.String p0) { }
            public java.lang.String getTableLayout() { return "s"; }
            public void setTableLayout(java.lang.String p0) { }
            public java.lang.String getTextAlign() { return "s"; }
            public void setTextAlign(java.lang.String p0) { }
            public java.lang.String getTextDecoration() { return "s"; }
            public void setTextDecoration(java.lang.String p0) { }
            public java.lang.String getTextIndent() { return "s"; }
            public void setTextIndent(java.lang.String p0) { }
            public java.lang.String getTextShadow() { return "s"; }
            public void setTextShadow(java.lang.String p0) { }
            public java.lang.String getTextTransform() { return "s"; }
            public void setTextTransform(java.lang.String p0) { }
            public java.lang.String getTop() { return "s"; }
            public void setTop(java.lang.String p0) { }
            public java.lang.String getUnicodeBidi() { return "s"; }
            public void setUnicodeBidi(java.lang.String p0) { }
            public java.lang.String getVerticalAlign() { return "s"; }
            public void setVerticalAlign(java.lang.String p0) { }
            public java.lang.String getVisibility() { return "s"; }
            public void setVisibility(java.lang.String p0) { }
            public java.lang.String getVoiceFamily() { return "s"; }
            public void setVoiceFamily(java.lang.String p0) { }
            public java.lang.String getVolume() { return "s"; }
            public void setVolume(java.lang.String p0) { }
            public java.lang.String getWhiteSpace() { return "s"; }
            public void setWhiteSpace(java.lang.String p0) { }
            public java.lang.String getWidows() { return "s"; }
            public void setWidows(java.lang.String p0) { }
            public java.lang.String getWidth() { return "s"; }
            public void setWidth(java.lang.String p0) { }
            public java.lang.String getWordSpacing() { return "s"; }
            public void setWordSpacing(java.lang.String p0) { }
            public java.lang.String getZIndex() { return "s"; }
            public void setZIndex(java.lang.String p0) { }
        };
        check(a != null, "anon impl of CSS2Properties");
        check("s".equals(a.getAzimuth()), "getAzimuth() returns fixed default");
        a.setAzimuth("s");
        check("s".equals(a.getBackground()), "getBackground() returns fixed default");
        a.setBackground("s");
        check("s".equals(a.getBackgroundAttachment()), "getBackgroundAttachment() returns fixed default");
        a.setBackgroundAttachment("s");
        check("s".equals(a.getBackgroundColor()), "getBackgroundColor() returns fixed default");
        a.setBackgroundColor("s");
        check("s".equals(a.getBackgroundImage()), "getBackgroundImage() returns fixed default");
        a.setBackgroundImage("s");
        check("s".equals(a.getBackgroundPosition()), "getBackgroundPosition() returns fixed default");
        a.setBackgroundPosition("s");
        check("s".equals(a.getBackgroundRepeat()), "getBackgroundRepeat() returns fixed default");
        a.setBackgroundRepeat("s");
        check("s".equals(a.getBorder()), "getBorder() returns fixed default");
        a.setBorder("s");
        check("s".equals(a.getBorderCollapse()), "getBorderCollapse() returns fixed default");
        a.setBorderCollapse("s");
        check("s".equals(a.getBorderColor()), "getBorderColor() returns fixed default");
        a.setBorderColor("s");
        check("s".equals(a.getBorderSpacing()), "getBorderSpacing() returns fixed default");
        a.setBorderSpacing("s");
        check("s".equals(a.getBorderStyle()), "getBorderStyle() returns fixed default");
        a.setBorderStyle("s");
        check("s".equals(a.getBorderTop()), "getBorderTop() returns fixed default");
        a.setBorderTop("s");
        check("s".equals(a.getBorderRight()), "getBorderRight() returns fixed default");
        a.setBorderRight("s");
        check("s".equals(a.getBorderBottom()), "getBorderBottom() returns fixed default");
        a.setBorderBottom("s");
        check("s".equals(a.getBorderLeft()), "getBorderLeft() returns fixed default");
        a.setBorderLeft("s");
        check("s".equals(a.getBorderTopColor()), "getBorderTopColor() returns fixed default");
        a.setBorderTopColor("s");
        check("s".equals(a.getBorderRightColor()), "getBorderRightColor() returns fixed default");
        a.setBorderRightColor("s");
        check("s".equals(a.getBorderBottomColor()), "getBorderBottomColor() returns fixed default");
        a.setBorderBottomColor("s");
        check("s".equals(a.getBorderLeftColor()), "getBorderLeftColor() returns fixed default");
        a.setBorderLeftColor("s");
        check("s".equals(a.getBorderTopStyle()), "getBorderTopStyle() returns fixed default");
        a.setBorderTopStyle("s");
        check("s".equals(a.getBorderRightStyle()), "getBorderRightStyle() returns fixed default");
        a.setBorderRightStyle("s");
        check("s".equals(a.getBorderBottomStyle()), "getBorderBottomStyle() returns fixed default");
        a.setBorderBottomStyle("s");
        check("s".equals(a.getBorderLeftStyle()), "getBorderLeftStyle() returns fixed default");
        a.setBorderLeftStyle("s");
        check("s".equals(a.getBorderTopWidth()), "getBorderTopWidth() returns fixed default");
        a.setBorderTopWidth("s");
        check("s".equals(a.getBorderRightWidth()), "getBorderRightWidth() returns fixed default");
        a.setBorderRightWidth("s");
        check("s".equals(a.getBorderBottomWidth()), "getBorderBottomWidth() returns fixed default");
        a.setBorderBottomWidth("s");
        check("s".equals(a.getBorderLeftWidth()), "getBorderLeftWidth() returns fixed default");
        a.setBorderLeftWidth("s");
        check("s".equals(a.getBorderWidth()), "getBorderWidth() returns fixed default");
        a.setBorderWidth("s");
        check("s".equals(a.getBottom()), "getBottom() returns fixed default");
        a.setBottom("s");
        check("s".equals(a.getCaptionSide()), "getCaptionSide() returns fixed default");
        a.setCaptionSide("s");
        check("s".equals(a.getClear()), "getClear() returns fixed default");
        a.setClear("s");
        check("s".equals(a.getClip()), "getClip() returns fixed default");
        a.setClip("s");
        check("s".equals(a.getColor()), "getColor() returns fixed default");
        a.setColor("s");
        check("s".equals(a.getContent()), "getContent() returns fixed default");
        a.setContent("s");
        check("s".equals(a.getCounterIncrement()), "getCounterIncrement() returns fixed default");
        a.setCounterIncrement("s");
        check("s".equals(a.getCounterReset()), "getCounterReset() returns fixed default");
        a.setCounterReset("s");
        check("s".equals(a.getCue()), "getCue() returns fixed default");
        a.setCue("s");
        check("s".equals(a.getCueAfter()), "getCueAfter() returns fixed default");
        a.setCueAfter("s");
        check("s".equals(a.getCueBefore()), "getCueBefore() returns fixed default");
        a.setCueBefore("s");
        check("s".equals(a.getCursor()), "getCursor() returns fixed default");
        a.setCursor("s");
        check("s".equals(a.getDirection()), "getDirection() returns fixed default");
        a.setDirection("s");
        check("s".equals(a.getDisplay()), "getDisplay() returns fixed default");
        a.setDisplay("s");
        check("s".equals(a.getElevation()), "getElevation() returns fixed default");
        a.setElevation("s");
        check("s".equals(a.getEmptyCells()), "getEmptyCells() returns fixed default");
        a.setEmptyCells("s");
        check("s".equals(a.getCssFloat()), "getCssFloat() returns fixed default");
        a.setCssFloat("s");
        check("s".equals(a.getFont()), "getFont() returns fixed default");
        a.setFont("s");
        check("s".equals(a.getFontFamily()), "getFontFamily() returns fixed default");
        a.setFontFamily("s");
        check("s".equals(a.getFontSize()), "getFontSize() returns fixed default");
        a.setFontSize("s");
        check("s".equals(a.getFontSizeAdjust()), "getFontSizeAdjust() returns fixed default");
        a.setFontSizeAdjust("s");
        check("s".equals(a.getFontStretch()), "getFontStretch() returns fixed default");
        a.setFontStretch("s");
        check("s".equals(a.getFontStyle()), "getFontStyle() returns fixed default");
        a.setFontStyle("s");
        check("s".equals(a.getFontVariant()), "getFontVariant() returns fixed default");
        a.setFontVariant("s");
        check("s".equals(a.getFontWeight()), "getFontWeight() returns fixed default");
        a.setFontWeight("s");
        check("s".equals(a.getHeight()), "getHeight() returns fixed default");
        a.setHeight("s");
        check("s".equals(a.getLeft()), "getLeft() returns fixed default");
        a.setLeft("s");
        check("s".equals(a.getLetterSpacing()), "getLetterSpacing() returns fixed default");
        a.setLetterSpacing("s");
        check("s".equals(a.getLineHeight()), "getLineHeight() returns fixed default");
        a.setLineHeight("s");
        check("s".equals(a.getListStyle()), "getListStyle() returns fixed default");
        a.setListStyle("s");
        check("s".equals(a.getListStyleImage()), "getListStyleImage() returns fixed default");
        a.setListStyleImage("s");
        check("s".equals(a.getListStylePosition()), "getListStylePosition() returns fixed default");
        a.setListStylePosition("s");
        check("s".equals(a.getListStyleType()), "getListStyleType() returns fixed default");
        a.setListStyleType("s");
        check("s".equals(a.getMargin()), "getMargin() returns fixed default");
        a.setMargin("s");
        check("s".equals(a.getMarginTop()), "getMarginTop() returns fixed default");
        a.setMarginTop("s");
        check("s".equals(a.getMarginRight()), "getMarginRight() returns fixed default");
        a.setMarginRight("s");
        check("s".equals(a.getMarginBottom()), "getMarginBottom() returns fixed default");
        a.setMarginBottom("s");
        check("s".equals(a.getMarginLeft()), "getMarginLeft() returns fixed default");
        a.setMarginLeft("s");
        check("s".equals(a.getMarkerOffset()), "getMarkerOffset() returns fixed default");
        a.setMarkerOffset("s");
        check("s".equals(a.getMarks()), "getMarks() returns fixed default");
        a.setMarks("s");
        check("s".equals(a.getMaxHeight()), "getMaxHeight() returns fixed default");
        a.setMaxHeight("s");
        check("s".equals(a.getMaxWidth()), "getMaxWidth() returns fixed default");
        a.setMaxWidth("s");
        check("s".equals(a.getMinHeight()), "getMinHeight() returns fixed default");
        a.setMinHeight("s");
        check("s".equals(a.getMinWidth()), "getMinWidth() returns fixed default");
        a.setMinWidth("s");
        check("s".equals(a.getOrphans()), "getOrphans() returns fixed default");
        a.setOrphans("s");
        check("s".equals(a.getOutline()), "getOutline() returns fixed default");
        a.setOutline("s");
        check("s".equals(a.getOutlineColor()), "getOutlineColor() returns fixed default");
        a.setOutlineColor("s");
        check("s".equals(a.getOutlineStyle()), "getOutlineStyle() returns fixed default");
        a.setOutlineStyle("s");
        check("s".equals(a.getOutlineWidth()), "getOutlineWidth() returns fixed default");
        a.setOutlineWidth("s");
        check("s".equals(a.getOverflow()), "getOverflow() returns fixed default");
        a.setOverflow("s");
        check("s".equals(a.getPadding()), "getPadding() returns fixed default");
        a.setPadding("s");
        check("s".equals(a.getPaddingTop()), "getPaddingTop() returns fixed default");
        a.setPaddingTop("s");
        check("s".equals(a.getPaddingRight()), "getPaddingRight() returns fixed default");
        a.setPaddingRight("s");
        check("s".equals(a.getPaddingBottom()), "getPaddingBottom() returns fixed default");
        a.setPaddingBottom("s");
        check("s".equals(a.getPaddingLeft()), "getPaddingLeft() returns fixed default");
        a.setPaddingLeft("s");
        check("s".equals(a.getPage()), "getPage() returns fixed default");
        a.setPage("s");
        check("s".equals(a.getPageBreakAfter()), "getPageBreakAfter() returns fixed default");
        a.setPageBreakAfter("s");
        check("s".equals(a.getPageBreakBefore()), "getPageBreakBefore() returns fixed default");
        a.setPageBreakBefore("s");
        check("s".equals(a.getPageBreakInside()), "getPageBreakInside() returns fixed default");
        a.setPageBreakInside("s");
        check("s".equals(a.getPause()), "getPause() returns fixed default");
        a.setPause("s");
        check("s".equals(a.getPauseAfter()), "getPauseAfter() returns fixed default");
        a.setPauseAfter("s");
        check("s".equals(a.getPauseBefore()), "getPauseBefore() returns fixed default");
        a.setPauseBefore("s");
        check("s".equals(a.getPitch()), "getPitch() returns fixed default");
        a.setPitch("s");
        check("s".equals(a.getPitchRange()), "getPitchRange() returns fixed default");
        a.setPitchRange("s");
        check("s".equals(a.getPlayDuring()), "getPlayDuring() returns fixed default");
        a.setPlayDuring("s");
        check("s".equals(a.getPosition()), "getPosition() returns fixed default");
        a.setPosition("s");
        check("s".equals(a.getQuotes()), "getQuotes() returns fixed default");
        a.setQuotes("s");
        check("s".equals(a.getRichness()), "getRichness() returns fixed default");
        a.setRichness("s");
        check("s".equals(a.getRight()), "getRight() returns fixed default");
        a.setRight("s");
        check("s".equals(a.getSize()), "getSize() returns fixed default");
        a.setSize("s");
        check("s".equals(a.getSpeak()), "getSpeak() returns fixed default");
        a.setSpeak("s");
        check("s".equals(a.getSpeakHeader()), "getSpeakHeader() returns fixed default");
        a.setSpeakHeader("s");
        check("s".equals(a.getSpeakNumeral()), "getSpeakNumeral() returns fixed default");
        a.setSpeakNumeral("s");
        check("s".equals(a.getSpeakPunctuation()), "getSpeakPunctuation() returns fixed default");
        a.setSpeakPunctuation("s");
        check("s".equals(a.getSpeechRate()), "getSpeechRate() returns fixed default");
        a.setSpeechRate("s");
        check("s".equals(a.getStress()), "getStress() returns fixed default");
        a.setStress("s");
        check("s".equals(a.getTableLayout()), "getTableLayout() returns fixed default");
        a.setTableLayout("s");
        check("s".equals(a.getTextAlign()), "getTextAlign() returns fixed default");
        a.setTextAlign("s");
        check("s".equals(a.getTextDecoration()), "getTextDecoration() returns fixed default");
        a.setTextDecoration("s");
        check("s".equals(a.getTextIndent()), "getTextIndent() returns fixed default");
        a.setTextIndent("s");
        check("s".equals(a.getTextShadow()), "getTextShadow() returns fixed default");
        a.setTextShadow("s");
        check("s".equals(a.getTextTransform()), "getTextTransform() returns fixed default");
        a.setTextTransform("s");
        check("s".equals(a.getTop()), "getTop() returns fixed default");
        a.setTop("s");
        check("s".equals(a.getUnicodeBidi()), "getUnicodeBidi() returns fixed default");
        a.setUnicodeBidi("s");
        check("s".equals(a.getVerticalAlign()), "getVerticalAlign() returns fixed default");
        a.setVerticalAlign("s");
        check("s".equals(a.getVisibility()), "getVisibility() returns fixed default");
        a.setVisibility("s");
        check("s".equals(a.getVoiceFamily()), "getVoiceFamily() returns fixed default");
        a.setVoiceFamily("s");
        check("s".equals(a.getVolume()), "getVolume() returns fixed default");
        a.setVolume("s");
        check("s".equals(a.getWhiteSpace()), "getWhiteSpace() returns fixed default");
        a.setWhiteSpace("s");
        check("s".equals(a.getWidows()), "getWidows() returns fixed default");
        a.setWidows("s");
        check("s".equals(a.getWidth()), "getWidth() returns fixed default");
        a.setWidth("s");
        check("s".equals(a.getWordSpacing()), "getWordSpacing() returns fixed default");
        a.setWordSpacing("s");
        check("s".equals(a.getZIndex()), "getZIndex() returns fixed default");
        a.setZIndex("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
