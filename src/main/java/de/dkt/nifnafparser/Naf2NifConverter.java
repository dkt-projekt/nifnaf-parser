package de.dkt.nifnafparser;

import ixa.kaflib.Entity;
import ixa.kaflib.ExternalRef;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.KAFDocument.Public;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFWriter;

public class Naf2NifConverter  {

	
	private Logger logger = Logger.getLogger(Naf2NifConverter.class);
	private KAFDocument nafdoc;
//	private NIFWriter nifwriter;
	
	private Model outputModel;
	
	public Naf2NifConverter(String nafContent) throws IOException, JDOMException{
		
		// represent the NAF src doc
		this.nafdoc = KAFDocument.createFromStream(new StringReader(nafContent));
		//represent the NIF target doc
		this.outputModel = NIFWriter.initializeOutputModel();
	}
	
	
	public Model convert(){	
		
		//TODO: what if NAF-Document doesn't have a document uri ? 
		String text = extractSrcText();
		String uri = extractDocUri();
		NIFWriter.addInitialString(this.outputModel, text, uri);
		
		extractEntities();

		return this.outputModel;
	}
	
	
	private String extractSrcText(){
		return this.nafdoc.getRawText();
	}
	
	private String extractDocUri(){
		Public p = this.nafdoc.getPublic();
		String uri = null;
		if (p!=null){
			uri = p.uri;
		}
		return uri;
	}
	
	
	private void extractEntities(){
		List<Entity> entities = this.nafdoc.getEntities();
		for(Entity entity : entities){
			
			String text = entity.getStr(); // anchorOf
			String nerType = entity.getType(); //taClassRef
			
			//beginIdx and endIdx
			List<Term> terms = entity.getTerms(); 
			List<WF> firstTermWords = terms.get(0).getWFs();
			int start = firstTermWords.get(0).getOffset();
			
			List<WF> lastTermWords = terms.get(terms.size()-1).getWFs();
			int end = lastTermWords.get(lastTermWords.size()-1).getOffset() + lastTermWords.get(lastTermWords.size()-1).getLength();

			
			List<ExternalRef> extRefs = entity.getExternalRefs();
			String taIdentRef = "";
			if(extRefs.size()>0){
				taIdentRef = extRefs.get(0).getReference();
				NIFWriter.addAnnotationEntity(this.outputModel, start, end, text, taIdentRef, nerType);
			}
			else{
				NIFWriter.addAnnotationEntitiesWithoutURI(this.outputModel, start, end, text, nerType);
			}
		}
			
		
	}
}
