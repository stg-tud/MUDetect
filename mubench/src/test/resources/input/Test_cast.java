package input;

import java.util.Map;

class Test_cast {

	private synchronized void setRollbackSegmentInfos(SegmentInfos infos) {
		SegmentInfos rollbackSegmentInfos = (SegmentInfos) infos.clone();
		rollbackSegmentInfos.size();
	}
	
	void m(Map<String, Foo> map, String a) {
		map.put(a, (Foo) map.get(a));
	}
}