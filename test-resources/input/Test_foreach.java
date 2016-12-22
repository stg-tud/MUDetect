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
	
	void p() {
		for (Map.Entry<Integer, Integer> kv : names.entrySet()) {
    		if (kv.getKey() < 256) {
    			cidbyte2uni[kv.getKey().intValue()] = (char)kv.getValue().intValue();
    		}
    	}
	}

    private void processUni2Byte() throws IOException{
        IntHashtable uni2byte = getUni2Byte();
        int e[] = uni2byte.toOrderedKeys();
        if (e.length == 0)
            return;
        
        cidbyte2uni = new char[256];
        if (toUnicodeCmap == null) {
        	for (int k = 0; k < e.length; ++k) {
        		int n = uni2byte.get(e[k]);
            
        		// this is messy, messy - an encoding can have multiple unicode values mapping to the same cid - we are going to arbitrarily choose the first one
        		// what we really need to do is to parse the encoding, and handle the differences info ourselves.  This is a huge duplication of code of what is already
        		// being done in DocumentFont, so I really hate to go down that path without seriously thinking about a change in the organization of the Font class hierarchy
        		if (n < 256 && cidbyte2uni[n] == 0)
                cidbyte2uni[n] = (char)e[k];
        	}
        }
        else {
        	Map<Integer, Integer> dm = toUnicodeCmap.createDirectMapping();
        	for (Map.Entry<Integer, Integer> kv : dm.entrySet()) {
        		if (kv.getKey() < 256) {
        			kv.getValue();
        		}
        	}
        }
        IntHashtable diffmap = getDiffmap();
        if (diffmap != null) {
            // the difference array overrides the existing encoding
            e = diffmap.toOrderedKeys();
            for (int k = 0; k < e.length; ++k) {
                int n = diffmap.get(e[k]);
                if (n < 256)
                    cidbyte2uni[n] = (char)e[k];
            }
        }
    }
}