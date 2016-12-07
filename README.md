# nifnaf-parser

The NIF-NAF Parser takes a NIF (NAF) document as input and converts it to NAF (NIF).

# Requirements
 * Dependencies
 This project uses the kaflib project:
 ```
		<dependency>
			<groupId>com.github.ixa-ehu</groupId>
			<artifactId>kaflib-naf</artifactId>
			<version>1.1.15</version>
		</dependency>
	```
 * NAF-File
 The NAF File must specify the uri of the document in the *uri* attribute of its public tag.


# Endpoint

## Parameters
*Optional*: 
naf-version: defaults to v3

nif-version: defaults to 2.0

lang: defaults to english ('en') Other languages are not supported for now.

*Required*
outputformat: If set to 'NIF' NAF will be converted to NIF, otherwise NIF to NAF.



# Output

The resulting NIF (NAF) - Document contains the following mappings.

So far, only Named Entities and temporal Entities are considered. 	
**Named Entities**
The object with the property nif:isString in the NIF-Document is mapped into the raw tag of the NAF-Document.
Named Entities in the NIF-Document are mapped into the <entities> layer of the NAF-Document.
The first class of the entitiy, i.e. the first object having property nif:taClassRef, is mapped to the entitie's externalReferences
attribute *resource* while the object having property nif:taIdentRef is mapped, if it exists, to the attribute *reference*.

**Temporal Entities**
Temporal Entities in NIF, i.e. subjects that have the property itsrdf:taClassRef with the object time:TemporalEntity are mapped to the
<timeExpressions> layer in the NAF-Document. So far, the type is always DURATION. The properties time:intervalFinishes and 
time:intervalStarts are mapped to a timeExpression each having the type *TIME*. Their ids are the values of the begin- and endPoint 
attribute for the time Expression that is mapped. 

