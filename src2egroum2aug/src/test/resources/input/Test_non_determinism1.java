package input;

class Test_non_determinism1 {

	  TypedScope getGlobalScope() {
	    TypedScope result = this;
	    while (result.getParent() != null) {
	      result = result.getParent();
	    }
	    return result;
	  }
}