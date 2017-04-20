package input;

class Test_cast {

	private synchronized void setRollbackSegmentInfos(SegmentInfos infos) {
		SegmentInfos rollbackSegmentInfos = (SegmentInfos) infos.clone();
		rollbackSegmentInfos.size();
	}
}