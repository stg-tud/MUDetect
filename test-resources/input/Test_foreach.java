package input;

import java.util.HashMap;
import java.util.Iterator;

class Test_foreach {
    HashMap<String, Object> names = getAttributes();
	
//	void m() {
//        for (Map.Entry<String, Object> entry : names.entrySet()) {
//            String name = entry.getKey();
//            System.out.println(name);
//        }
//	}
	
//	void n() {
//		for (Iterator<Map.Entry<String, PdfObject>> it = names.entrySet().iterator(); it.hasNext();) {
//            Map.Entry<String, PdfObject> entry = it.next();
//            entry.getValue();
//        }
//	}
    
    void m(java.lang.Iterable it) {
    	java.util.Iterator itr = it.iterator();
    	while (itr.hasNext()) {
    		Object o = itr.next();
    		o.hashCode();
    	}
    }
    
    void n(java.lang.Iterable it) {
    	for (Object o : it) {
    		o.hashCode();
    	}
    }
	
	void p() {
		for (Map.Entry<Integer, Integer> kv : names.entrySet()) {
    		if (kv.getKey() < 256) {
    			cidbyte2uni[kv.getKey().intValue()] = (char)kv.getValue().intValue();
    		}
    	}
	}
	
    private IntHashtable readWidths(PdfArray ws) {
        IntHashtable hh = new IntHashtable();
        if (ws == null)
            return hh;
        for (int k = 0; k < ws.size(); ++k) {
            int c1 = ((PdfNumber)PdfReader.getPdfObjectRelease(ws.getPdfObject(k))).intValue();
            PdfObject obj = PdfReader.getPdfObjectRelease(ws.getPdfObject(++k));
            if (obj.isArray()) {
                PdfArray a2 = (PdfArray)obj;
                for (int j = 0; j < a2.size(); ++j) {
                    int c2 = ((PdfNumber)PdfReader.getPdfObjectRelease(a2.getPdfObject(j))).intValue();
                    hh.put(c1++, c2);
                }
            }
            else {
                int c2 = ((PdfNumber)obj).intValue();
                int w = ((PdfNumber)PdfReader.getPdfObjectRelease(ws.getPdfObject(++k))).intValue();
                for (; c1 <= c2; ++c1)
                    hh.put(c1, w);
            }
        }
        return hh;
    }
}