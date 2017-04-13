package input;

import java.util.ArrayList;
import java.util.Iterator;

public static class Tes_deleteUnusedDataNodes {
    Collection pendingMerges;

    void m() {
    	 Iterator it = pendingMerges.iterator();
         while(it.hasNext()) {
           final MergePolicy.OneMerge merge = (MergePolicy.OneMerge) it.next();
           merge.optimize = true;
           merge.maxNumSegmentsOptimize = maxNumSegments;
         }
    }
}