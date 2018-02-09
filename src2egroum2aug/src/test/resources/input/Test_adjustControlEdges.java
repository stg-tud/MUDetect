package input;

public class Test_adjustControlEdges {
	
	public QualifiedName getFullyQualifiedName(final EObject e, final NamingStrategy namingStrategy) {
	    if (shouldIgnore(e)) {
	      return null;
	    }
	    Pair<EObject, String> key = pair(e, "fqn");
	    Pair<NameType, QualifiedName> cached = cache.get(key, e.eResource(), new Provider<Pair<NameType, QualifiedName>>() {
	      @Override public Pair<NameType, QualifiedName> get() {
	        EObject current = e;
	        Pair<NameType, String> name = namingStrategy.nameOf(e);
	        if (name == null) {
	          return EMPTY_NAME;
	        }
	        QualifiedName qualifiedName = converter.toQualifiedName(name.getSecond());
	        while (current.eContainer() != null) {
	          current = current.eContainer();
	          QualifiedName parentsQualifiedName = getFullyQualifiedName(current, namingStrategy);
	          if (parentsQualifiedName != null) {
	            return pair(name.getFirst(), parentsQualifiedName.append(qualifiedName));
	          }
	        }
	        return pair(name.getFirst(), addPackage(e, qualifiedName));
	      }
	    });
	    return cached.getSecond();
	  }
}