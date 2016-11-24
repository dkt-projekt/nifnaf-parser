package de.dkt.nifnafparser;

import ixa.kaflib.Entity;
import ixa.kaflib.ExternalRef;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.KAFDocument.Public;
import ixa.kaflib.Span;
import ixa.kaflib.Term;
import ixa.kaflib.Timex3;
import ixa.kaflib.WF;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.exception.BadRequestException;

public class Naf2NifConverter  {


	private Logger logger = Logger.getLogger(Naf2NifConverter.class);
	private KAFDocument nafdoc;

	private Model outputModel;

	public Naf2NifConverter(String nafContent) throws IOException, JDOMException{

		// represent the NAF src doc
		this.nafdoc = KAFDocument.createFromStream(new StringReader(nafContent));
		//represent the NIF target doc
		this.outputModel = NIFWriter.initializeOutputModel();
		NIFWriter.addPrefixToModel(this.outputModel, "time", "http://www.w3.org/2006/time#");
	}


	public Model convert(){	

		String text = extractSrcText();
		String uri = extractDocUri();
		if(uri==null){
			throw new BadRequestException("NAF-Document must specify 'uri' in public tag.");
		}
		
		NIFWriter.addInitialString(this.outputModel, text, uri);
		
		
		extractEntities();
		extractTemporalExpressions();

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
		if(entities != null){
			for(Entity entity : entities){

				String text = entity.getStr(); // anchorOf
				
				// get externalReferences resources corresponding to taClassRef
				StringBuilder stringBuilder = new StringBuilder();
				List<ExternalRef> externalRefs = entity.getExternalRefs();
				for(ExternalRef ref: externalRefs){
					stringBuilder.append(ref.getResource());
				}
				String taClassRef = stringBuilder.toString();
				String nerType = entity.getType(); 
				
				
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
				}
				if(taIdentRef!=null){
					NIFWriter.addAnnotationEntity(this.outputModel, start, end, text, taIdentRef, taClassRef);
				}
				else{
					NIFWriter.addAnnotationEntitiesWithoutURI(this.outputModel, start, end, text, taClassRef);
				}
			}
		}	

	}

	private void extractTemporalExpressions(){
		
		List<Timex3> timeExpressions = this.nafdoc.getTimeExs();
		for(Timex3 timeExpr : timeExpressions){
			//			String anchor = timeExpr.getAnchorTimeId();
			//			Timex3 begin = timeExpr.getBeginPoint();
			//			Timex3 ende = timeExpr.getEndPoint();
			//			String val = timeExpr.getValue();
			Span<WF> span = timeExpr.getSpan();
			StringBuilder builder = new StringBuilder();
			int w=0;

			List<WF> allWFs = span.getTargets();
			while(w < allWFs.size()){
				builder.append(" ");
				String text = allWFs.get(w).getForm();
				builder.append(text);
				w++;
			}
			String temporalExpr = builder.toString().substring(1);
			int offset = allWFs.get(0).getOffset();
			WF lastWord = allWFs.get(allWFs.size()-1);
			int end = lastWord.getOffset() + lastWord.getLength();

			//TODO intervalStart + intervalEnds need to be inserted to NAF!
			String label = "2016-10-24T00:00:00_2016-10-24T00:00:00";
			NIFWriter.addTemporalEntity(this.outputModel, offset, end, temporalExpr, label);
		}

	}


}
