package jlo.translate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jlo.model.language.JLo;
import jnome.core.language.Java;
import jnome.input.JavaFactory;
import jnome.output.JavaCodeWriter;
import chameleon.core.document.Document;
import chameleon.core.lookup.LookupException;
import chameleon.exception.ModelException;
import chameleon.oo.plugin.ObjectOrientedFactory;
import chameleon.plugin.build.BuildProgressHelper;
import chameleon.plugin.output.Syntax;
import chameleon.support.translate.IncrementalTranslator;

public class IncrementalJavaTranslator extends IncrementalTranslator<JLo, Java> {

	public IncrementalJavaTranslator(JLo source, Java target) {
		super(source, target);
		_translator = new JavaTranslator();
	}
	
	public IncrementalJavaTranslator(JLo source) {
		this(source,createJava());
	}
	
	private static Java createJava() {
		Java java = new Java();
		java.setPlugin(Syntax.class, new JavaCodeWriter());
		java.setPlugin(ObjectOrientedFactory.class, new JavaFactory());
		return java;
	}

	private JavaTranslator _translator;
	
	public AbstractTranslator basicTranslator() {
		return _translator;
	}
	
 /*@
   @ public behavior
   @
   @ pre compilationUnit != null;
   @
   @ post \result != null;
   @ post \fresh(\result);
   @*/
	public Collection<Document> build(Document source, List<Document> allProjectCompilationUnits,	BuildProgressHelper buildProgressHelper) throws ModelException {
		//initTargetLanguage();
		List<Document> result = translate(source,implementationCompilationUnit(source));
		Document ifaceCU = (result.size() > 1 ? result.get(1) : null); 
		store(source, ifaceCU,_interfaceMap);
		if(buildProgressHelper != null) {
			buildProgressHelper.addWorked(1);
		}
		return result;
	}

	public List<Document> translate(Document source, Document implementationCompilationUnit) throws LookupException, ModelException {
		return _translator.translate(source, implementationCompilationUnit);
	}
  
	private Map<Document,Document> _interfaceMap = new HashMap<Document,Document>();
}