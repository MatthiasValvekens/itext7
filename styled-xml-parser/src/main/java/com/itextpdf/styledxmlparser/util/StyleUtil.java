package com.itextpdf.styledxmlparser.util;

import com.itextpdf.io.util.DecimalFormatUtil;
import com.itextpdf.styledxmlparser.css.CommonCssConstants;
import com.itextpdf.styledxmlparser.css.resolve.CssPropertyMerger;
import com.itextpdf.styledxmlparser.css.resolve.IStyleInheritance;
import com.itextpdf.styledxmlparser.css.util.CssUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StyleUtil {

    private StyleUtil() {

    }

    /**
     * List to store the properties whose value can depend on parent or element font-size
     */
    private static final List<String> fontSizeDependentPercentage = new ArrayList<String>(2);

    static {
        fontSizeDependentPercentage.add(CommonCssConstants.FONT_SIZE);
        fontSizeDependentPercentage.add(CommonCssConstants.LINE_HEIGHT);
    }

    /**
     * Merge parent CSS declarations.
     *
     * @param styles          the styles map
     * @param styleProperty     the CSS property
     * @param parentPropValue the parent properties value
     * @param inheritanceRules set of inheritance rules
     *
     * @return a map of updated styles after merging parent and child style declarations
     */
    public static  Map<String, String> mergeParentStyleDeclaration(Map<String, String> styles, String styleProperty, String parentPropValue, String parentFontSizeString, Set<IStyleInheritance> inheritanceRules) {
        String childPropValue = styles.get(styleProperty);
        if ((childPropValue == null && checkInheritance(styleProperty, inheritanceRules)) || CommonCssConstants.INHERIT.equals(childPropValue)) {
            if (valueIsOfMeasurement(parentPropValue, CommonCssConstants.EM)
                    || valueIsOfMeasurement(parentPropValue, CommonCssConstants.EX)
                    || valueIsOfMeasurement(parentPropValue, CommonCssConstants.PERCENTAGE) && fontSizeDependentPercentage.contains(styleProperty)) {
                float absoluteParentFontSize = CssUtils.parseAbsoluteLength(parentFontSizeString);
                // Format to 4 decimal places to prevent differences between Java and C#
                styles.put(styleProperty, DecimalFormatUtil
                        .formatNumber(CssUtils.parseRelativeValue(parentPropValue, absoluteParentFontSize),
                                "0.####") + CommonCssConstants.PT);
            } else {
                styles.put(styleProperty, parentPropValue);
            }
        } else if (CommonCssConstants.TEXT_DECORATION_LINE.equals(styleProperty)
                && !CommonCssConstants.INLINE_BLOCK.equals(styles.get(CommonCssConstants.DISPLAY))) {
            // Note! This property is formally not inherited, but the browsers behave very similar to inheritance here.
            // Text decorations on inline boxes are drawn across the entire element,
            // going across any descendant elements without paying any attention to their presence.
            // Also, when, for example, parent element has text-decoration:underline, and the child text-decoration:overline,
            // then the text in the child will be both overline and underline. This is why the declarations are merged
            // See TextDecorationTest#textDecoration01Test
            styles.put(styleProperty, CssPropertyMerger.mergeTextDecoration(childPropValue, parentPropValue));
        }

        return styles;
    }

    /**
     * Check all inheritance rule-sets to see if the passed property is inheritable
     *
     * @param styleProperty property identifier to check
     * @param inheritanceRules a set of inheritance rules
     * @return True if the property is inheritable by one of the rule-sets,
     * false if it is not marked as inheritable in all rule-sets
     */
    private static boolean checkInheritance(String styleProperty, Set<IStyleInheritance> inheritanceRules) {
        for (IStyleInheritance inheritanceRule : inheritanceRules) {
            if (inheritanceRule.isInheritable(styleProperty)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the passed value is a measurement of the type based on the passed measurement symbol string
     *
     * @param value       string containing value to check
     * @param measurement measurement symbol (e.g. % for relative, px for pixels)
     * @return True if the value is numerical and ends with the measurement symbol, false otherwise
     */
    private static boolean valueIsOfMeasurement(String value, String measurement) {
        if (value == null) {
            return false;
        }
        return value.endsWith(measurement) && CssUtils
                .isNumericValue(value.substring(0, value.length() - measurement.length()).trim());
    }
}
