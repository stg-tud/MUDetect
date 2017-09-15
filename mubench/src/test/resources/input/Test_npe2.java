package input;

import com.google.javascript.jscomp.newtypes.JSType;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.UnionTypeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class Test_npe2 {

	public Rectangle placeBarcode(PdfContentByte cb, BaseColor barColor, BaseColor textColor) {
		String fullCode = code;
		if (generateChecksum && checksumText)
			fullCode = calculateChecksum(code);
		if (!startStopText)
			fullCode = fullCode.substring(1, fullCode.length() - 1);
		float fontX = 0;
		if (font != null) {
			fontX = font.getWidthPoint(fullCode = altText != null ? altText : fullCode, size);
		}
		byte bars[] = getBarsCodabar(generateChecksum ? calculateChecksum(code) : code);
		int wide = 0;
		for (int k = 0; k < bars.length; ++k) {
			wide += bars[k];
		}
		int narrow = bars.length - wide;
		float fullWidth = x * (narrow + wide * n);
		float barStartX = 0;
		float textStartX = 0;
		switch (textAlignment) {
		case Element.ALIGN_LEFT:
			break;
		case Element.ALIGN_RIGHT:
			if (fontX > fullWidth)
				barStartX = fontX - fullWidth;
			else
				textStartX = fullWidth - fontX;
			break;
		default:
			if (fontX > fullWidth)
				barStartX = (fontX - fullWidth) / 2;
			else
				textStartX = (fullWidth - fontX) / 2;
			break;
		}
		float barStartY = 0;
		float textStartY = 0;
		if (font != null) {
			if (baseline <= 0)
				textStartY = barHeight - baseline;
			else {
				textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size);
				barStartY = textStartY + baseline;
			}
		}
		boolean print = true;
		if (barColor != null)
			cb.setColorFill(barColor);
		for (int k = 0; k < bars.length; ++k) {
			float w = (bars[k] == 0 ? x : x * n);
			if (print)
				cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight);
			print = !print;
			barStartX += w;
		}
		cb.fill();
		if (font != null) {
			if (textColor != null)
				cb.setColorFill(textColor);
			cb.beginText();
			cb.setFontAndSize(font, size);
			cb.setTextMatrix(textStartX, textStartY);
			cb.showText(fullCode);
			cb.endText();
		}
		return getBarcodeSize();
	}
}