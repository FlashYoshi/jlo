package be.kuleuven.cs.distrinet.jlo.translate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.cs.distrinet.jlo.model.language.JLo;
import be.kuleuven.cs.distrinet.jnome.core.language.Java;
import be.kuleuven.cs.distrinet.chameleon.core.document.Document;
import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
import be.kuleuven.cs.distrinet.chameleon.exception.ModelException;
import be.kuleuven.cs.distrinet.chameleon.plugin.build.BuildException;
import be.kuleuven.cs.distrinet.chameleon.plugin.build.BuildProgressHelper;
import be.kuleuven.cs.distrinet.chameleon.support.translate.IncrementalTranslator;
import be.kuleuven.cs.distrinet.chameleon.workspace.View;

public class IncrementalJavaTranslator extends IncrementalTranslator<JLo, Java> {

	public IncrementalJavaTranslator(View source, View target) {
		super(source, target);
		_translator = new JavaTranslator();
	}
	
//	public IncrementalJavaTranslator(JLo source) {
//		this(source,createJava(source));
//	}
	
//	private static Java createJava(View source) {
//		Java result = new JavaLanguageFactory().create();
//		Project project = new Project("clone", (RootNamespace) source.namespace().clone(), result, source.project().ro ot());
//		return result;
//	}

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
	public Collection<Document> build(Document source, BuildProgressHelper buildProgressHelper) throws BuildException {
		//initTargetLanguage();
		try {
			List<Document> result = translate(source,implementationCompilationUnit(source));
			Document ifaceCU = (result.size() > 1 ? result.get(1) : null); 
			store(source, ifaceCU,_interfaceMap);
			if(buildProgressHelper != null) {
				buildProgressHelper.addWorked(1);
			}
			return result;
		} catch (LookupException e) {
			throw new BuildException(e);
		} catch (ModelException e) {
			throw new BuildException(e);
		}
	}

	public List<Document> translate(Document source, Document implementationCompilationUnit) throws LookupException, ModelException {
		return _translator.translate(source, implementationCompilationUnit);
	}
  
	private Map<Document,Document> _interfaceMap = new HashMap<Document,Document>();
}