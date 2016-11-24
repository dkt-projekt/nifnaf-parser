package de.dkt.nifnafparser;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFReader;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;


@RestController
public class NifNafParserController extends BaseRestController{

	Logger logger = Logger.getLogger(NifNafParserController.class);
	
	
	@RequestMapping(value = "/nifnaf-parser", method = RequestMethod.POST)
	public ResponseEntity<String> parse(		
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody(required = false) String postBody,
            @RequestParam(value = "nif-version", required = false) String nifVersion,
            @RequestParam(value ="naf-version", defaultValue = "v3") String nafVersion,
            @RequestParam(value="lang", defaultValue = "en") String lang,
            @RequestParam(value = "outputformat") String outputFormat,
			@RequestParam Map<String, String> allParams) throws Exception {
		
		// normalizeNif takes care of input vs. body for instance
		NIFParameterSet parameters = getRestHelper().normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, false);
		// send content of NAF file to converter
		//FormatConverter converter; 
		ResponseEntity<String> response;
		if(outputFormat.equals("NIF")){
			// naf to nif
			Naf2NifConverter nafConverter = new Naf2NifConverter(parameters.getInput());
			Model outputModel = nafConverter.convert();
			response = createSuccessResponse(outputModel, parameters.getOutformatString());
		}else{
			// nif to naf
			Model inModel = NIFReader.extractModelFromTurtleString(parameters.getInput());
			Nif2NafConverter nifConverter = new Nif2NafConverter(inModel, lang, nafVersion);
			String content = nifConverter.convert();
			response = new ResponseEntity<String>(content,HttpStatus.OK);
		}
		
		if(!Strings.isNullOrEmpty(nifVersion)){
			parameters.setNifVersion(nifVersion);}

		return response;
	}

}
