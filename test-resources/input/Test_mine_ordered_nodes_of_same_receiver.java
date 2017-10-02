package input;


class Test_mine_ordered_nodes_of_same_receiver {

	protected PdfFormField getField(boolean isRadio) throws IOException, DocumentException {
        PdfFormField field = null;
        if (isRadio)
            field = PdfFormField.createEmpty(writer);
        else
            field = PdfFormField.createCheckBox(writer);
        if (!isRadio) {
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
        }
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
        return field;
    }
	
	public PdfFormField getField() throws IOException, DocumentException {
        PdfFormField field = PdfFormField.createPushButton(writer);
        if (fieldName != null) {
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
        }
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
        return field;
    }
	
	public PdfFormField getTextField() throws IOException, DocumentException {
        PdfFormField field = PdfFormField.createTextField(writer, false, false, maxCharacterLength);
        if (fieldName != null) {
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            if ((options & MULTILINE) != 0)
                field.setFieldFlags(PdfFormField.FF_MULTILINE);
            if ((options & DO_NOT_SCROLL) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSCROLL);
            if ((options & PASSWORD) != 0)
                field.setFieldFlags(PdfFormField.FF_PASSWORD);
            if ((options & FILE_SELECTION) != 0)
                field.setFieldFlags(PdfFormField.FF_FILESELECT);
            if ((options & DO_NOT_SPELL_CHECK) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
            if ((options & COMB) != 0)
                field.setFieldFlags(PdfFormField.FF_COMB);
        }
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
        return field;
    }
	
	protected PdfFormField getChoiceField(boolean isList) throws IOException, DocumentException {
        PdfFormField field = null;
        if (choiceExports == null) {
            if (isList)
                field = PdfFormField.createList(writer, uchoices, topChoice);
            else
                field = PdfFormField.createCombo(writer, (options & EDIT) != 0, uchoices, topChoice);
        }
        if (fieldName != null) {
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            if ((options & DO_NOT_SPELL_CHECK) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
            if ((options & MULTISELECT) != 0) {
            	field.setFieldFlags( PdfFormField.FF_MULTISELECT );
            }
        }
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
        return field;
    }
	
}