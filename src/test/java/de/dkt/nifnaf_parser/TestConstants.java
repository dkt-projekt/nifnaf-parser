package de.dkt.nifnaf_parser;

public class TestConstants {
	
	public static final String pathToPackage = "rdftest/nifnaf-test-package.xml";

	
	static String nifInput = 
			"@prefix dktnif: <http://dkt.dfki.de/ontologies/nif#> .\n" +
					"@prefix geo:   <http://www.w3.org/2003/01/geo/wgs84_pos/> .\n" +
					"@prefix dbo:   <http://dbpedia.org/ontology/> .\n" +
					"@prefix nif-ann: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-annotation#> .\n" +
					"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
					"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
					"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
					"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
					"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
					"@prefix time:  <http://www.w3.org/2006/time#> .\n" +
					"\n" +
					"<http://dkt.dfki.de/documents/#char=11,17>\n" +
					"        a                     nif:RFC5147String , nif:String ;\n" +
					"        nif:anchorOf          \"Berlin\"^^xsd:string ;\n" +
					"        nif:beginIndex        \"11\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:endIndex          \"17\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:referenceContext  <http://dkt.dfki.de/documents/#char=0,25> ;\n" +
					"        geo:lat               \"52.516666666666666\"^^xsd:double ;\n" +
					"        geo:long              \"13.383333333333333\"^^xsd:double ;\n" +
					"        itsrdf:taClassRef     dbo:Location ;\n" +
					"        itsrdf:taIdentRef     <http://dbpedia.org/resource/Berlin> .\n" +
					"\n" +
					"<http://dkt.dfki.de/documents/#char=21,25>\n" +
					"        a                      nif:RFC5147String , nif:String ;\n" +
					"        nif:anchorOf           \"2016\"^^xsd:string ;\n" +
					"        nif:beginIndex         \"21\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:endIndex           \"25\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:referenceContext   <http://dkt.dfki.de/documents/#char=0,25> ;\n" +
					"        itsrdf:taClassRef      time:TemporalEntity ;\n" +
					"        time:intervalFinishes  \"2017-01-01T00:00:00\"^^xsd:dateTime ;\n" +
					"        time:intervalStarts    \"2016-01-01T00:00:00\"^^xsd:dateTime .\n" +
					"\n" +
					"<http://dkt.dfki.de/documents/#char=0,25>\n" +
					"        a                        nif:Context , nif:String , nif:RFC5147String ;\n" +
					"        dktnif:averageLatitude   \"52.516666666666666\"^^xsd:double ;\n" +
					"        dktnif:averageLongitude  \"13.383333333333333\"^^xsd:double ;\n" +
					"        dktnif:meanDateEnd       \"2017-01-01T01:00:00\"^^xsd:dateTime ;\n" +
					"        dktnif:meanDateStart     \"2016-01-01T01:00:00\"^^xsd:dateTime ;\n" +
					"        dktnif:standardDeviationLatitude\n" +
					"                \"0.0\"^^xsd:double ;\n" +
					"        dktnif:standardDeviationLongitude\n" +
					"                \"0.0\"^^xsd:double ;\n" +
					"        nif:beginIndex           \"0\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:endIndex             \"25\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:isString             \"Welcome to Berlin in 2016\"^^xsd:string .\n" +
					"";
	
	
	static String expectedResult = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<NAF xml:lang=\"en\" version=\"v3\">\n" +
					"  <nafHeader>\n" +
					"    <public uri=\"http://dkt.dfki.de/documents/#char=0,25\" />\n" +
					"  </nafHeader>\n" +
					"  <raw><![CDATA[Welcome to Berlin in 2016]]></raw>\n" +
					"  <text>\n" +
					"    <wf id=\"w1\" offset=\"0\" length=\"7\" sent=\"1\">Welcome</wf>\n" +
					"    <wf id=\"w2\" offset=\"8\" length=\"2\" sent=\"1\">to</wf>\n" +
					"    <wf id=\"w3\" offset=\"11\" length=\"6\" sent=\"1\">Berlin</wf>\n" +
					"    <wf id=\"w4\" offset=\"18\" length=\"2\" sent=\"1\">in</wf>\n" +
					"    <wf id=\"w5\" offset=\"21\" length=\"4\" sent=\"1\">2016</wf>\n" +
					"  </text>\n" +
					"  <terms>\n" +
					"    <!--Welcome-->\n" +
					"    <term id=\"t1\">\n" +
					"      <span>\n" +
					"        <target id=\"w1\" />\n" +
					"      </span>\n" +
					"    </term>\n" +
					"    <!--to-->\n" +
					"    <term id=\"t2\">\n" +
					"      <span>\n" +
					"        <target id=\"w2\" />\n" +
					"      </span>\n" +
					"    </term>\n" +
					"    <!--Berlin-->\n" +
					"    <term id=\"t3\">\n" +
					"      <span>\n" +
					"        <target id=\"w3\" />\n" +
					"      </span>\n" +
					"    </term>\n" +
					"    <!--in-->\n" +
					"    <term id=\"t4\">\n" +
					"      <span>\n" +
					"        <target id=\"w4\" />\n" +
					"      </span>\n" +
					"    </term>\n" +
					"    <!--2016-->\n" +
					"    <term id=\"t5\">\n" +
					"      <span>\n" +
					"        <target id=\"w5\" />\n" +
					"      </span>\n" +
					"    </term>\n" +
					"  </terms>\n" +
					"  <entities>\n" +
					"    <entity id=\"e1\" type=\"Time\">\n" +
					"      <references>\n" +
					"        <!--2016-->\n" +
					"        <span>\n" +
					"          <target id=\"t5\" />\n" +
					"        </span>\n" +
					"      </references>\n" +
					"      <externalReferences>\n" +
					"        <externalRef resource=\"http://www.w3.org/2006/time#TemporalEntity\" />\n" +
					"      </externalReferences>\n" +
					"    </entity>\n" +
					"    <entity id=\"e2\" type=\"MISC\">\n" +
					"      <references>\n" +
					"        <!--Berlin-->\n" +
					"        <span>\n" +
					"          <target id=\"t3\" />\n" +
					"        </span>\n" +
					"      </references>\n" +
					"      <externalReferences>\n" +
					"        <externalRef resource=\"http://dbpedia.org/ontology/Location\" reference=\"http://dbpedia.org/resource/Berlin\" />\n" +
					"      </externalReferences>\n" +
					"    </entity>\n" +
					"  </entities>\n" +
					"  <timeExpressions>\n" +
					"    <timex3 id=\"tmx1\" type=\"DURATION\" beginPoint=\"tmx2\" endPoint=\"tmx3\">\n" +
					"      <!--2016-->\n" +
					"      <span>\n" +
					"        <target id=\"w5\" />\n" +
					"      </span>\n" +
					"    </timex3>\n" +
					"    <timex3 id=\"tmx2\" type=\"TIME\" value=\"2016-01-01T00:00:00\" />\n" +
					"    <timex3 id=\"tmx3\" type=\"TIME\" value=\"2017-01-01T00:00:00\" />\n" +
					"  </timeExpressions>\n" +
					"</NAF>\n" +
					"";
	

}
