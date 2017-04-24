package input;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class Test_dependent_control {
	ArrayList arrayList;
	Oject object;
	
	void m2(java.util.Iterable it) {
		for (Object o : it) {
			o.hashCode();
		}
	}

	void m1(java.util.List l) {
		if (l.size() > 42)
			l.get(41);
	}
	
//    public static List<Integer> expand(String ranges, int maxNumber) {
//        SequenceList parse = new SequenceList(ranges);
//        LinkedList<Integer> list = new LinkedList<Integer>();
//        boolean sair = false;
//        while (!sair) {
//            sair = parse.getAttributes();
//            if (parse.low == -1 && parse.high == -1 && !parse.even && !parse.odd)
//                continue;
//            if (parse.low < 1)
//                parse.low = 1;
//            if (parse.high < 1 || parse.high > maxNumber)
//                parse.high = maxNumber;
//            if (parse.low > maxNumber)
//                parse.low = maxNumber;
//
//            //System.out.println("low="+parse.low+",high="+parse.high+",odd="+parse.odd+",even="+parse.even+",inverse="+parse.inverse);
//            int inc = 1;
//            if (parse.inverse) {
//                if (parse.low > parse.high) {
//                    int t = parse.low;
//                    parse.low = parse.high;
//                    parse.high = t;
//                }
//                for (ListIterator<Integer> it = list.listIterator(); it.hasNext();) {
//                    int n = it.next().intValue();
//                    if (parse.even && (n & 1) == 1)
//                        continue;
//                    if (parse.odd && (n & 1) == 0)
//                        continue;
//                    if (n >= parse.low && n <= parse.high)
//                        it.remove();
//                }
//            }
//            else {
//                if (parse.low > parse.high) {
//                    inc = -1;
//                    if (parse.odd || parse.even) {
//                        --inc;
//                        if (parse.even)
//                            parse.low &= ~1;
//                        else
//                            parse.low -= (parse.low & 1) == 1 ? 0 : 1;
//                    }
//                    for (int k = parse.low; k >= parse.high; k += inc)
//                        list.add(Integer.valueOf(k));
//                }
//                else {
//                    if (parse.odd || parse.even) {
//                        ++inc;
//                        if (parse.odd)
//                            parse.low |= 1;
//                        else
//                            parse.low += (parse.low & 1) == 1 ? 1 : 0;
//                    }
//                    for (int k = parse.low; k <= parse.high; k += inc) {
//                        list.add(Integer.valueOf(k));
//                    }
//                }
//            }
////            for (int k = 0; k < list.size(); ++k)
////                System.out.print(((Integer)list.get(k)).intValue() + ",");
////            System.out.println();
//        }
//        return list;
//    }
	
    public void toPdf(final PdfWriter writer, final OutputStream os) throws IOException {
        Iterator<PdfObject> i = arrayList.iterator();
        if (i.hasNext()) {
            object = i.next();
        }
        while (i.hasNext()) {
            object = i.next();
        }
    }
    
	void n() {
		Iterator<String> i = arrayList.iterator();
		if (i.hasNext()) {
			String object = i.next();
			if (object == null)
				object = "";
			object.toString();
		}
	}

	void m(List l, int i) {
		int s = l.size();
		if (l != null ) {
			if (i > 0) {
				l.set(i, null);
				l.add(null);
				System.out.println(i);
			}
		}
	}
}