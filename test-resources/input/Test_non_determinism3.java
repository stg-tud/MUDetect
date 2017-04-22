package input;

import java.util.Iterator;
import java.util.LinkedHashMap;

class Test_non_determinism3 {

    public void toPdf(final PdfWriter writer, final OutputStream os) throws IOException {
        os.write('[');

        Iterator<PdfObject> i = arrayList.iterator();
        PdfObject object;
        int type = 0;
        if (i.hasNext()) {
            object = i.next();
            if (object == null)
                object = PdfNull.PDFNULL;
            object.toPdf(writer, os);
        }
        while (i.hasNext()) {
            object = i.next();
            if (object == null)
                object = PdfNull.PDFNULL;
            type = object.type();
            if (type != PdfObject.ARRAY && type != PdfObject.DICTIONARY && type != PdfObject.NAME && type != PdfObject.STRING)
                os.write(' ');
            object.toPdf(writer, os);
        }
        os.write(']');
    }
	
	class Doc {
		LinkedHashMap<String, String> parameters;
	}
	
	Doc documentation;

	boolean documentParam(String parameter, String description) {
		if (!m()) {
			return true;
		}

		if (documentation.parameters == null) {
			documentation.parameters = new LinkedHashMap<>();
		}

		if (!documentation.parameters.containsKey(parameter)) {
			documentation.parameters.put(parameter, description);
			return true;
		} else {
			return false;
		}
	}
}