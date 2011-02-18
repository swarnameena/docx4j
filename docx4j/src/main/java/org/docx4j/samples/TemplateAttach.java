/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.samples;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.contenttype.CTDefault;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.contenttype.ObjectFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Tbl;

/**
 * Creates a WordprocessingML document from scratch,
 * and attaches a template
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class TemplateAttach extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/TemplateAttach_out.docx";	    	
		}
		
		boolean save = 
			(inputfilepath == null ? false : true);
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");

		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
		
		// Create settings part, and init content
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = Context.getWmlObjectFactory().createCTSettings();
		dsp.setJaxbElement(settings);
		wordMLPackage.getMainDocumentPart().addTargetPart(dsp);
		
		// Create external rel
		RelationshipsPart rp = RelationshipsPart.createRelationshipsPartForPart(dsp); 		
		org.docx4j.relationships.Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType( "http://schemas.openxmlformats.org/officeDocument/2006/relationships/attachedTemplate"  );
		rel.setTarget("file:///C:\\Users\\jsmith\\AppData\\Roaming\\Microsoft\\Templates\\yours.dotm");
		rel.setTargetMode("External");  		
		rp.addRelationship(rel); // addRelationship sets the rel's @Id
		
		settings.setAttachedTemplate(
				(CTRel)XmlUtils.unmarshalString("<w:attachedTemplate xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + rel.getId() + "\"/>", Context.jc, CTRel.class)
				);
		 
		// or (yuck)... 
//		CTRel id = new CTRel();
//		id.setId( rel.getId() );
//		JAXBElement<CTRel> je = new JAXBElement<CTRel>(
//				new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "attachedTemplate"), 
//				CTRel.class, null, id);
//		settings.setAttachedTemplate(je.getValue());
		
		
		
		// Now save it
		if (save) {
			wordMLPackage.save(new java.io.File(inputfilepath) );
			System.out.println("Saved " + inputfilepath);
		} else {
		   	// Create a org.docx4j.wml.Package object
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
			org.docx4j.xmlPackage.Package pkg = worker.get();
	    	
	    	// Now marshall it
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			System.out.println( "\n\n OUTPUT " );
			System.out.println( "====== \n\n " );	
			marshaller.marshal(pkg, System.out);				
			
		}
		
		System.out.println("Done.");
				
	}
	
	
}
