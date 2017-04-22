package input;

class Test_non_determinism2 {

	  private String resolveJsModuleNodeFileOrDirectory(String scriptAddress, String moduleAddress) {
	    String loadAddress;
	    loadAddress = resolveJsModuleFile(scriptAddress, moduleAddress);
	    if (loadAddress == null) {
	      loadAddress = resolveJsModuleNodeDirectory(scriptAddress, moduleAddress);
	    }
	    return loadAddress;
	  }
}