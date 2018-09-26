/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is "Simplenlg".
 *
 * The Initial Developer of the Original Code is Ehud Reiter, Albert Gatt and Dave Westwater.
 * Portions created by Ehud Reiter, Albert Gatt and Dave Westwater are Copyright (C) 2010-11 The University of Aberdeen. All Rights Reserved.
 *
 * Contributor(s): Ehud Reiter, Albert Gatt, Dave Wewstwater, Roman Kutlak, Margaret Mitchell, Pierre-Luc Vaudry.
 */

package simplenlg.lexicon.dutch;

import org.w3c.dom.Node;
import simplenlg.features.*;
import simplenlg.features.dutch.DutchLexicalFeature;
import simplenlg.features.dutch.PronounType;
import simplenlg.framework.*;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;

/**
 * Extension of simplenlg.lexicon.XMLLexicon for Dutch.
 * 
 * @author vaudrypl, rfdj
 *
 */
public class XMLLexicon extends simplenlg.lexicon.XMLLexicon {

	/**********************************************************************/
	// constructors
	/**********************************************************************/

	/**
	 * Load an XML Lexicon from a named file
	 * 
	 * @param filename
	 */
	public XMLLexicon(String filename) {
		super(Language.DUTCH, filename);
	}

	/**
	 * Load an XML Lexicon from a File
	 * 
	 * @param file
	 */
	public XMLLexicon(File file) {
		super(Language.DUTCH, file);
	}

	/**
	 * Load an XML Lexicon from a URI
	 * 
	 * @param lexiconURI
	 */
	public XMLLexicon(URI lexiconURI) {
		super(Language.DUTCH, lexiconURI);
	}

	public XMLLexicon() {
		super(Language.DUTCH);
	}



	/**
	 * create a simplenlg WordElement from a Word node in a lexicon XML file
	 * based on superclass
	 * 
	 * @param wordNode
	 * @return
	 * @throws XPathUtilException
	 */
	@Override
	protected WordElement convertNodeToWord(Node wordNode) {
		WordElement word = super.convertNodeToWord( wordNode );
		
		// converts String to Gender value for feature LexicalFeature.GENDER
		if (word.hasFeature(LexicalFeature.GENDER)) {
			String stringValue = word.getFeatureAsString(LexicalFeature.GENDER);
			Gender genderValue = Gender.valueOf(stringValue.toUpperCase());
			word.setFeature(LexicalFeature.GENDER, genderValue);
		}
		
		// converts String to NumberAgreement value for feature Feature.NUMBER
		if (word.hasFeature(Feature.NUMBER)) {
			String stringValue = word.getFeatureAsString(Feature.NUMBER);
			NumberAgreement numberValue = NumberAgreement.valueOf(stringValue.toUpperCase());
			word.setFeature(Feature.NUMBER, numberValue);
		}
		
		// converts String to Person value for feature Feature.PERSON
		if (word.hasFeature(Feature.PERSON)) {
			String stringValue = word.getFeatureAsString(Feature.PERSON);
			Person personValue = Person.valueOf(stringValue.toUpperCase());
			word.setFeature(Feature.PERSON, personValue);
		}
		
		// converts String to DiscourseFunction value for feature InternalFeature.DISCOURSE_FUNCTION
		if (word.hasFeature(InternalFeature.DISCOURSE_FUNCTION)) {
			String stringValue = word.getFeatureAsString(InternalFeature.DISCOURSE_FUNCTION);
			DiscourseFunction functionValue = DiscourseFunction.valueOf(stringValue.toUpperCase());
			word.setFeature(InternalFeature.DISCOURSE_FUNCTION, functionValue);
		}
		
		// converts String to PronounType value for feature DutchLexicalFeature.PRONOUN_TYPE
		if (word.hasFeature(DutchLexicalFeature.PRONOUN_TYPE)) {
			String stringValue = word.getFeatureAsString(DutchLexicalFeature.PRONOUN_TYPE);
			PronounType pronounType = PronounType.valueOf(stringValue.toUpperCase());
			word.setFeature(DutchLexicalFeature.PRONOUN_TYPE, pronounType);
		}
		
		return word;
	}
	
	/**
	 * routine for getting morph variants
	 * based on English for now, should be augmented
	 * 
	 * @param word
	 * @return set of variants of the word
	 */
	@Override
	protected Set<String> getVariants(WordElement word) {
		// base form of the word added by superclass
		Set<String> variants = super.getVariants(word);
		
		ElementCategory category = word.getCategory();
		if (category instanceof LexicalCategory) {
			InflectedWordElement inflected = new InflectedWordElement(word);
			switch ((LexicalCategory) category) {
			case NOUN:
				// singular and plural forms
				addVariant(variants, inflected);
				inflected.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
				addVariant(variants, inflected);
				break;

			case ADJECTIVE:
			case DETERMINER:
				// masculine singular and plural, feminine plural and singular forms
				addVariant(variants, inflected);
				inflected.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
				addVariant(variants, inflected);
				inflected.setFeature(LexicalFeature.GENDER, Gender.FEMININE);
				addVariant(variants, inflected);
				inflected.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
				addVariant(variants, inflected);
				// optional variant : "liaison" form
				addVariant(variants, word, DutchLexicalFeature.LIAISON);
				break;

			case VERB:
				// indicative, varrying tense, person and number
				for (Tense tense : Tense.values()) {
					for (Person person : Person.values()) {
						for (NumberAgreement number : Arrays.asList(NumberAgreement.SINGULAR, NumberAgreement.PLURAL)) {
							inflected.setFeature(Feature.TENSE, tense);
							inflected.setFeature(Feature.PERSON, person);
							inflected.setFeature(Feature.NUMBER, number);
							addVariant(variants, inflected);
						}
					}
				}
				// imperative present, varrying person and number
				inflected.setFeature(Feature.FORM, Form.IMPERATIVE);
				inflected.setFeature(Feature.TENSE, Tense.PRESENT);
				for (Person person : Person.values()) {
					for (NumberAgreement number : Arrays.asList(NumberAgreement.SINGULAR, NumberAgreement.PLURAL)) {
						inflected.setFeature(Feature.PERSON, person);
						inflected.setFeature(Feature.NUMBER, number);
						addVariant(variants, inflected);
					}
				}
				// participles, varying gender and number
				inflected = new InflectedWordElement(word);
				for (Form form : Arrays.asList(Form.PRESENT_PARTICIPLE, Form.PAST_PARTICIPLE)) {
					for (Gender gender : Arrays.asList(Gender.MASCULINE,Gender.FEMININE)) {
						for (NumberAgreement number : Arrays.asList(NumberAgreement.SINGULAR, NumberAgreement.PLURAL)) {
							inflected.setFeature(Feature.FORM, form);
							inflected.setFeature(LexicalFeature.GENDER, gender);
							inflected.setFeature(Feature.NUMBER, number);
							addVariant(variants, inflected);
						}
					}
				}
				break;
			
			default:
				// only base needed for other forms
				break;
			}
		}
		return variants;
	}
	
	/**
	 * Checks if a feature is not empty before adding it to the variants set.
	 * 
	 * @param variants	set of variants of the word, may already contain elements
	 * @param word
	 * @param feature	feature potentially containing a variant of the word
	 */
	protected void addVariant(Set<String> variants, WordElement word,
							  String feature)
	{
		String featureString = word.getFeatureAsString(feature);
		if (featureString != null && !featureString.equals("")) {
			variants.add(featureString);
		}
	}
	
	protected void addVariant(Set<String> variants, InflectedWordElement inflected) {
		variants.add( inflected.realiseMorphology().getRealisation() );
	}
}
