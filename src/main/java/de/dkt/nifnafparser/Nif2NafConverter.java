package de.dkt.nifnafparser;

import ixa.kaflib.Entity;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.KAFDocument.Public;
import ixa.kaflib.Span;
import ixa.kaflib.Term;
import ixa.kaflib.Timex3;
import ixa.kaflib.WF;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dkt.common.niftools.NIFReader;

public class Nif2NafConverter {

	private Logger logger = Logger.getLogger(Nif2NafConverter.class);

	private KAFDocument targetNafDoc;
	private Model srcNifDoc;
	private String textContent;
	String lang;
	private Locale locale;
	//private Map<String,String> wfidToWordMap;
	private Map<String,String> wordToIdMap;

	public Nif2NafConverter(Model inModel, String lang, String nafVersion) throws Exception {
		// represent src nif doc ; 
		this.srcNifDoc = inModel;
		// represent target naf doc
		this.targetNafDoc = new KAFDocument(lang,nafVersion);
		
		this.wordToIdMap = new HashMap<String,String>();
		this.lang = lang;
		this.locale = Locale.ENGLISH;

		this.textContent = NIFReader.extractIsString(this.srcNifDoc);
		Resource publicUri = NIFReader.extractDocumentResourceURI(this.srcNifDoc);
		String uri = publicUri.getURI();
		
		Public publicElem = this.targetNafDoc.createPublic();
		publicElem.uri = uri;
		buildTextTag();
		buildTermTag();
		
		
	}

	
	
	public String convert() {


		this.targetNafDoc.setRawText(this.textContent);

		extractEntities();
		extractTemporalExpressions();

		String naf = this.targetNafDoc.toString();
		return naf;
	}


	private void extractTemporalExpressions(){
		
		String NIF = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
		String TIME = "http://www.w3.org/2006/time#";

		Map<String, Map<String,String>> tempEntities = NIFReader.extractTemporalEntitiesExtended(this.srcNifDoc);

		if(tempEntities!=null){
			Set<String> keys = tempEntities.keySet();
			for(String key:keys){
				// map from predicate to object
				Map<String, String> entityPredObjMap = tempEntities.get(key);

				String offset = entityPredObjMap.get(NIF+"beginIndex");
				String temporalEntity = entityPredObjMap.get(NIF+"anchorOf");
				String begin = entityPredObjMap.get(TIME + "intervalStarts");
				String end = entityPredObjMap.get(TIME + "intervalFinishes");

				List<String> tokenizedWords = new ArrayList<String>();
				List<Integer> beginIndices = this.getTokenizedString(temporalEntity, tokenizedWords, this.locale);

				List<String> wordIds = new ArrayList<String>();
				int w = 0;
				for(String word : tokenizedWords){

					int offsetInt = Integer.parseInt(offset);
					int nb = offsetInt + beginIndices.get(w);
					wordIds.add(this.wordToIdMap.get(word+String.valueOf(nb)));
					w++;
				}
				//add temporal Expression
				List<WF> allWFs = this.targetNafDoc.getWFs();
				List<WF> targetWFs = new ArrayList<WF>();
				for(WF wf:allWFs){
					String currentId = wf.getId();
					if(wordIds.contains(currentId)){
						targetWFs.add(wf);
					}
				}
				Span<WF> wfSpan = KAFDocument.newWFSpan(targetWFs);
				// TODO for now always duration 
				Timex3 newTimeExpr = this.targetNafDoc.newTimex3("DURATION");
				newTimeExpr.setSpan(wfSpan);
				
				Timex3 beginTimeExpr = this.targetNafDoc.newTimex3("TIME");
				beginTimeExpr.setValue(begin);
				Timex3 endTimeExpr = this.targetNafDoc.newTimex3("TIME");
				endTimeExpr.setValue(end);

				newTimeExpr.setBeginPoint(beginTimeExpr);
				newTimeExpr.setEndPoint(endTimeExpr);
				
				
			}
		}
	}

