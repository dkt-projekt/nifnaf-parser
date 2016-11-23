package de.dkt.nifnafparser;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Span;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIF;
import de.dkt.common.niftools.NIFReader;

public class Nif2NafConverter {

	private Logger logger = Logger.getLogger(Nif2NafConverter.class);

	private KAFDocument targetNafDoc;
	private Model srcNifDoc;
	private String textContent;
	String lang;
	//private Map<String,String> wfidToWordMap;
	private Map<String,String> wordToIdMap;

	public Nif2NafConverter(Model inModel, String lang, String nafVersion) throws Exception {
		// represent src nif doc ; 
		this.srcNifDoc = inModel;
		// represent target naf doc
		this.targetNafDoc = new KAFDocument(lang,nafVersion);
		
		this.wordToIdMap = new HashMap<String,String>();

		this.textContent = NIFReader.extractIsString(this.srcNifDoc);
		this.lang = lang;
	}

	//@Override
	public String convert() {

		buildTextTag();
		buildTermTag();
		
		this.targetNafDoc.setRawText(this.textContent);

		extractEntities();
		extractTemporalEntities();

		String naf = this.targetNafDoc.toString();
		return naf;
	}


	private void extractTemporalEntities(){
		String NIF = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
		
		Map<String, Map<String,String>> tempEntities = NIFReader.extractTemporalEntitiesExtended(this.srcNifDoc);
//		List<String[]> tempExpressions = NIFReader.extractTemporalExpressions(this.srcNifDoc);
		
		Set<String> keys = tempEntities.keySet();
		for(String key:keys){
			Map<String, String> entityPredObjMap = tempEntities.get(key);
			
			String offset = entityPredObjMap.get(NIF+"beginIndex");
			String temporalEntity = entityPredObjMap.get(NIF+"anchorOf");
			
			List<String> tokenizedWords = new ArrayList<String>();
			List<Integer> beginIndices = this.getTokenizedString(temporalEntity, tokenizedWords);
		
			List<String> wordIds = new ArrayList<String>();
			int w = 0;
			for(String word : tokenizedWords){
				
				int offsetInt = Integer.parseInt(offset);
				int nb = offsetInt + beginIndices.get(w);
				wordIds.add(this.wordToIdMap.get(word+String.valueOf(nb)));
				w++;
			}
			
			// add new Entity
			List<Term> terms = this.targetNafDoc.getTermsFromWFs(wordIds);
			Span<Term> termSpan = KAFDocument.newTermSpan(terms);
			List<Span<Term>> targets = new ArrayList<Span<Term>>();
			targets.add(termSpan);
			this.targetNafDoc.newEntity("Time", targets);  

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
			this.targetNafDoc.newTimex3("Time").setSpan(wfSpan);;
		}
	}
	
	private void extractEntities(){
		
		//TODO extract all taClassRefs! 
		//List<String> taClassRefs = NIFReader.extractTaClassRefsFromModel(this.srcNifDoc);

		List<String[]> entities = NIFReader.extractEntityIndices(this.srcNifDoc);
		//returned String Arrays contain: taIdentRef, anchorOf, taClassRef, begin, End
		for(String[] entity : entities){
			//anchorOf
			String namedEntity = entity[1]; 
			String beginIdx = entity[3];
			
			List<String> wfids = new ArrayList<String>();
			List<String> tokenizedWords = new ArrayList<String>();

			this.getTokenizedString(namedEntity, tokenizedWords);
			
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
			
			
			//TODO taClassRef: wird immer nur das erste extracted?
			String type;
			if(entity[2].toLowerCase().contains("Person") || entity[2].toLowerCase().contains("Time") || entity[2].toLowerCase().contains("Location") 
					|| entity[2].toLowerCase().contains("Organization")|| entity[2].toLowerCase().contains("Money")
					|| entity[2].toLowerCase().contains("Percent") || entity[2].toLowerCase().contains("Date")){
				type = entity[2];
			}else{ type = "MISC";}

			this.targetNafDoc.newEntity(type, references);  
			
			//taIdentRef TODO: how to attach externalRef to this entity? 
			this.targetNafDoc.newExternalRef(entity[0]);
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

		Locale[] availableLocales = Locale.getAvailableLocales();
		Locale locale = Locale.ENGLISH;
		
		// TODO adapt language
		BreakIterator sentTokenizer = BreakIterator.getSentenceInstance(locale);
		sentTokenizer.setText(this.textContent);
		
		BreakIterator wordTokenizer = BreakIterator.getWordInstance(locale);

		//sentence-level
		int start = sentTokenizer.first();
		int sent = 0;
		for (int end = sentTokenizer.next(); end != BreakIterator.DONE; start = end, end = sentTokenizer.next()) {
			sent++;
			// word-level
			String currentSent = this.textContent.substring(start,end);
			List<String> tokenizedWords = new ArrayList<String>();
			List<Integer> startIndices = this.getTokenizedString(currentSent, tokenizedWords);
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
		
		//Map<String,String> wfidToWordMap = new HashMap<String, String>();
		List<WF> allWFs = this.targetNafDoc.getWFs();
		for(WF wf : allWFs){
			String id = wf.getId();
			String word = wf.getForm();
			int offset = wf.getOffset();
//			wfidToWordMap.put(id, word);
			this.wordToIdMap.put(word+offset, id);
		}
		//this.wfidToWordMap = wfidToWordMap;
	}

	
	
	private List<Integer> getTokenizedString(String stringOfWords, List<String> tokenizedWords){
		//TODO adapt language
		BreakIterator wordTokenizer = BreakIterator.getWordInstance(Locale.ENGLISH);
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
