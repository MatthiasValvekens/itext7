package com.itextpdf.forms.fields;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Category(UnitTest.class)
public class CheckBoxFormFieldBuilderTest extends ExtendedITextTest {
    private static final PdfDocument DUMMY_DOCUMENT = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
    private static final String DUMMY_NAME = "dummy name";
    private static final Rectangle DUMMY_RECTANGLE = new Rectangle(7, 11, 13, 17);

    @Test
    public void constructorTest() {
        CheckBoxFormFieldBuilder builder = new CheckBoxFormFieldBuilder(DUMMY_DOCUMENT, DUMMY_NAME);

        Assert.assertSame(DUMMY_DOCUMENT, builder.getDocument());
        Assert.assertSame(DUMMY_NAME, builder.getFormFieldName());
    }

    @Test
    public void setGetCheckType() {
        CheckBoxFormFieldBuilder builder = new CheckBoxFormFieldBuilder(DUMMY_DOCUMENT, DUMMY_NAME);
        builder.setCheckType(55);

        Assert.assertEquals(55, builder.getCheckType());
    }

    @Test
    public void createCheckBoxWithWidgetTest() {
        PdfButtonFormField checkBoxFormField = new CheckBoxFormFieldBuilder(DUMMY_DOCUMENT, DUMMY_NAME)
                .setWidgetRectangle(DUMMY_RECTANGLE).createCheckBox();

        compareCheckBoxes(checkBoxFormField, true);
    }

    @Test
    public void createCheckBoxWithoutWidgetTest() {
        PdfButtonFormField checkBoxFormField =
                new CheckBoxFormFieldBuilder(DUMMY_DOCUMENT, DUMMY_NAME).createCheckBox();

        compareCheckBoxes(checkBoxFormField, false);
    }

    @Test
    public void createCheckBoxWithConformanceLevelTest() {
        PdfButtonFormField checkBoxFormField = new CheckBoxFormFieldBuilder(DUMMY_DOCUMENT, DUMMY_NAME)
                .setWidgetRectangle(DUMMY_RECTANGLE).setConformanceLevel(PdfAConformanceLevel.PDF_A_1A)
                .createCheckBox();

        compareCheckBoxes(checkBoxFormField, true);
    }

    private static void compareCheckBoxes(PdfButtonFormField checkBoxFormField, boolean widgetExpected) {
        PdfDictionary expectedDictionary = new PdfDictionary();

        List<PdfWidgetAnnotation> widgets = checkBoxFormField.getWidgets();

        if (widgetExpected) {
            Assert.assertEquals(1, widgets.size());

            PdfWidgetAnnotation annotation = widgets.get(0);

            Assert.assertTrue(DUMMY_RECTANGLE.equalsWithEpsilon(annotation.getRectangle().toRectangle()));

            PdfArray kids = new PdfArray();
            kids.add(annotation.getPdfObject());
            putIfAbsent(expectedDictionary, PdfName.Kids, kids);
        } else {
            Assert.assertEquals(0, widgets.size());
        }

        putIfAbsent(expectedDictionary, PdfName.FT, PdfName.Btn);
        putIfAbsent(expectedDictionary, PdfName.T, new PdfString(DUMMY_NAME));
        putIfAbsent(expectedDictionary, PdfName.V, new PdfName(PdfFormField.OFF_STATE_VALUE));

        expectedDictionary.makeIndirect(DUMMY_DOCUMENT);
        checkBoxFormField.makeIndirect(DUMMY_DOCUMENT);
        Assert.assertNull(
                new CompareTool().compareDictionariesStructure(expectedDictionary, checkBoxFormField.getPdfObject()));
    }

    private static void putIfAbsent(PdfDictionary dictionary, PdfName name, PdfObject value) {
        if (!dictionary.containsKey(name)) {
            dictionary.put(name, value);
        }
    }
}