	private void extractEntities(){

		List<String[]> entities = NIFReader.extractEntityIndices(this.srcNifDoc);
		if(entities != null){
			//returned String Arrays contain: taIdentRef, anchorOf, taClassRef, begin, End
			for(String[] entity : entities){
				//anchorOf
				String namedEntity = entity[1]; 
				String beginIdx = entity[3];
				String taClassRef = entity[2];
				String taIdentRef = entity[0];

				List<String> wfids = new ArrayList<String>();
				List<String> tokenizedWords = new ArrayList<String>();

				this.getTokenizedString(namedEntity, tokenizedWords, this.locale);

				String firstPartId = this.wordToIdMap.get(tokenizedWords.get(0)+beginIdx);
				int startId = Integer.parseInt(firstPartId.substring(1));

				for(String word:tokenizedWords){
					wfids.add("w" + String.valueOf(startId));
					startId++;
				}

				List<Term> terms = this.targetNafDoc.getTermsFromWFs(wfids);
				Span<Term> termSpan = KAFDocument.newTermSpan(terms);

				List<Span<Term>> references = new ArrayList<Span<Term>>();
				references.add(termSpan);


				String type;
				// It only extracts the first taClassRef, but supposed to be just 1  
				if(taClassRef.contains("TemporalEntity")){
					type = "Time";
				}
				else if(taClassRef.contains("Person")){
					type="Person";
				} else if(taClassRef.contains("Organisation")){
					type = "Organization";
				} else{ 
					type = "MISC";
				}

				// add attributes 
				Entity newEntity = this.targetNafDoc.newEntity(references);
				newEntity.setType(type);
				if(!Strings.isNullOrEmpty(taIdentRef)){
					//newExternalRef: resource, reference // taClassRef exists for all extracted entities
					newEntity.addExternalRef(this.targetNafDoc.newExternalRef(taClassRef, taIdentRef));
				} else{
					//add resource
					newEntity.addExternalRef(this.targetNafDoc.newExternalRef(taClassRef));
				}
			}
		}
	}
	
	
	/**
	 * build one term for each wordform
	 */
	private void buildTermTag(){
		List<WF> targets = targetNafDoc.getWFs();
		for(WF word: targets){
			List<WF> list = new ArrayList<WF>();
			list.add(word);
			Span<WF> singleWFspan = KAFDocument.newSpan(list);
			this.targetNafDoc.newTerm(singleWFspan);
		}
	}

	private void buildTextTag(){

		BreakIterator sentTokenizer = BreakIterator.getSentenceInstance(this.locale);
		sentTokenizer.setText(this.textContent);

		//sentence-level
		int start = sentTokenizer.first();
		int sent = 0;
		for (int end = sentTokenizer.next(); end != BreakIterator.DONE; start = end, end = sentTokenizer.next()) {
			sent++;
			// word-level
			String currentSent = this.textContent.substring(start,end);
			List<String> tokenizedWords = new ArrayList<String>();
			List<Integer> startIndices = this.getTokenizedString(currentSent, tokenizedWords, locale);
			int w=0;
			for(String word:tokenizedWords){
				this.targetNafDoc.newWF(start + startIndices.get(w),word,sent);
				w++;
			}
		}
		// save all words with their id in map
		buildWFIdWordMap();
	}

	private void buildWFIdWordMap(){

		List<WF> allWFs = this.targetNafDoc.getWFs();
		for(WF wf : allWFs){
			String id = wf.getId();
			String word = wf.getForm();
			int offset = wf.getOffset();
			this.wordToIdMap.put(word+offset, id);
		}
	}



	private List<Integer> getTokenizedString(String stringOfWords, List<String> tokenizedWords, Locale locale){
		BreakIterator wordTokenizer = BreakIterator.getWordInstance(locale);
		wordTokenizer.setText(stringOfWords);
		int start = wordTokenizer.first();
		List<Integer> startIndices = new ArrayList<Integer>();

		for (int end = wordTokenizer.next(); end != BreakIterator.DONE; start = end, end = wordTokenizer.next()) {
			String partOfNamedEntity = stringOfWords.substring(start,end);
			if(partOfNamedEntity.matches("\\s+")){
				continue;
			}
			tokenizedWords.add(partOfNamedEntity);
			startIndices.add(start);
		}
		return startIndices;
	}
}
