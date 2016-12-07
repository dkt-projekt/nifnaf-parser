package de.dkt.nifnaf_parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.ValidationHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;


public class NifNafParser {

	TestHelper testHelper;
	ValidationHelper validationHelper;
	String indexPath = "";
	
	@Before
	public void setup() {
		ApplicationContext context = IntegrationTestSetup
				.getContext(TestConstants.pathToPackage);
		testHelper = context.getBean(TestHelper.class);
		validationHelper = context.getBean(ValidationHelper.class);
		
		
	}
	
	private HttpRequestWithBody nifnafParserRequest() {
		String url = testHelper.getAPIBaseUrl() + "/nifnaf-parser";
		return Unirest.post(url);
	}
	
	
	
	
	@Test
	public void basicNifInNafOutTest() throws UnirestException, IOException,Exception {
		
		HttpResponse<String> response = nifnafParserRequest()
				.queryString("informat", "turtle")
				.queryString("outputformat", "NAF")
				.queryString("language", "en")
				.queryString("input", TestConstants.nifInput)
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		Assert.assertEquals(TestConstants.expectedResult, response.getBody());
		
		
	}
	
	

}
