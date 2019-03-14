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

package simplenlg.morphology.dutch;

import simplenlg.features.*;
import simplenlg.features.dutch.DutchFeature;
import simplenlg.features.dutch.DutchLexicalFeature;
import simplenlg.features.dutch.PronounType;
import simplenlg.framework.*;
import simplenlg.lexicon.Lexicon;
import simplenlg.morphology.MorphologyRulesInterface;
import simplenlg.syntax.dutch.VerbPhraseHelper;

import java.util.*;

import static simplenlg.features.NumberAgreement.SINGULAR;
import static simplenlg.syntax.dutch.VerbPhraseHelper.getSeparableCompoundVerb;

/**
 * Morphology rules for Dutch.
 * To allow overriding of its methods, this class's methods are not static.
 *
 * 
 * @author vaudrypl, rfdj
 *
 */
public class MorphologyRules extends simplenlg.morphology.english.NonStaticMorphologyRules
		implements MorphologyRulesInterface {
	
	public static final String a_o_regex = "\\A[aäàâoô].*";

	/**
	 * This method performs the morphology for determiners.
	 * It returns a StringElement made from the baseform, or
	 * the plural or feminine singular form of the determiner
	 * if it applies.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 */
	@Override
	public NLGElement doDeterminerMorphology(InflectedWordElement element) {
		String inflectedForm;
		// Get gender from parent, or from self if there is no parent
		NLGElement parent = element.getParent();
		Object gender = null;
		
		if (parent != null) gender = parent.getFeature(LexicalFeature.GENDER);
		else gender = element.getFeature(LexicalFeature.GENDER);
		
		boolean feminine = Gender.FEMININE.equals( gender );
		
		// plural form
		if (element.isPlural() && 
			element.hasFeature(LexicalFeature.PLURAL)) {
			inflectedForm = element.getFeatureAsString(LexicalFeature.PLURAL);
			
			if (feminine && element.hasFeature(DutchLexicalFeature.FEMININE_PLURAL)) {
				inflectedForm = element.getFeatureAsString(DutchLexicalFeature.FEMININE_PLURAL);
			}
			
			// "des" -> "de" in front of noun premodifiers
			if (parent != null && "des".equals(inflectedForm)) {
				List<NLGElement> preModifiers = parent.getFeatureAsElementList(InternalFeature.PREMODIFIERS);
				if (!preModifiers.isEmpty()) {
					inflectedForm = "de";
				}
			}
			
		// feminine singular form
		} else if (feminine	&& element.hasFeature(DutchLexicalFeature.FEMININE_SINGULAR)) {
			inflectedForm = element.getFeatureAsString(DutchLexicalFeature.FEMININE_SINGULAR);
		// masculine singular form
		} else {
			inflectedForm = element.getBaseForm();
			// remove particle if the determiner has one
			String particle = getParticle(element);
			inflectedForm = inflectedForm.replaceFirst(particle, "");
			inflectedForm = inflectedForm.trim();
		}
		
		StringElement realisedElement = new StringElement(inflectedForm, element);
		return realisedElement;
	}
	
	/**
	 * This method performs the morphology for adjectives.
	 * Based in part on the same method in the english rules
	 * Reference: http://www.taalportaal.org/taalportaal/topic/pid/topic-13998813296919801
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	@Override
	public NLGElement doAdjectiveMorphology(
			InflectedWordElement element, WordElement baseWord) {

		String realised = null;
        Object patternValue = element.getFeature(Feature.PATTERN);
        if (patternValue != Pattern.REGULAR_DOUBLE) {
            checkIfRegularDouble(element, baseWord);
            patternValue = element.getFeature(Feature.PATTERN);
        }



		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);
        NLGElement parent = element.getParent();



        if (element.getFeatureAsBoolean(Feature.IS_COMPARATIVE)) {
            realised = element.getFeatureAsString(LexicalFeature.COMPARATIVE);

            if (realised == null && baseWord != null) {
                realised = baseWord.getFeatureAsString(LexicalFeature.COMPARATIVE);
            }
            if (realised == null) {
                if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
                    realised = buildDoubleCompAdjective(baseForm);
                } else {
                    realised = buildRegularComparative(baseForm);
                }
            }
        } else if (element.getFeatureAsBoolean(Feature.IS_SUPERLATIVE)) {

            realised = element.getFeatureAsString(LexicalFeature.SUPERLATIVE);

            if (realised == null && baseWord != null) {
                realised = baseWord.getFeatureAsString(LexicalFeature.SUPERLATIVE);
            }
            if (realised == null) {
                realised = buildRegularSuperlative(baseForm);
            }

        } else if (parent != null) {
            String specifier = parent.getFeatureAsString(InternalFeature.SPECIFIER);
            if ("een".equals(specifier)){
                realised = baseForm;
            }
        }

		// Get gender from parent or "grandparent" or self, in that order
		Object function = element.getFeature(InternalFeature.DISCOURSE_FUNCTION);
		boolean feminine = false;
		if (parent != null) {
			if (function == DiscourseFunction.HEAD) {
				function = parent.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			}

			if (!parent.hasFeature(LexicalFeature.GENDER) && parent.getParent() != null) {
				parent = parent.getParent();
			}
		} else {
			parent = element;
		}

		// Plural, common gender, definite determiner or possesive pronoun, therefore, add -e.
		int length = baseForm.length();
        String specifier = parent.getFeatureAsString(InternalFeature.SPECIFIER);
        String regexCVVC = ".*" // anything, followed by a consonant
                + "([aeiouAEIOU])\\1" // two repeated vowels
                + "[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]";


        if (realised == null) {

            if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
                realised =  baseForm + baseForm.charAt(baseForm.length() - 1);
            } else if (baseForm.matches(regexCVVC)
					&& (parent.isPlural()
					|| parent.getFeature(LexicalFeature.GENDER) == Gender.COMMON
					|| (!"een".equals(specifier)
						&& ! "geen".equals(specifier)))
					&& parent.getCategory() != PhraseCategory.VERB_PHRASE) {
                String lastConsonant = baseForm.substring(length -1);
                String front = baseForm.substring(0, length - 2);
                realised = front + lastConsonant;
            } else {
                realised = baseForm;
            }

			String regexVCC = ".*"
					+ "[aeiouyAEIOUY]"
					+ "[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]{2,}"; // ending in two or more consonants

			if (!baseForm.matches(regexVCC)) {
				if (baseForm.endsWith("f")) {
					realised = realised.substring(0, realised.length() - 1) + "v";
				} else if (baseForm.endsWith("s")) {
					realised = realised.substring(0, realised.length() - 1) + "z";
				}
			}
        }

        if ((parent.isPlural()
                    || parent.getFeature(LexicalFeature.GENDER) == Gender.COMMON
                    || (!"een".equals(specifier)
						&& ! "geen".equals(specifier)))
                    && parent.getCategory() != PhraseCategory.VERB_PHRASE
                    && !realised.endsWith("e")) {
            if (realised.matches(regexCVVC)) {
                String lastConsonant = realised.substring(realised.length() - 1);
                String front = realised.substring(0, realised.length() - 2);
                realised = front + lastConsonant;
            }

            realised += "e";
		} else {
        	// Roll back repeated consonant
			if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
				realised = baseForm;
			}

			// roll back f/v and s/z changes
            if (realised.endsWith("v")) {
                realised = realised.substring(0, realised.length() - 1) + "f";
            } else if (realised.endsWith("z")) {
                realised = realised.substring(0, realised.length() - 1) + "s";
            }
        }

		realised += getParticle(element);
		StringElement realisedElement = new StringElement(realised, element);
		return realisedElement;
	}

    /**
     * Checks if the element is a regular word of which the last consonant has to be duplicated before adding suffixes.
     * This feature {@code Feature.PATTERN} can be set by the user.
     *
     * @param element
     *            the <code>InflectedWordElement</code>.
     * @param baseWord
     *            the <code>WordElement</code>.
     */
    protected void checkIfRegularDouble(InflectedWordElement element, WordElement baseWord) {
        if (element.getFeature(Feature.PATTERN) == null) {
            String baseForm = baseWord.getBaseForm();
            String regexCVC = ".*[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]" // anything, followed by a consonant
                    + "[aeiouyAEIOUY]" // a vowel
                    + "[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]";

            if (baseForm.matches(regexCVC)
					&& !baseForm.endsWith("ig")
					&& !baseForm.endsWith("ijk")) {
                element.setFeature(Feature.PATTERN, Pattern.REGULAR_DOUBLE);
            }

        }

    }

    /**
     * Builds the comparative form for adjectives that follow the doubling form
     * of the last consonant. <em>-er</em> is added to the end after the last
     * consonant is doubled. For example, <em>fat</em> becomes <em>fatter</em>.
     *
     * @param baseForm
     *            the base form of the word.
     * @return the inflected word.
     */
    protected String buildDoubleCompAdjective(String baseForm) {
        String morphology = null;
        if (baseForm != null) {
            morphology = baseForm + baseForm.charAt(baseForm.length() - 1)
                    + "er"; //$NON-NLS-1$
        }
        return morphology;
    }

    /**
     * Builds the comparative form for regular adjectives.
     * References: http://ans.ruhosting.nl/e-ans/06/04/03/01/01/body.html
     *             http://taaladvies.net/taal/advies/tekst/92/omschreven_trappen_van_vergelijking_algemeen/
     *
     * @param baseForm
     *            the base form of the word.
     * @return the inflected word.
     */
    protected String buildRegularComparative(String baseForm) {
        String morphology = null;
        if (baseForm != null) {

            // remove double vowel
            int length = baseForm.length();
            String regexCVVC = ".*" // anything
                    + "([aeiouAEIOU])\\1" // two repeated vowels
                    + "[b-df-hj-npqstv-xzB-DF-HJ-NPQSTV-XZ]"; // not 'r', because that is handled with a suffix

            // replace trailing 'f' with 'v' and 's' with 'z'
            if (baseForm.matches(".*([aeiouyAEIOUY]|(ij)|(ei)|(ui)|(au)|(ou))[fsFS]")) {
                if (baseForm.endsWith("f")) {
                    baseForm = baseForm.substring(0, length - 1) + "v";
                } else if (baseForm.endsWith("s")) {
                    baseForm = baseForm.substring(0, length - 1) + "z";
                }
            }

            if (baseForm.matches(regexCVVC)) {
                String lastConsonant = baseForm.substring(length -1);
                String front = baseForm.substring(0, length - 2);
                baseForm = front + lastConsonant;
            }


            if (baseForm.endsWith("r")) { //$NON-NLS-1$
                morphology = baseForm + "der"; //$NON-NLS-1$
            } else {
                morphology = baseForm + "er"; //$NON-NLS-1$
            }

        }
        return morphology;
    }

    /**
     * Builds the superlative form for regular adjectives.
     *
     * @param baseForm
     *            the base form of the word.
     * @return the inflected word.
     */
    protected String buildRegularSuperlative(String baseForm) {
        String morphology = null;
        if (baseForm != null) {
            if (baseForm.endsWith("st")
                    || baseForm.endsWith("sd")
                    || baseForm.endsWith("s")
                    || baseForm.endsWith("sch")
                    || baseForm.endsWith("sk")
                    || baseForm.endsWith("de")) {

                // replace trailing 'f' with 'v' and 's' with 'z'
                if (baseForm.matches(".*([aeiouyAEIOUY]|(ij)|(ei)|(ui)|(au)|(ou))[fsFS]")) {
                    if (baseForm.endsWith("f")) {
                        baseForm = baseForm.substring(0, baseForm.length() - 1) + "v";
                    } else if (baseForm.endsWith("s")) {
                        baseForm = baseForm.substring(0, baseForm.length() - 1) + "z";
                    }
                }

                morphology = "meest " + baseForm;
            } else {
                if (baseForm.endsWith("s") || baseForm.endsWith("sch")) { //$NON-NLS-1$
                    morphology = baseForm + "t"; //$NON-NLS-1$
                } else {
                    morphology = baseForm + "st"; //$NON-NLS-1$
                }
            }
        }
        return morphology;
    }

	/**
	 * Return an empty string if the element doesn't have a particle.
	 * If it has a non empty one, it returns it prepended by a dash.
	 * 
	 * @param element
	 * @return	the String to be appended to the element's realisation
	 */
	protected String getParticle(InflectedWordElement element) {
		String particle = element.getFeatureAsString(Feature.PARTICLE);
		
		if (particle == null) particle = "";
		else if (!particle.isEmpty()) particle = "-" + particle;
		
		return particle;
	}
	
	/**
	 * Builds the plural form of a noun following regular rules.
	 * References: http://taalportaal.org/taalportaal/topic/pid/topic-13998813298166667#section_gxn_b4l_kj
	 * 			   http://www.dutchgrammar.com/en/?n=NounsAndArticles.08 (.08 to .14)
	 * 			   http://ans.ruhosting.nl/e-ans/
	 * 
	 * @param form form being realised on which to apply the plural morphology
	 * @return the plural form
	 */
	public String buildRegularPlural(String form) {
		// Short rule: add -en if the last syllable is stressed, add -s if unstressed.
		// As the lexicon does not include stress, we try to use some rules.


		// References: http://www.dutchgrammar.com/en/?n=NounsAndArticles.11
		// 			   http://ans.ruhosting.nl/e-ans/03/05/03/body.html

		if(form.matches("[a-zA-Z-]+[aiouyAIOUY]")) {
			if(!form.endsWith("ee") && !form.endsWith("ie") && !form.endsWith("eau")) {
				// ends in a vowel, except for 'ee', 'ie' or 'eau', then add an apostrophe
				form += "'s";
			} else {
				form = addNounSSuffix(form);
			}
		}
		else if (form.endsWith("el") ||
				form.endsWith("em") ||
				form.endsWith("en") ||
				( form.endsWith("er") && !form.endsWith("oer") ) ||
				form.endsWith("aar") ||
				form.endsWith("aard") ||
				form.endsWith("erd") ||
				form.endsWith("je") ||
				form.endsWith("ster") ||
				form.endsWith("stel") ||
				form.endsWith("sel") ||
				form.endsWith("te") ||
				form.endsWith("age") ||
				form.endsWith("e") ||
				form.endsWith("ette") ||
				form.endsWith("eur") ||
				form.endsWith("ier") ||
				form.endsWith("trice") ||
				form.endsWith("ine") ||
				form.endsWith("oir")) {
			form = addNounSSuffix(form);
		} else if (form.length() == 1) {
			form += "'s";
		} else {
			form = addNounEnSuffix(form);
		}
		return form;
	}

	/**
	 * Adds an 's' as suffix in accordance with phonological rules
	 * Reference:
	 *
	 * @param form
	 * @return resultant form
	 */
	public String addNounSSuffix(String form) {

		String suffix = "s";

		return form + suffix;
	}

	/**
	 * Adds 'en' as suffix in accordance with phonological rules
	 * Reference:
	 *
	 * @param form
	 * @return resultant form
	 */
	public String addNounEnSuffix(String form) {
		int length = form.length();
		String regexCVVC = ".*" // anything, followed by a consonant
						 + "([aeiouAEIOU])\\1" // two repeated vowels
						 + "[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]";

		String regexCVC = ".*[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]" // one consonant
						+ "[aeiouyAEIOUY]" // a vowel or diphtong
						+ "[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]";

		String suffix = "en";


		if (form.endsWith("ij")) {

		} else if (form.endsWith("erik")) {
			suffix = "en";
		} else if (form.matches(regexCVVC)) {

			if (form.endsWith("f")) {
				form = form.substring(0, length - 1) + "v";
			} else if (form.endsWith("s")) {
				form = form.substring(0, length - 1) + "z";
			}
			String lastConsonant = form.substring(length -1);
			String front = form.substring(0, length - 2);
			form = front + lastConsonant;

		} else if (form.matches(regexCVC)) {

			String lastVowel = "";
			if (form.endsWith("ijf")) {
				form = form.substring(0, length - 3) + "ijv";
			} else if (form.endsWith("eis")) {
				form = form.substring(0, length - 3) + "eiz";
			} else if (form.endsWith("f")) {
				form = form.substring(0, length - 1) + "v";
			} else if (form.endsWith("s")) {
				form = form.substring(0, length - 1) + "z";
			} else {
				lastVowel = form.substring(length - 1);
			}
			suffix = lastVowel + "en";

		} else if (form.endsWith("heid")) {

		    form = form.substring(0, length - 4);
			suffix = "heden";

		}

		// form = form.substring(0, length-1) + "ç";
		return form + suffix;
	}

	/**
	 * This method performs the morphology for nouns.
	 * Based in part on the same method in the english rules
	 * Reference : sections 504-505 of Grevisse (1993)
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	@Override
	public StringElement doNounMorphology(
			InflectedWordElement element, WordElement baseWord) {
		String realised;

		// Check if the noun's gender needs to be changed
		// and change base form and baseWord accordingly
		if (baseWord != null) {
			Object elementGender = element.getFeature(LexicalFeature.GENDER);
			Object baseWordGender = baseWord.getFeature(LexicalFeature.GENDER);
			// The gender of the inflected word is opposite to the base word 
			if ((Gender.MASCULINE.equals(baseWordGender) &&	Gender.FEMININE.equals(elementGender))
				|| (Gender.FEMININE.equals(baseWordGender) && Gender.MASCULINE.equals(elementGender))) {
				
				String oppositeGenderForm = baseWord.getFeatureAsString(DutchLexicalFeature.OPPOSITE_GENDER);
				
				if (oppositeGenderForm == null) {
					// build opposite gender form if possible
					if (Gender.MASCULINE.equals(baseWordGender)) {
						// the base word is masculine and the feminine must be build
						// (to be completed if necessary)
					}
					else {
						// the base word is feminine and the masculine must be build
						// (to be completed if necessary)
					}
				}
				// if oppositeGenderForm is specified or has been built
				if (oppositeGenderForm != null) {
					// change base form and base word
					element.setFeature(LexicalFeature.BASE_FORM, oppositeGenderForm);
					baseWord = baseWord.getLexicon().lookupWord(oppositeGenderForm, LexicalCategory.NOUN);
					element.setBaseWord(baseWord);
				}
			}
		}
		
		// base form from element if it exists, otherwise from baseWord 
		String baseForm = getBaseForm(element, baseWord);
		
		if (element.isPlural()
				&& !element.getFeatureAsBoolean(LexicalFeature.PROPER)) {

			String pluralForm = null;

			pluralForm = element.getFeatureAsString(LexicalFeature.PLURAL);

			if (pluralForm == null && baseWord != null) {
				pluralForm = baseWord.getFeatureAsString(LexicalFeature.PLURAL);
			}
			
			if (pluralForm == null) {
				pluralForm = buildRegularPlural(baseForm);
			}
			realised = pluralForm;
		} else {
			realised = baseForm;
		}

		realised = checkPossessive(element, realised);
		realised += getParticle(element);
		StringElement realisedElement = new StringElement(realised, element);
		return realisedElement;
	}

	/**
	 * Checks to see if the noun is possessive. If it is then nouns in ending in
	 * <em>-s</em> become <em>-s'</em> while every other noun has <em>-'s</em> appended to
	 * the end.
	 *
	 * @param element
	 *            the <code>InflectedWordElement</code>
	 * @param realised
	 *            the realisation of the word.
	 * @return realised
	 *            the new realisation of the word.
	 */
	private static String checkPossessive(InflectedWordElement element,
										String realised) {

		if (element.getFeatureAsBoolean(Feature.POSSESSIVE)) {
			if (realised.charAt(realised.length() - 1) == 's') {
				realised += "'";
			} else {
				realised += "'s";
			}
		}
		return realised;
	}

	/**
	 * This method performs the morphology for verbs.
	 * Based in part on the same method in the english rules
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	@Override
	public NLGElement doVerbMorphology(InflectedWordElement element,
			WordElement baseWord) {

		String realised = null;
		
		Object numberValue = element.getFeature(Feature.NUMBER);
		// default number is SINGULAR
		NumberAgreement number = NumberAgreement.SINGULAR;
		if (numberValue instanceof NumberAgreement) {
			number = (NumberAgreement) numberValue;
		}
		
		Object personValue = element.getFeature(Feature.PERSON);
		// default person is THIRD
		Person person = Person.THIRD;
		if (personValue instanceof Person) {
			person = (Person) personValue;
		}
		
		Object genderValue = element.getFeature(LexicalFeature.GENDER);
		// default gender is MASCULINE
		Gender gender = Gender.MASCULINE;
		if (genderValue instanceof Gender) {
			gender = (Gender) genderValue;
		}
		
		Object tenseValue = element.getFeature(Feature.TENSE);
		// default tense is PRESENT
		Tense tense = Tense.PRESENT;
		if (tenseValue instanceof Tense) {
			tense = (Tense) tenseValue;
		}
		
		Object formValue = element.getFeature(Feature.FORM);

		// participles that are not directly in a verb phrase
		// get their gender and number like adjectives
		if (formValue == Form.PRESENT_PARTICIPLE || formValue == Form.PAST_PARTICIPLE) {
            // Get gender and number from parent or "grandparent" or self, in that order
			NLGElement parent = element.getParent();
			if ( parent != null) {

				boolean aggreement = false;
				Object function = element.getFeature(InternalFeature.DISCOURSE_FUNCTION);
				// used as epithet or as attribute of the subject
				if (!parent.isA(PhraseCategory.VERB_PHRASE) || function == DiscourseFunction.OBJECT) {
					if (!parent.hasFeature(LexicalFeature.GENDER) && parent.getParent() != null) {
						parent = parent.getParent();
					}
					aggreement = true;
				} else {
					// used as attribute of the direct object
					if (function == DiscourseFunction.FRONT_MODIFIER
							|| function == DiscourseFunction.PRE_MODIFIER
							|| function == DiscourseFunction.POST_MODIFIER) {
						List<NLGElement> complements =
							parent.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
						NLGElement directObject = null;
						for (NLGElement complement: complements) {
							if (complement.getFeature(InternalFeature.DISCOURSE_FUNCTION) ==
									DiscourseFunction.OBJECT) {
								directObject = complement;
							}
						}
						if (directObject != null) parent = directObject;
						aggreement = true;
					}
				}
				
				if (aggreement) {
					Object parentGender = parent.getFeature(LexicalFeature.GENDER);
					if (parentGender instanceof Gender) {
						gender = (Gender) parentGender;
					}
					
					Object parentNumber = parent.getFeature(Feature.NUMBER);
					if (parentNumber instanceof NumberAgreement) {
						number = (NumberAgreement) parentNumber;
					}
				}
			}
		}
			
		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord).replace("|", "");
		String originalBaseForm = baseForm;
		String SCVMainVerb = "";
		String SCVPreVerb = "";

        VerbPhraseHelper.GetSeparableCompoundVerbReturn SCV = getSeparableCompoundVerb(element);

		if (SCV.isSCV) {

            // Try to split the preverbs from the main verb
            // E.g.: The past 'kwam <i>vrij</i>' should result in 'komen', by removing 'vrij' from 'vrijkomen'.
            SCVMainVerb = SCV.mainVerb;
            SCVPreVerb = SCV.preVerb;
            baseForm = SCVMainVerb;
            baseWord = element.getLexicon().getWord(baseForm);

            element.setFeature(DutchFeature.PREVERB, SCVPreVerb);
        }

        if (Form.BARE_INFINITIVE.equals(formValue) || Form.INFINITIVE.equals(formValue) ) {
			if (SCV.isSCV)
				realised = originalBaseForm;
			else
				realised = baseForm;
			
		} else if ( Form.PRESENT_PARTICIPLE.equals(formValue)
		         || Form.GERUND.equals(formValue) ) {
			// Reference : section 777 of Grevisse (1993)
			realised = element
					.getFeatureAsString(LexicalFeature.PRESENT_PARTICIPLE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.PRESENT_PARTICIPLE);
			}
			if (realised == null) {
				GetPastRadicalReturn pastRadicalReturn = getPastRadical(element, baseWord, baseForm);
				String radical = pastRadicalReturn.radical;
				realised = radical + "ant";
			}
			// Note : The gender and number features must only be
			// passed to the present participle by the syntax when
			// the present participle is used as an adjective.
			// Otherwise it is immutable.
			if (gender == Gender.FEMININE) realised += "e";
			if (number == NumberAgreement.PLURAL) realised += "s";
			
		} else if (Form.PAST_PARTICIPLE.equals(formValue)) {
			// Reference : section 778 of Grevisse (1993)
			// get or build masculine form
			realised = element
					.getFeatureAsString(LexicalFeature.PAST_PARTICIPLE);

			String SCVbaseForm = baseForm.replaceAll("[|]", "");
			WordElement SCVbaseWord = element.getLexicon().lookupWord(SCVbaseForm, LexicalCategory.VERB);

			if (realised == null && SCVbaseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.PAST_PARTICIPLE);
			}
			
			if (realised == null) {
				realised = buildPastParticipleVerb(element, baseWord, baseForm);
			}
			
			// get or build feminine form
			if (gender == Gender.FEMININE) {
				String feminineForm = element
					.getFeatureAsString(DutchLexicalFeature.FEMININE_PAST_PARTICIPLE);
				if (feminineForm == null && baseWord != null) {
					feminineForm = baseWord
						.getFeatureAsString(DutchLexicalFeature.FEMININE_PAST_PARTICIPLE);
				}
				if (feminineForm == null) realised += "e";
				else realised = feminineForm;
			}
			
			// build plural form
			if (number == NumberAgreement.PLURAL && !realised.endsWith("s")) {
				realised += "s";
			}

		} else if (formValue == Form.SUBJUNCTIVE) {
			// try to get inflected form from user feature or lexicon
			switch ( number ) {
			case SINGULAR: case BOTH:
				switch ( person ) {
				case FIRST:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE1S);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE1S);
					}
					break;
				case SECOND:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE2S);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE2S);
					}
					break;
				case THIRD:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE3S);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE3S);
					}
					break;
				}
				break;
			case PLURAL:
				switch ( person ) {
				case FIRST:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE1P);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE1P);
					}
					break;
				case SECOND:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE2P);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE2P);
					}
					break;
				case THIRD:
					realised = element.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE3P);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.SUBJUNCTIVE3P);
					}
					break;
				}
				break;
			}
			// build inflected form if none was specified by the user or lexicon
			if (realised == null) {
				realised = buildSubjunctiveVerb(baseForm, number, person);
			}

		} else if (tense == null || tense == Tense.PRESENT || formValue == Form.IMPERATIVE) {
			
			if (formValue == Form.IMPERATIVE) {
				switch (number) {
				case  SINGULAR: case BOTH:
					realised = element.getFeatureAsString(DutchLexicalFeature.IMPERATIVE2S);
					if (realised == null && baseWord != null) {
						realised = baseWord.getFeatureAsString(DutchLexicalFeature.IMPERATIVE2S);
					}
					// generally, imperative present 2S = indicative present 1S
					if (realised == null) person = Person.FIRST;
					break;
				case PLURAL:
					switch (person) {
					case FIRST:
						realised = element.getFeatureAsString(DutchLexicalFeature.IMPERATIVE1P);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.IMPERATIVE1P);
						}
						// generally, imperative 1P = indicative 1P
						break;
					default:
						realised = element.getFeatureAsString(DutchLexicalFeature.IMPERATIVE2P);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.IMPERATIVE2P);
						}
						// generally, imperative 2P = indicative 2P
						if (realised == null) person = Person.SECOND;
						break;
					}
					break;
				}
			}
			
			// indicative
			if (realised == null) {
				WordElement mainVerbLookup = null;
				if (SCV.isSCV) {
					mainVerbLookup = element.getLexicon().getWord(baseForm, LexicalCategory.VERB);
				}
				// try to get inflected form from user feature or lexicon
				switch ( number ) {
				case SINGULAR: case BOTH:
					switch ( person ) {
					case FIRST:

						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT1S);
						if (realised == null && baseWord != null && mainVerbLookup != null) {
							realised = mainVerbLookup.getFeatureAsString(DutchLexicalFeature.PRESENT1S);
//							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT1S);
						}
						break;
					case SECOND:
						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT2S);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT2S);
						}
						break;
					case THIRD:
						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT3S);
						if (realised == null && baseWord != null && mainVerbLookup != null) {
							realised = mainVerbLookup.getFeatureAsString(DutchLexicalFeature.PRESENT3S);
//							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT3S);
						}
						break;
					}
					break;
				case PLURAL:
					switch ( person ) {
					case FIRST:
						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT1P);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT1P);
						}
						break;
					case SECOND:
						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT2P);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT2P);
						}
						break;
					case THIRD:
						realised = element.getFeatureAsString(DutchLexicalFeature.PRESENT3P);
						if (realised == null && baseWord != null) {
							realised = baseWord.getFeatureAsString(DutchLexicalFeature.PRESENT3P);
						}
						break;
					}
					break;
				}
				// build inflected form if none was specified by the user or lexicon
				if (realised == null) {
				    if (SCV.isSCV) {
                        realised = buildPresentVerb(SCVMainVerb, number, person);

                    } else {
                        realised = buildPresentVerb(baseForm, number, person);
                    }
				}
			}
		
		} else if (tense == Tense.FUTURE) {

			realised = baseForm;

		} else if (tense == Tense.CONDITIONAL) {

			realised = baseForm;

		} else if (tense == Tense.PAST)  {
            if (realised == null) {
                // try to get inflected form from user feature or lexicon
                switch (number) {
                    case SINGULAR:
                    case BOTH:

                        switch (person) {
                            case FIRST:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST1S);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST1S);
                                }
                                break;
                            case SECOND:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST2S);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST2S);
                                }
                                break;
                            case THIRD:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST3S);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST3S);
                                }
                                break;
                        }
                        break;
                    case PLURAL:
                        switch (person) {
                            case FIRST:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST1P);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST1P);
                                }
                                break;
                            case SECOND:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST2P);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST2P);
                                }
                                break;
                            case THIRD:
                                realised = element.getFeatureAsString(DutchLexicalFeature.PAST3P);
                                if (realised == null && baseWord != null) {
                                    realised = baseWord.getFeatureAsString(DutchLexicalFeature.PAST3P);
                                }
                                break;
                        }
                        break;
                }
            }

			// build inflected form with radical
            if (realised == null) {
                GetPastRadicalReturn pastRadicalReturn = getPastRadical(element, baseWord, baseForm);
                //TODO: check for SCV to prevent "liep wegen"
                String radical = pastRadicalReturn.radical;
                boolean isStrongVerb = pastRadicalReturn.isStrongVerb;
                realised = addPastSuffix(radical, number, isStrongVerb);
            }
			
		} else {
			realised = baseForm;
		}

		realised += getParticle(element);
		StringElement realisedElement = new StringElement(realised, element);
		return realisedElement;
	}

	/**
	 * Gets or builds the radical used for "imparfait" and present participle.
	 * Reference : http://woordenlijst.org/leidraad/11/3
	 * 
	 * @param element
	 * @param baseWord
	 * @param baseForm
	 * @return the past radical
	 */
	protected GetPastRadicalReturn getPastRadical(InflectedWordElement element, WordElement baseWord, String baseForm) {

        Boolean isStrongVerb = true;
		// try to get inflected form from user feature or lexicon
		// otherwise take infinitive (base form)
		String radical = element.getFeatureAsString(LexicalFeature.PAST);

		if (radical != null && radical.contains(" ")) {
			radical = radical.split("\\s+", 2)[0];
		}

        if (radical == null && baseWord != null) {
			radical = baseWord.getFeatureAsString(LexicalFeature.PAST);
		}
        // uses first person plural present radical
		if (radical == null) {
			radical = getBaseRadical(baseForm);
            isStrongVerb = false;
        }

        return new GetPastRadicalReturn(radical, isStrongVerb);
	}
	
	/**
	 * Builds the present form for regular verbs. 
	 * Reference : Mansouri (1996)
	 *
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 * @param person
	 * @return the inflected word.
	 */
	protected String buildPresentVerb(String baseForm, NumberAgreement number,
			Person person) {

        // Get radical and verbEndingCategory.
        GetPresentRadicalReturn multReturns =
                getPresentRadical(baseForm);
        String radical = multReturns.radical;

        String presentVerb = baseForm;

        switch ( number ) {
            case SINGULAR: case BOTH:
                switch ( person ) {
                    case FIRST:
                        presentVerb = radical;
                        break;
                    case SECOND: case THIRD:
                        if (radical.endsWith("t")) {
                            presentVerb = radical;
                        } else {
                            presentVerb = radical + "t";
                        }
                }
                break;
            case PLURAL:
                presentVerb =  baseForm;
        }

        return presentVerb;

	}
	
	/**
	 * Builds the subjunctive present form for regular verbs. 
	 * Reference : Mansouri (1996)
	 *
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 * @param person
	 * @return the inflected word.
	 */
	protected String buildSubjunctiveVerb( String baseForm,
			NumberAgreement number, Person person) {
		Person radicalPerson = person;
		NumberAgreement radicalNumber = number;
		
		// Compared to indicative present, singular persons
		// take the radical of third person plural.
		if (number == SINGULAR) {
			radicalNumber = NumberAgreement.PLURAL;
			radicalPerson = Person.THIRD;
		}
		
		// Get radical.
		GetPresentRadicalReturn multReturns =
				getPresentRadical(baseForm);
		String radical = multReturns.radical;
		
		// Determine suffix.
		String suffix = "";		
		switch ( number ) {
		case SINGULAR: case BOTH:
			switch ( person ) {
			case FIRST: case THIRD:
				suffix = "e";
				break;
			case SECOND:
				suffix = "es";
				break;
			}
			break;
		case PLURAL:
			switch ( person ) {
			case FIRST:
				suffix = "ions";
				break;
			case SECOND:
				suffix = "iez";
				break;
			case THIRD:
				suffix = "ent";
				break;
			}
			break;
		}
		
		return addSuffix(radical, suffix);
	}

	/**
	 * @param baseForm the baseForm
	 * @return the radical used in indicative present simple
	 */
	protected GetPresentRadicalReturn getPresentRadical( String baseForm ) {

	    String radical = getBaseRadical(baseForm);

		// replace 'v' with 'f' and 'z' with 's'
		if (radical.endsWith("v"))
			radical = radical.substring(0, radical.length() - 1) + "f";
		if (radical.endsWith("z"))
			radical = radical.substring(0, radical.length() - 1) + "s";

        int verbEndingCategory = 0;

        return new GetPresentRadicalReturn(radical, verbEndingCategory);

	}
	/**
	 * @param baseForm the baseForm
	 * @return the common radical used as a base for present and past radicals.
	 */
	protected String getBaseRadical(String baseForm) {

		int length = baseForm.length();
		String radical = baseForm;

		if (baseForm.endsWith("en"))
			radical = baseForm.substring(0, length-2);

		// keep the sound consistent
		if (radical.matches("[a-zA-Z]*"
                                + "[b-df-hj-np-tv-zB-DF-HJ-NP-TV-Z]+"
                                + "[aeouAEOU]{1}"
                                + "[b-df-hj-np-tv-zB-DF-HJ-NP-TV-Z]{1}")) {
			// repeat vowel, except i's
			String includingVowel = radical.substring(0, radical.length()-1);
			String vowelToDuplicate = includingVowel.substring(includingVowel.length()-1);
			String lastConsonant = radical.substring(radical.length()-1);

			radical = includingVowel + vowelToDuplicate + lastConsonant;
		}

		// transform radicals ending in dubble consonants
		// (e.g. 'hebben' to 'heb')
		if (radical.charAt(radical.length()-1) == radical.charAt(radical.length()-2)) {
			radical = radical.substring(0, radical.length()-1);
		}

		return radical;
	}

	/**
	 * Class used to get two return values from the getPresentRadical method
	 * @author vaudrypl
	 */
	protected class GetPresentRadicalReturn {
		public final String radical;
		public final int verbEndingCategory;
		
		public GetPresentRadicalReturn(String radical, int verbEndingCategory) {
			this.radical = radical;
			this.verbEndingCategory = verbEndingCategory;
		}
	}

	/**
	 * Class used to get two return values from the getPastRadical method
	 * @author rfdj
	 */
	protected class GetPastRadicalReturn {
		public final String radical;
		public final boolean isStrongVerb;

		public GetPastRadicalReturn(String radical, boolean isStrongVerb) {
			this.radical = radical;
			this.isStrongVerb = isStrongVerb;
		}
	}

	/**
	 * Adds a radical and a suffix applying phonological rules
	 * Reference : sections 760-761 of Grevisse (1993)
	 * 
	 * @param radical
	 * @param suffix
	 * @return resultant form
	 */
	public String addSuffix(String radical, String suffix) {
		int length = radical.length();
		// change "c" to "ç" and "g" to "ge" before "a" and "o";
		if (suffix.matches(a_o_regex)) {
			if (radical.endsWith("c")) {
				radical = radical.substring(0, length-1) + "ç";
			} else if (radical.endsWith("g")) {
				radical += "e";
			}
		}
		// if suffix begins with mute "e"
		if (!suffix.equals("ez") && suffix.startsWith("e")) {
			// change "y" to "i" if not in front of "e"
			if (!radical.endsWith("ey") && radical.endsWith("y")) {
				radical = radical.substring(0,length-1) + "i";
			}
			// change "e" and "é" to "è" in last sillable of radical
			char penultimate = radical.charAt(length-2);
			if (penultimate == 'e' || penultimate == 'é') {
				radical = radical.substring(0,length-2) + "è"
						+ radical.substring(length-1);
			}
		}
		return radical + suffix;
	}

	/**
	 * Builds the simple future form for all verbs. 
	 * Reference : Mansouri (1996)
	 *
	 * @param radical
	 *            the future radical of the word.
	 * @param number
	 * @param person
	 * @return the inflected word.
	 */
	protected String buildFutureVerb(String radical, NumberAgreement number,
			Person person) {


		
		return radical;
	}

	/**
	 * Builds the conditional present form for all verbs. 
	 * Reference : Mansouri (1996)
	 *
	 * @param radical
	 *            the future radical of the word.
	 * @param number
	 * @param person
	 * @return the inflected word.
	 */
	protected String buildConditionalVerb(String radical, NumberAgreement number,
			Person person) {
		String suffix = "";
		
		switch ( number ) {
		case SINGULAR: case BOTH:
			switch ( person ) {
			case FIRST:
				suffix = "ais";
				break;
			case SECOND:
				suffix = "ais";
				break;
			case THIRD:
				suffix = "ait";
				break;
			}
			break;
		case PLURAL:
			switch ( person ) {
			case FIRST:
				suffix = "ions";
				break;
			case SECOND:
				suffix = "iez";
				break;
			case THIRD:
				suffix = "aient";
				break;
			}
			break;
		}
		
		return radical + suffix;
	}

	/**
	 * Adds the "imparfait" and "conditionel" suffix to a verb radical. 
	 * Reference : Mansouri (1996)
	 *
	 * @param radical the past radical of the word.
	 * @param number the <code>NumberAgreement</code>
	 * @param isStrongVerb is the verb a strong verb?
	 * @return the inflected word.
	 */
	protected String addPastSuffix(String radical, NumberAgreement number,
                                   Boolean isStrongVerb) {
		String suffix = "";

        if (isStrongVerb) {
			switch ( number ) {
				case SINGULAR:
					return radical;
				case PLURAL:
					if (radical.endsWith("e"))
						return radical + "n";
					else {
						// remove double vowel
						int length = radical.length();
						String regexCVVC = ".*" // anything
								+ "([aeiouAEIOU])\\1" // two repeated vowels
								+ "[b-df-hj-npqstv-xzB-DF-HJ-NPQSTV-XZ]"; // not 'r', because that is handled with a suffix

						// replace trailing 'f' with 'v' and 's' with 'z'
						if (radical.matches(".*([aeiouyAEIOUY]|(ij)|(ei)|(ui)|(au)|(ou))[fsFS]")) {
							if (radical.endsWith("f")) {
								radical = radical.substring(0, length - 1) + "v";
							} else if (radical.endsWith("s")) {
								radical = radical.substring(0, length - 1) + "z";
							}
						}

						if (radical.matches(regexCVVC)) {
							String lastConsonant = radical.substring(length - 1);
							String front = radical.substring(0, length - 2);
							radical = front + lastConsonant;
						}
						return radical + "en";
					}
				default:
					return radical;
			}
		}

		// radicals ending in an unvoiced consonant get the suffix 'te' or 'ten',
		// radicals ending in a voiced consonant get the suffix 'de' or 'den'.
        // Reference: https://onzetaal.nl/taaladvies/t-kofschip/
        String[] unvoicedConsonants = {"t", "k", "f", "s", "ch", "p", "x", "sj", "c"};
        boolean hasUnvoicedConsonant = false;

		for (String endSound : unvoicedConsonants) {
		    if (radical.endsWith(endSound)) {
                hasUnvoicedConsonant = true;
            }
        }

        // replace 'v' with 'f' and 'z' with 's'
        if (radical.endsWith("v"))
            radical = radical.substring(0, radical.length() - 1) + "f";
        if (radical.endsWith("z"))
            radical = radical.substring(0, radical.length() - 1) + "s";

        switch ( number ) {
            case SINGULAR: case BOTH:
                if (hasUnvoicedConsonant) {
                    suffix = "te";
                } else {
                    suffix = "de";
                }
                break;
            case PLURAL:
                if (hasUnvoicedConsonant) {
                    suffix = "ten";
                } else {
                    suffix = "den";
                }
                break;
        }
        return radical + suffix;
	}

	/**
	 * Builds the past participle form for regular verbs. 
	 * Reference:
	 *
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildPastParticipleVerb(InflectedWordElement element, WordElement baseWord, String baseForm) {
		String realised = baseForm;
        VerbPhraseHelper.GetSeparableCompoundVerbReturn scv = getSeparableCompoundVerb(element);
        GetPresentRadicalReturn presentRadicalReturn;

        if (scv.isSCV) {
            presentRadicalReturn = getPresentRadical(scv.mainVerb);
        } else {
            presentRadicalReturn = getPresentRadical(baseForm);
        }

		String radical = presentRadicalReturn.radical;

        // radicals ending in an unvoiced consonant get the suffix 'te' or 'ten',
        // radicals ending in a voiced consonant get the suffix 'de' or 'den'.
        // Reference: https://onzetaal.nl/taaladvies/t-kofschip/
        String[] unvoicedConsonants = {"k", "f", "s", "ch", "p", "x", "sj", "c"}; // "t" is exlcuded, no need for suffix
        boolean hasUnvoicedConsonant = false;

        for (String endSound : unvoicedConsonants) {
            if (radical.endsWith(endSound)) {
                hasUnvoicedConsonant = true;
            }
        }

        String preverb = getPreverb(element);


        // replace 'v' with 'f' and 'z' with 's'
        if (radical.endsWith("v"))
            radical = radical.substring(0, radical.length() - 1) + "f";
        if (radical.endsWith("z"))
            radical = radical.substring(0, radical.length() - 1) + "s";

        String suffix;
        if (radical.endsWith("t") || radical.endsWith("d")) {
            // don't add an extra 't' or 'd' (instead of removing the last 't'/'d' from the radical and then adding a suffix)
            suffix = "";
        } else if (hasUnvoicedConsonant) {
            suffix = "t";
        } else {
            suffix = "d";
        }


        realised = preverb + "ge" + radical + suffix;

        return realised;
	}

    /**
     * This method retrieves the preverb from the feature
     *
     * @param element the verb
     * @return the preverb string
     */
    protected String getPreverb(InflectedWordElement element) {

        String preverb;

        preverb = element.getFeatureAsString(DutchFeature.PREVERB);

        NLGElement parent = element.getParent();
        if (preverb == null && parent != null) {
            preverb = parent.getFeatureAsString(DutchFeature.PREVERB);
        }
        if (preverb == null) preverb = "";

        return preverb;
    }

    /**
	 * This method performs the morphology for adverbs.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doAdverbMorphology(InflectedWordElement element,
			WordElement baseWord) {

		String realised = null;

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		// Comparatives and superlatives are mainly treated by syntax
		// in French. Only exceptions, provided by the lexicon, are
		// treated by morphology.
		if (element.getFeatureAsBoolean(Feature.IS_COMPARATIVE).booleanValue()) {
			realised = element.getFeatureAsString(LexicalFeature.COMPARATIVE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.COMPARATIVE);
			}
			if (realised == null) realised = baseForm;
		} else {
			realised = baseForm;
		}

		realised += getParticle(element);
		StringElement realisedElement = new StringElement(realised, element);
		return realisedElement;
	}

	/**
	 * This method performs the morphology for pronouns.
	 * Reference : sections 633-634 of Grevisse (1993)
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doPronounMorphology(InflectedWordElement element) {
		String realised = element.getBaseForm();

        Object type = element.getFeature(DutchLexicalFeature.PRONOUN_TYPE);
        // inflect only personal pronouns
		if (type == PronounType.PERSONAL
			&& element.getFeature(InternalFeature.DISCOURSE_FUNCTION)
				!= DiscourseFunction.COMPLEMENT) {
			
			// this will contain the features we want the pronoun to have
			Map<String, Object> pronounFeatures = new HashMap<String, Object>();
	
			pronounFeatures.put(DutchLexicalFeature.PRONOUN_TYPE, type);
			
			boolean passive = element.getFeatureAsBoolean(Feature.PASSIVE);
			boolean reflexive = element.getFeatureAsBoolean(LexicalFeature.REFLEXIVE);
			NLGElement parent = element.getParent();
			
			Object gender = element.getFeature(LexicalFeature.GENDER);
			if (!(gender instanceof Gender) || gender == Gender.NEUTER) gender = Gender.MASCULINE;
			
			Object person = element.getFeature(Feature.PERSON);
			Object number = element.getFeature(Feature.NUMBER);

			// agree the reflexive pronoun with the subject
			if (reflexive && parent != null) {
				NLGElement verbPhrase = null;
				NLGElement grandParent =  parent.getParent();
				NLGElement grandGrandParent = null;

				if (grandParent != null && grandParent.getCategory().equalTo(PhraseCategory.VERB_PHRASE)) {
					verbPhrase = grandParent;
					grandGrandParent = grandParent.getParent();
				}
				if (grandGrandParent != null && grandGrandParent.getCategory().equalTo(PhraseCategory.VERB_PHRASE)) {
					verbPhrase = grandGrandParent;
				}
				if (verbPhrase != null) {
                    person = verbPhrase.getFeature(Feature.PERSON);
                    number = verbPhrase.getFeature(Feature.NUMBER);

                    // If the verb phrase is in imperative form,
                    // the reflexive pronoun can only be in 2S, 1P or 2P.
                    if (verbPhrase.getFeature(Feature.FORM) == Form.IMPERATIVE) {
                        if (number == NumberAgreement.PLURAL) {
                            if (person != Person.FIRST && person != Person.SECOND) {
                                person = Person.SECOND;
                            }
                        } else {
                            person = Person.SECOND;
                        }
                    }
                }
			}
			if (!(person instanceof Person)) person = Person.THIRD;
			if (!(number instanceof NumberAgreement)) number = SINGULAR;

			Object function = element.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			// If the pronoun is the head of a noun phrase,
			// take the discourse function of this noun phrase
			if (function == DiscourseFunction.SUBJECT && parent != null
					&& parent.isA(PhraseCategory.NOUN_PHRASE)) {
				function = parent.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			}
			if (!(function instanceof DiscourseFunction)) function = DiscourseFunction.SUBJECT;
			if (passive) {
				if (function == DiscourseFunction.SUBJECT) function = DiscourseFunction.OBJECT;
				else if (function == DiscourseFunction.OBJECT) function = DiscourseFunction.SUBJECT;
			}
			
			if (function != DiscourseFunction.OBJECT && function != DiscourseFunction.INDIRECT_OBJECT) {
				reflexive = false;
			}
			
			pronounFeatures.put(Feature.PERSON, person);
			pronounFeatures.put(Feature.NUMBER, number);
            if (reflexive) {
                pronounFeatures.put(LexicalFeature.REFLEXIVE, true);
                pronounFeatures.put(InternalFeature.DISCOURSE_FUNCTION, function);

                Lexicon lexicon = element.getLexicon();
                // search the lexicon for the right pronoun
                WordElement proElement =
                        lexicon.getWord(LexicalCategory.PRONOUN, pronounFeatures);

                // if the right pronoun is not found in the lexicon,
                // leave the original pronoun
                if (proElement != null) {
                    element = new InflectedWordElement(proElement);
                    realised = proElement.getBaseForm();
                }
            }

		// Agreement of relative pronouns with parent noun phrase.
		} else if (type == PronounType.RELATIVE) {
			// Get parent clause.
			NLGElement antecedent = element.getParent();
			while (antecedent != null
					&& !antecedent.isA(PhraseCategory.CLAUSE)) {
				antecedent = antecedent.getParent();
			}

			if (antecedent != null) {
				// Get parent noun phrase of parent clause.
				antecedent = antecedent.getParent();
				if (antecedent != null) {
					boolean feminine = antecedent.getFeature(LexicalFeature.GENDER)
							== Gender.FEMININE;
					boolean plural = antecedent.getFeature(Feature.NUMBER)
							== NumberAgreement.PLURAL;
					
					// Lookup lexical entry for appropriate form.
					// If the corresponding form is not found :
					// Feminine plural defaults to masculine plural.
					// Feminine singular and masculine plural default
					// to masculine singular.
					String feature = null;
					if (feminine && plural) {
						feature = element.getFeatureAsString(
								DutchLexicalFeature.FEMININE_PLURAL);
					} else if (feminine) {
						feature = element.getFeatureAsString(
								DutchLexicalFeature.FEMININE_SINGULAR);
					}
					
					if (plural && feature == null ) {
						feature = element.getFeatureAsString(
								LexicalFeature.PLURAL);
					}
					
					if (feature != null) realised = feature;
				}
			}
		}
	
		realised += getParticle(element);
		StringElement realisedElement = new StringElement(realised, element);

		return realisedElement;
	}

}
