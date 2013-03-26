package be.kuleuven.cs.distrinet.jlo.eclipse;

import be.kuleuven.cs.distrinet.jlo.model.language.JLo;
import be.kuleuven.cs.distrinet.jlo.model.language.JLoLanguageFactory;
import be.kuleuven.cs.distrinet.chameleon.core.language.Language;
import be.kuleuven.cs.distrinet.chameleon.eclipse.connector.EclipseBootstrapper;
import be.kuleuven.cs.distrinet.chameleon.eclipse.connector.EclipseEditorExtension;
import be.kuleuven.cs.distrinet.chameleon.workspace.ProjectException;


public class Bootstrapper extends EclipseBootstrapper {
	
	public final static String PLUGIN_ID="be.chameleon.eclipse.jlo";
	
	public Bootstrapper() {
		super("JLo","0.1",PLUGIN_ID);
	}
	
	public Language createLanguage() throws ProjectException  {
		JLo result = new JLoLanguageFactory().create();
		result.setPlugin(EclipseEditorExtension.class, new JLoEditorExtension(getLanguageName()));
		return result;
	}

}