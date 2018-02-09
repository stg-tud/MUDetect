package input;

import java.util.LinkedHashMap;

class Test_non_determinism3 {

	Scope getGlobalScope() {
		Scope result = m(this);
		while (result.getParent() != null) {
			result = result.getParent();
		}
		return result;
	}

	private Symbol declareSymbol(
			String name, JSType type, boolean inferred,
			SymbolScope scope, Node declNode, JSDocInfo info) {
		Symbol symbol = addSymbol(name, type, inferred, scope, declNode);
		symbol.setJSDocInfo(info);
		symbol.setDeclaration(symbol.defineReferenceAt(declNode));
		return symbol;
	}

	private Compiler createCompiler(CompilerOptions options) {
		Compiler compiler = new Compiler();
		MessageFormatter formatter = options.getErrorFormat().toFormatter(compiler, false);
		AntErrorManager errorManager = new AntErrorManager(formatter, this);
		compiler.setErrorManager(errorManager);
		return compiler;
	}

	class Doc {
		LinkedHashMap<String, Integer> map;
	}

	Doc doc;

	boolean documentParam(String parameter, Integer description) {
		if (!m())
			return true;

		if (doc.map == null) {
			doc = new Doc();
			doc.map = new LinkedHashMap<>();
		}

		if (!doc.map.containsKey(parameter)) {
			doc.map.put(parameter, description + Integer.MIN_VALUE);
			return true;
		} else
			return false;
	}
}