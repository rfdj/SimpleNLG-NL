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
package simplenlg.syntax.dutch;

import simplenlg.features.*;
import simplenlg.features.dutch.DutchFeature;
import simplenlg.features.dutch.DutchInternalFeature;
import simplenlg.features.dutch.DutchLexicalFeature;
import simplenlg.features.french.FrenchInternalFeature;
import simplenlg.features.french.FrenchLexicalFeature;
import simplenlg.features.french.PronounType;
import simplenlg.framework.*;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.PPPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class contains static methods to help the syntax processor realise verb
 * phrases for French.
 * 
 * Reference :
 * 
 * Grevisse, Maurice (1993). Le bon usage, grammaire française,
 * 12e édition refondue par André Goosse, 8e tirage, Éditions Duculot,
 * Louvain-la-Neuve, Belgique.
 * 
 * @author vaudrypl, rfdj
 */
public class VerbPhraseHelper extends simplenlg.syntax.english.nonstatic.VerbPhraseHelper {

	/**
	 * Class used to return all parts of a verb phrase separately
	 */
	class RealiseVerbPhraseReturn {

		NLGElement mainVerb,
				preObject, object, postObject,
				preIndirectObject, indirectObject, postIndirectObject;
		List<NLGElement> auxVerbs, complements;

		RealiseVerbPhraseReturn(NLGElement mainVerb, List<NLGElement> auxVerbs,
								NLGElement preObject, NLGElement object, NLGElement postObject,
								NLGElement preIndirectObject, NLGElement indirectObject, NLGElement postIndirectObject,
								List<NLGElement> complements) {
			this.mainVerb = mainVerb;
			this.auxVerbs = auxVerbs;
			this.preObject = preObject;
			this.object = object;
			this.postObject = postObject;
			this.preIndirectObject = preIndirectObject;
			this.indirectObject = indirectObject;
			this.postIndirectObject = postIndirectObject;
			this.complements = complements;

		}
	}

	/**
	 * The main method for realising verb phrases.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> to be realised.
	 * @return the realised <code>NLGElement</code>.
	 */
	@Override
	public NLGElement realise(PhraseElement phrase) {
		ListElement realisedElement = null;
		Stack<NLGElement> vgComponents;
		Stack<NLGElement> mainVerbRealisation = new Stack<>();
		Stack<NLGElement> auxiliaryRealisation = new Stack<>();

		if (phrase != null) {

			Boolean isInSubordinatedClause;

			if (phrase.getFeatureAsBoolean(DutchFeature.TE_INFINITIVE)) {
				phrase.setFeature(Feature.FORM, Form.INFINITIVE);
			}

			vgComponents = createVerbGroup(phrase);
			splitVerbGroup(vgComponents, mainVerbRealisation,
					auxiliaryRealisation);

			// vaudrypl added phrase argument to ListElement constructor
			// to copy all features from the PhraseElement
			realisedElement = new ListElement(phrase);

			if (!mainVerbRealisation.isEmpty()) {

				NLGElement parent = phrase.getParent();
				NLGElement verb = mainVerbRealisation.peek();
				Object verbForm = verb.getFeature(Feature.FORM);

				// Complementiser
				if (phrase.hasFeature(DutchFeature.TE_INFINITIVE) && phrase.hasFeature(Feature.COMPLEMENTISER)) {
					realisedElement.addComponent(phrase.getFeatureAsElement(Feature.COMPLEMENTISER));
				}

				// Reflexive pronouns, which are set as complements
				if (phrase.getFeatureAsBoolean(Feature.PERFECT)
						|| parent.getFeatureAsBoolean(Feature.PERFECT)
						|| Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))
						|| Tense.FUTURE.equals(parent.getFeature(Feature.TENSE))
						|| verbForm == Form.INFINITIVE
						|| parent.hasFeature(DutchFeature.RELATIVE_PHRASE)) {
					realiseReflexivePronouns(phrase, realisedElement);
				}

				// Complements
				if (phrase.hasFeature(DutchFeature.TE_INFINITIVE)) {
					realiseComplements(phrase, realisedElement);
				}

				if (parent.hasFeature(DutchFeature.RELATIVE_PHRASE)) {

					// Complements
					List<NLGElement> complements = phrase.getHead().getFeatureAsElementList(InternalFeature.COMPLEMENTS);
					List<NLGElement> toRemove = new ArrayList<>();

					for (NLGElement complement : complements) {

						// Add premodifiers
						List<NLGElement> preModifiers = complement.getFeatureAsElementList(InternalFeature.PREMODIFIERS);
						List<NLGElement> preModToRemove = new ArrayList<>();

						for (NLGElement preMod : preModifiers) {
							realisedElement.addComponent(preMod.realiseSyntax());
							preModToRemove.add(preMod);
						}
						preModifiers.removeAll(preModToRemove);
						complement.setFeature(InternalFeature.PREMODIFIERS, preModifiers);

						// Add subcomplements
						realiseObjectsOfElement(realisedElement, complement);

						toRemove.add(complement);
					}

					if (complements.removeAll(toRemove)) {
						phrase.setFeature(InternalFeature.COMPLEMENTS, complements);
					}
				}

				phrase.getPhraseHelper().realiseList(realisedElement, phrase
						.getPreModifiers(), DiscourseFunction.PRE_MODIFIER);

				// If this is a main verb phrase in a subordinate clause:
				//     Realise objects of sibling first
				isInSubordinatedClause = ClauseStatus.SUBORDINATE.equals(parent.getFeature(InternalFeature.CLAUSE_STATUS))
						&& hasSubordinateComplementiser(parent);

				if (isInSubordinatedClause) {
					List<NLGElement> complements = phrase.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
					List<NLGElement> compsToRemove = new ArrayList<>();

					for (NLGElement complement : complements) {
						if (complement.isA(PhraseCategory.NOUN_PHRASE)
								|| complement.isA(PhraseCategory.CANNED_TEXT)) {


							if (!complement.hasFeature(InternalFeature.COMPLEMENTS)) {
								if (complement.getFeatureAsBoolean(LexicalFeature.REFLEXIVE)) {
									realisedElement.addComponent(complement.realiseSyntax());
								}
							}

							// Realise subcomplements
							realiseObjectsOfElement(realisedElement, complement);

							Object discourseFunction = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
							if (discourseFunction == DiscourseFunction.OBJECT) {
								realisedElement.addComponent(complement.realiseSyntax());
							}

							compsToRemove.add(complement);
						}
					}
					complements.removeAll(compsToRemove);
					phrase.setFeature(InternalFeature.COMPLEMENTS, complements);
				}

				// If we should realise auxiliary verbs
				if ((!phrase.hasFeature(InternalFeature.REALISE_AUXILIARY)
						|| phrase.getFeatureAsBoolean(InternalFeature.REALISE_AUXILIARY))
						&& !auxiliaryRealisation.isEmpty()) {

					realiseAuxiliaries(realisedElement,
							auxiliaryRealisation);

					// Realise direct objects
					if ((Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))
								|| Tense.CONDITIONAL.equals(phrase.getFeature(Feature.TENSE))
								|| phrase.getFeatureAsBoolean(Feature.PERFECT)
							)
							&& !phrase.getFeatureAsBoolean(Feature.PASSIVE)) {

						List<NLGElement> complements = phrase.getFeatureAsElementList(InternalFeature.COMPLEMENTS);

						for (NLGElement complement : complements) {

							Object discourseFunction = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
							if (discourseFunction == DiscourseFunction.OBJECT) {
								realisedElement.addComponent(complement.realiseSyntax());
							}
						}
					}
				}

				realiseMainVerb(phrase, mainVerbRealisation,
						realisedElement);

				// `CLAUSE-INITIAL FIELD'
				// Interrogative sentences realise the subjects between the verb and its objects
				Object interrogativeType = parent.getFeature(Feature.INTERROGATIVE_TYPE);
				if (interrogativeType == InterrogativeType.WHY
						|| interrogativeType == InterrogativeType.WHO_INDIRECT_OBJECT
						|| interrogativeType == InterrogativeType.WHERE) {

					List<NLGElement> subjects = parent.getFeatureAsElementList(InternalFeature.SUBJECTS);

					for (NLGElement subject : subjects) {
						realisedElement.addComponent(subject.realiseSyntax());
					}
				}
			}

			// 'POSTVERBAL FIELD'
			phrase.getPhraseHelper().realiseList(realisedElement, phrase
					.getPostModifiers(), DiscourseFunction.POST_MODIFIER);

			if (!phrase.hasFeature(DutchFeature.TE_INFINITIVE)) {
				realiseComplements(phrase, realisedElement);
			}

		}

		return realisedElement;
	}

	/**
	 * Checks to see if the base form of the word is copular.
	 * 
	 * @param element
	 *            the element to be checked
	 * @return <code>true</code> if the element is copular.
	 */
	@Override
	public boolean isCopular(NLGElement element) {
		if (element != null) {
			return element.getFeatureAsBoolean(FrenchLexicalFeature.COPULAR);
		} else return true;
	}

	/**
	 * Checks to see if the clause is a subordinate clause based on its complementiser.
	 *
	 * The set of complementisers to check against may not be complete.
	 * References: http://ans.ruhosting.nl/e-ans/10/03/body.html
	 * 			   https://www.taal-oefenen.nl/instruction/taal/woordsoorten/voegwoorden/onderschikkende-voegwoorden
	 */
	private boolean hasSubordinateComplementiser(NLGElement phrase) {
		boolean hasSubordComp = false;
		String phraseComplementiser = phrase.getFeatureAsString(Feature.COMPLEMENTISER);
		String[] complementisers = {"waardoor", "ofschoon", "dat", "of", "omdat", "doordat", "aangezien",
				"daar", "vermits", "door", "zodat", "dat", "dan dat", "om",
				"opdat", "teneinde", "als", "wanneer", "indien", "ingeval", "zo",
				"mits", "tenzij", "tenware", "zonder dat", "in plaats van dat",
				"behalve dat", "uitgezonderd dat", "in zover", "in zoverre",
				"voor zover", "voor zoverre", "laat staan dat", "naargelang",
				"naarmate", "hoe meer", "des te meer", "naar"};

		for (String complementiser : complementisers) {
			if (complementiser.equals(phraseComplementiser)) {
				hasSubordComp = true;
			}
		}
		return hasSubordComp;
	}

	/**
	 * Splits the stack of verb components into two sections. One being the verb
	 * associated with the main verb group, the other being associated with the
	 * auxiliary verb group.
	 * 
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @param mainVerbRealisation
	 *            the main group of verbs.
	 * @param auxiliaryRealisation
	 *            the auxiliary group of verbs.
	 */
	@Override
	protected void splitVerbGroup(Stack<NLGElement> vgComponents,
			Stack<NLGElement> mainVerbRealisation,
			Stack<NLGElement> auxiliaryRealisation) {

		boolean mainVerbSeen = false;
		boolean cliticsSeen = false;

		for (NLGElement word : vgComponents) {
			if (!mainVerbSeen) {
				mainVerbRealisation.push(word);
//				if (!word.equals("pas") &&
				if (!word.isA(LexicalCategory.ADVERB) &&
						!word.getFeatureAsBoolean(FrenchInternalFeature.CLITIC)) {
					mainVerbSeen = true;
				}
			} else if (!cliticsSeen) {
//				if (!word.equals("ne") &&
				if (!"ne".equals(word.getFeatureAsString(LexicalFeature.BASE_FORM)) &&
						!word.getFeatureAsBoolean(FrenchInternalFeature.CLITIC)) {
					cliticsSeen = true;
					auxiliaryRealisation.push(word);
				} else {
					mainVerbRealisation.push(word);
				}
			} else {
				auxiliaryRealisation.push(word);
			}
		}

	}

	/**
	 * Creates a stack of verbs for the verb phrase. Additional auxiliary verbs
	 * are added as required based on the features of the verb phrase.
	 * 
	 * Based on English method of the same name.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @return the verb group as a <code>Stack</code> of <code>NLGElement</code>
	 *         s.
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected Stack<NLGElement> createVerbGroup(PhraseElement phrase) {

		String actualModal = null;
		Object formValue = phrase.getFeature(Feature.FORM);
		Tense tenseValue = phrase.getTense();
		String modal = phrase.getFeatureAsString(Feature.MODAL);
		boolean modalPast = false;
		Stack<NLGElement> vgComponents = new Stack<NLGElement>();
		boolean interrogative = phrase.hasFeature(Feature.INTERROGATIVE_TYPE);
		boolean progressive = phrase.getFeatureAsBoolean(Feature.PROGRESSIVE);
		boolean perfect = phrase.getFeatureAsBoolean(Feature.PERFECT);
		boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);
		boolean negative = phrase.getFeatureAsBoolean(Feature.NEGATED);
		boolean te_infinitive = phrase.getFeatureAsBoolean(DutchFeature.TE_INFINITIVE);
		NLGFactory factory = phrase.getFactory();
		boolean insertClitics = true;

		// With "si" as complemetiser, change future to present,
		// conditional present to "imparfait"
		// and conditional past to "plus-que-parfait".
		NLGElement parent = phrase.getParent();
		if ( parent != null
				&& parent.getFeature(InternalFeature.CLAUSE_STATUS)
					== ClauseStatus.SUBORDINATE
				&& !parent.getFeatureAsBoolean(Feature.SUPRESSED_COMPLEMENTISER) ) {

			NLGElement complementiser = factory.createWord(
					parent.getFeature(Feature.COMPLEMENTISER), LexicalCategory.COMPLEMENTISER);
			NLGElement si = factory.createWord("si", LexicalCategory.COMPLEMENTISER);
			if (complementiser == si) {
				if (tenseValue == Tense.FUTURE) tenseValue = Tense.PRESENT;
				else if (tenseValue == Tense.CONDITIONAL) {
					tenseValue = Tense.PAST;
					if (!perfect) progressive = true;
				}
			}
		}
		
		WordElement modalWord =	null;
		boolean cliticRising = false;
		if (modal != null) {
			modalWord = phrase.getLexicon().lookupWord(modal, LexicalCategory.VERB);
			cliticRising = modalWord.getFeatureAsBoolean(FrenchLexicalFeature.CLITIC_RISING);
		}

		if (te_infinitive) {
			phrase.setFeature(Feature.FORM, Form.INFINITIVE);
		}

		if (Form.INFINITIVE.equals(formValue)) {
			actualModal = null;
		
		} else if (formValue == null || formValue == Form.NORMAL
				|| (formValue == Form.IMPERATIVE && cliticRising)) {
			if (modal != null) {
				actualModal = modal;

				if (Tense.PAST.equals(tenseValue)) {
					modalPast = true;
				}
			}
		}

		if (actualModal == null) modalWord = null;

		NLGElement frontVG = grabHeadVerb(phrase, tenseValue, modal != null);
		if (frontVG == null) return vgComponents;
		frontVG.setFeature(Feature.TENSE, tenseValue);

		if (passive) {
			frontVG = addPassiveAuxiliary(frontVG, vgComponents, phrase);
			frontVG.setFeature(Feature.TENSE, tenseValue);
		}
		
		// progressive not perfect past = "imparfait"
		// the rest is with "être en train de" auxiliary
		if (progressive	&& (tenseValue != Tense.PAST
					|| perfect || actualModal != null
					|| formValue == Form.SUBJUNCTIVE)) {
			NLGElement newFront =
					addProgressiveAuxiliary(frontVG, vgComponents, factory, phrase);
			if (frontVG != newFront) {
				frontVG = newFront;
				frontVG.setFeature(Feature.TENSE, tenseValue);
				insertClitics = false;
			}
		}

		// "hebben" or "zijn" auxiliary
		AddAuxiliaryReturn auxReturn = null;
        if (phrase.getFeatureAsBoolean(Feature.PERFECT)) {
            auxReturn = addAuxiliary(frontVG, vgComponents, modal, tenseValue, phrase);
            frontVG = auxReturn.newFront;
        }

        // "zullen" for future tense
		AddAuxiliaryReturn futureReturn = null;
        if (Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))) {
			futureReturn = addZullen(frontVG, vgComponents, modal, tenseValue, phrase);
			frontVG = futureReturn.newFront;
        }

        // past tense "zullen" for conditional tense
		AddAuxiliaryReturn conditionalReturn = null;
        if (Tense.CONDITIONAL.equals(phrase.getFeature(Feature.TENSE))) {
			conditionalReturn = addZullen(frontVG, vgComponents, modal, tenseValue, phrase);
			frontVG = conditionalReturn.newFront;
        }
		
		frontVG = pushIfModal(actualModal != null, phrase, frontVG,	vgComponents);
		// insert clitics here if imperative and not negative
		// or if there is a modal verb without clitic rising
		NLGElement cliticDirectObject = null;
		if (insertClitics) {
			if (!negative && formValue == Form.IMPERATIVE) {
				cliticDirectObject = insertCliticComplementPronouns(phrase, vgComponents);
				insertClitics = false;
			} else if (frontVG == null) {
				if (!cliticRising) {
					cliticDirectObject = insertCliticComplementPronouns(phrase, vgComponents);
					insertClitics = false;
				}
			}
		}

		createNiet(phrase, vgComponents, frontVG, modal != null);

		pushModal(modalWord, phrase, vgComponents);

		if (frontVG != null) {
			pushFrontVerb(phrase, vgComponents, frontVG, formValue,
					interrogative);
			frontVG.setFeature(Feature.FORM, formValue);
		}
		// default place for inserting clitic complement pronouns
		if (insertClitics) {
			cliticDirectObject = insertCliticComplementPronouns(phrase, vgComponents);
			insertClitics = false;
		}
		if (te_infinitive) {
			createTe(phrase, vgComponents);
		}

		if (auxReturn != null) {
			// Check if verb phrase is part of a relative clause with
			// the relative phrase being a direct object. In that case,
			// Make object agreement with the parent NP of the clause.
			if (!passive && parent != null && parent.hasRelativePhrase(DiscourseFunction.OBJECT)) {
				NLGElement grandParent = parent.getParent();
				if (grandParent instanceof NPPhraseSpec) {
					cliticDirectObject = grandParent;
				}
			}
			makePastParticipleWithAvoirAgreement(auxReturn.pastParticipleAvoir, cliticDirectObject);
		}

		return vgComponents;
	}

	/**
	 * Transfers the agreement features from the direct object to
	 * the past participle with auxiliary "avoir" if the direct object
	 * is placed before the past participle. (For now, this only means
	 * if there is a direct object clitic pronoun. Eventually it will
	 * include checks for relative clause, etc.)
	 * 
	 * @param pastParticiple
	 * @param cliticDirectObject
	 */
	protected void makePastParticipleWithAvoirAgreement(
			NLGElement pastParticiple, NLGElement cliticDirectObject) {
		
		if (pastParticiple != null && cliticDirectObject != null) {
			Object gender = cliticDirectObject.getFeature(LexicalFeature.GENDER);
			if (gender instanceof Gender) {
				pastParticiple.setFeature(LexicalFeature.GENDER, gender);
			}
			
			Object number = cliticDirectObject.getFeature(Feature.NUMBER);
			if (number instanceof NumberAgreement) {
				pastParticiple.setFeature(Feature.NUMBER, number);
			}
		}
	}

	/**
	 * Determine wich pronominal complements are clitics and inserts
	 * them in the verb group components.
	 * Reference : section 657 of Grevisse (1993)
	 * 
	 * @param phrase
	 * @param vgComponents
	 */
	protected NLGElement insertCliticComplementPronouns(PhraseElement phrase,
			Stack<NLGElement> vgComponents) {
		List<NLGElement> complements =
			phrase.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
		boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);
		NLGElement pronounEn = null, pronounY = null,
					directObject = null, indirectObject = null;

		// identify clitic candidates
		for (NLGElement complement : complements) {
			if (complement != null && !complement.getFeatureAsBoolean(Feature.ELIDED)) {
				Object discourseValue = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
				if (!(discourseValue instanceof DiscourseFunction)) {
					discourseValue = DiscourseFunction.COMPLEMENT;
				}
				// Realise complement only if it is not the relative phrase of
				// the parent clause and not a phrase with the same function in case
				// of a direct or indirect object.
				NLGElement parent = phrase.getParent();
				if ( parent == null ||
						(complement != parent.getFeatureAsElement(DutchFeature.RELATIVE_PHRASE) &&
							(discourseValue == DiscourseFunction.COMPLEMENT ||
								!parent.hasRelativePhrase((DiscourseFunction) discourseValue)))) {
					NLGElement head = null;
					Object type = null;
					
					// if a complement is or contains a pronoun, or will be pronominalised
					if (complement.isA(LexicalCategory.PRONOUN)) {
						head = complement;
					} else if (complement instanceof NPPhraseSpec
							&& ((NPPhraseSpec)complement).getHead() != null
							&& ((NPPhraseSpec)complement).getHead().isA(LexicalCategory.PRONOUN)) {
						head = ((NPPhraseSpec)complement).getHead();
					}
					else if (complement.getFeatureAsBoolean(Feature.PRONOMINAL)) {
						type = PronounType.PERSONAL;
					}
					
					if (head != null) {
						type = head.getFeature(FrenchLexicalFeature.PRONOUN_TYPE);
					}
					
					if (type != null) {
						complement.setFeature(FrenchInternalFeature.CLITIC, false);
						if (type == PronounType.SPECIAL_PERSONAL) {
							String baseForm = ((WordElement)head).getBaseForm();
							if (baseForm.equals("en")) {
								pronounEn = complement;
							}
							else if (baseForm.equals("y")) {
								pronounY = complement;
							}
						} else if (type == PronounType.PERSONAL) {
							Object discourseFunction = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
							if (discourseFunction == DiscourseFunction.OBJECT && !passive) {
								directObject = complement;
							} else if (discourseFunction == DiscourseFunction.INDIRECT_OBJECT) {
								indirectObject = complement;
							}
						}
					}
				}
			}
		}
		
		// place clitics in order :
		// (indirect object) (direct object) y en
		
		if (pronounEn != null) {
			pronounEn.setFeature(FrenchInternalFeature.CLITIC, true);
			vgComponents.push(pronounEn);
		}
		
		if (pronounY != null) {
			pronounY.setFeature(FrenchInternalFeature.CLITIC, true);
			vgComponents.push(pronounY);
		}
		
		if (directObject != null) {
			directObject.setFeature(FrenchInternalFeature.CLITIC, true);
			vgComponents.push(directObject);
		}
		
		// the indirect object is clitic if there's no direct object
		// or if it is third person and not reflexive
		if ( indirectObject != null && (directObject == null || 
				((directObject.getFeature(Feature.PERSON) == Person.THIRD
						|| directObject.getFeature(Feature.PERSON) == null)
					&& !directObject.getFeatureAsBoolean(LexicalFeature.REFLEXIVE) )) ) {
			
			indirectObject.setFeature(FrenchInternalFeature.CLITIC, true);

			Object person = indirectObject.getFeature(Feature.PERSON);
			boolean luiLeurPronoun = (person == null || person == Person.THIRD);
			// place indirect object after direct object if indirect object is "lui" or "leur"
			if (directObject != null && luiLeurPronoun) vgComponents.pop();
			vgComponents.push(indirectObject);
			if (directObject != null && luiLeurPronoun) {
				vgComponents.push(directObject);
			}
		}
		
		// return the direct object for use with past participle agreement with auxiliary "avoir"
		return directObject;
	}

	/**
	 * Checks to see if the phrase is in infinitive form. If it is then
	 * no morphology is done on the main verb.
	 * 
	 * Based on English method checkImperativeInfinitive(...)
	 * 
	 * @param formValue
	 *            the <code>Form</code> of the phrase.
	 * @param frontVG
	 *            the first verb in the verb group.
	 */
	protected void checkInfinitive(Object formValue,
			NLGElement frontVG) {

		if ((Form.INFINITIVE.equals(formValue) || Form.BARE_INFINITIVE.equals(formValue))
				&& frontVG != null) {
			frontVG.setFeature(InternalFeature.NON_MORPH, true);
		}
	}

	/**
	 * Adds the passive auxiliary verb to the front of the group.
	 * 
	 * Based on English method addBe(...)
	 * 
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @return the new element for the front of the group.
	 */
	protected NLGElement addPassiveAuxiliary(NLGElement frontVG,
			Stack<NLGElement> vgComponents, PhraseElement phrase) {

		// adds the current front verb in pas participle form
		// with aggreement with the subject (auxiliary "être")
		if (frontVG != null) {
			frontVG.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
			Object number = phrase.getFeature(Feature.NUMBER);
			frontVG.setFeature(Feature.NUMBER, number);
			Object gender = phrase.getFeature(LexicalFeature.GENDER);
			frontVG.setFeature(LexicalFeature.GENDER, gender);
			vgComponents.push(frontVG);
		}
		// adds auxiliary "être"
		WordElement passiveAuxiliary = (WordElement)
			frontVG.getLexicon().lookupWord("zijn", LexicalCategory.VERB); //$NON-NLS-1$
		return new InflectedWordElement(passiveAuxiliary);
	}

	/**
	 * Adds the progressive auxiliary verb to the front of the group.
	 * 
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @return the new element for the front of the group.
	 */
	protected NLGElement addProgressiveAuxiliary(NLGElement frontVG,
			Stack<NLGElement> vgComponents, NLGFactory factory, PhraseElement phrase) {

		// pushes on stack "en train de " + clitics + verb in infinitive form
		if (frontVG != null) {
			frontVG.setFeature(Feature.FORM, Form.INFINITIVE);
			vgComponents.push(frontVG);
			insertCliticComplementPronouns(phrase, vgComponents);
			
			PPPhraseSpec deVerb = factory.createPrepositionPhrase("de");
//				deVerb.setComplement(frontVG);
			NPPhraseSpec train = factory.createNounPhrase("train");
			train.addPostModifier(deVerb);
			PPPhraseSpec enTrain = factory.createPrepositionPhrase("en", train);
			vgComponents.push(enTrain);
			
			// adds auxiliary "être"
			WordElement passiveAuxiliary = (WordElement)
				frontVG.getLexicon().lookupWord("être", LexicalCategory.VERB); //$NON-NLS-1$
			frontVG = new InflectedWordElement(passiveAuxiliary);
		}
		return frontVG;
	}

	/**
	 * Adds <em>have</em> to the stack.
	 * 
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @param modal
	 *            the modal to be used.
	 * @param tenseValue
	 *            the <code>Tense</code> of the phrase.
	 * @return the new element for the front of the group.
	 */
	protected AddAuxiliaryReturn addAuxiliary(NLGElement frontVG,
			Stack<NLGElement> vgComponents, String modal, Tense tenseValue,
			PhraseElement phrase) {
		NLGElement newFront = frontVG, pastParticipleHebben = null;
		WordElement auxiliaryWord = null;

		PhraseElement parent = (PhraseElement) phrase.getParent();
		if (parent != null && phrase.getParent().isA(PhraseCategory.VERB_PHRASE)) {
			if (parent.getHead() != null) {
				frontVG.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
				vgComponents.push(frontVG);
			}
		} else if (frontVG != null) {
			frontVG.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
			vgComponents.push(frontVG);
			// choose between "hebben" or "zijn" as auxiliary
			String auxiliary = "hebben"; //$NON-NLS-1$
			NLGElement vpHead = frontVG;
			if (frontVG.isA(PhraseCategory.VERB_PHRASE)) {
				// Lexical features like aux verbs are not copied to phrases. Instead look at the head's lexical features.
				VPPhraseSpec vp = (VPPhraseSpec) frontVG;
				vpHead = vp.getHead();
			}
			if ( frontVG.getFeatureAsBoolean(DutchLexicalFeature.AUXILIARY_ZIJN)
					|| vpHead.getFeatureAsBoolean(DutchLexicalFeature.AUXILIARY_ZIJN)
					|| hasReflexiveObject(phrase) ) {
                // if auxiliary "zijn", the past participle agrees with the subject
				auxiliary = "zijn"; //$NON-NLS-1$
				Object number = phrase.getFeature(Feature.NUMBER);
				frontVG.setFeature(Feature.NUMBER, number);
				Object gender = phrase.getFeature(LexicalFeature.GENDER);
				frontVG.setFeature(LexicalFeature.GENDER, gender);
			} else {
				pastParticipleHebben = frontVG;
			}
			
			auxiliaryWord = (WordElement)
				frontVG.getLexicon().lookupWord(auxiliary, LexicalCategory.VERB); //$NON-NLS-1$
		}
		newFront = new InflectedWordElement(auxiliaryWord);
		newFront.setFeature(Feature.FORM, Form.NORMAL);
		newFront.setFeature(Feature.TENSE, tenseValue);
		
		if (modal != null) {
			newFront.setFeature(InternalFeature.NON_MORPH, true);
		}
		return new AddAuxiliaryReturn(newFront, pastParticipleHebben);
	}

	/**
	 * Adds <em>zullen</em> to the stack.
	 *
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @param modal
	 *            the modal to be used.
	 * @param tenseValue
	 *            the <code>Tense</code> of the phrase.
	 * @return the new element for the front of the group.
	 */
	protected AddAuxiliaryReturn addZullen(NLGElement frontVG,
			Stack<NLGElement> vgComponents, String modal, Tense tenseValue,
			PhraseElement phrase) {
		NLGElement newFront = frontVG, pastParticipleZullen = null;
		WordElement auxiliaryWord = null;

		PhraseElement parent = (PhraseElement) phrase.getParent();

		if (frontVG != null) {
			frontVG.setFeature(Feature.FORM, Form.INFINITIVE);
			vgComponents.push(frontVG);
		}

		//future tense
		if (parent != null && phrase.getParent().isA(PhraseCategory.VERB_PHRASE)) {
			if (parent.getHead() != null) {
				frontVG.setFeature(Feature.FORM, Form.INFINITIVE);
				vgComponents.push(frontVG);
			}
		} else if (frontVG != null) {
			String auxiliary = "zullen"; //$NON-NLS-1$
			Object number = phrase.getFeature(Feature.NUMBER);
			frontVG.setFeature(Feature.NUMBER, number);
			Object gender = phrase.getFeature(LexicalFeature.GENDER);
			frontVG.setFeature(LexicalFeature.GENDER, gender);
			auxiliaryWord = (WordElement)
					frontVG.getLexicon().lookupWord(auxiliary, LexicalCategory.VERB); //$NON-NLS-1$

		}

		Tense auxTense = Tense.PRESENT;
		if (Tense.CONDITIONAL.equals(tenseValue)) auxTense = Tense.PAST;

		newFront = new InflectedWordElement(auxiliaryWord);
		newFront.setFeature(Feature.FORM, Form.NORMAL);
		newFront.setFeature(Feature.TENSE, auxTense);

		if (modal != null) {
			newFront.setFeature(InternalFeature.NON_MORPH, true);
		}
		return new AddAuxiliaryReturn(newFront, pastParticipleZullen);
	}

	/**
	 * Says if the verb phrase has a reflexive object (direct or indirect)
	 * 
	 * @param phrase	the verb phrase
	 * @return			true if the verb phrase has a reflexive object (direct or indirect)
	 */
	protected boolean hasReflexiveObject(PhraseElement phrase) {
		boolean reflexiveObjectFound = false;
		List<NLGElement> complements =
			phrase.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
		boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);
		Object subjectPerson = phrase.getFeature(Feature.PERSON);
		Object subjectNumber = phrase.getFeature(Feature.NUMBER);
		if (subjectNumber != NumberAgreement.PLURAL) {
			subjectNumber = NumberAgreement.SINGULAR;
		}
		
		for (NLGElement complement : complements) {
			if (complement != null && !complement.getFeatureAsBoolean(Feature.ELIDED)) {
				
				Object function = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
				boolean reflexive = complement.getFeatureAsBoolean(LexicalFeature.REFLEXIVE);
				Object person = complement.getFeature(Feature.PERSON);
				Object number = complement.getFeature(Feature.NUMBER);
				if (number != NumberAgreement.PLURAL) {
					number = NumberAgreement.SINGULAR;
				}
				
				// if the complement is a direct or indirect object
				if ( (function == DiscourseFunction.INDIRECT_OBJECT
						|| (!passive && function == DiscourseFunction.OBJECT))
					// and if it is reflexive, or the same as the subject if not third person
					&& ( reflexive ||
						((person == Person.FIRST || person == Person.SECOND)
								&& person == subjectPerson && number == subjectNumber) )) {
					reflexiveObjectFound = true;
					break;
				}
			}
		}
		
		return reflexiveObjectFound;
	}

	/**
	 * Class used to get two return values from the addAuxiliary method
	 * @author vaudrypl
	 */
	protected class AddAuxiliaryReturn {
		public final NLGElement newFront, pastParticipleAvoir;
		
		public AddAuxiliaryReturn(NLGElement newFront, NLGElement pastParticipleAvoir) {
			this.newFront = newFront;
			this.pastParticipleAvoir = pastParticipleAvoir;
		}
	}

	/**
	 * Adds <em>pas</em> to the stack if the phrase is negated.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param hasModal
	 *            the phrase has a modal
	 * @return the new element for the front of the group.
	 */
	protected void createNiet(PhraseElement phrase,
							  Stack<NLGElement> vgComponents, NLGElement frontVG, boolean hasModal) {
		if (phrase.getFeatureAsBoolean(Feature.NEGATED)) {
			// first get negation auxiliary; if not specified, it is "pas" by default
			WordElement negation = null;
			Lexicon lexicon = phrase.getLexicon();
			
			Object negationObject = phrase.getFeature(DutchFeature.NEGATION_AUXILIARY);
			if (negationObject instanceof WordElement) {
				negation = (WordElement) negationObject;
			} else if (negationObject != null) {
				String negationString;
				if (negationObject instanceof StringElement) {
					negationString = ((StringElement)negationObject).getRealisation();
				} else {
					negationString = negationObject.toString();
				}
				negation = lexicon.lookupWord(negationString);
			}
			
			if (negation == null) {
				negation = lexicon.lookupWord("niet", LexicalCategory.ADVERB);
			}
			InflectedWordElement inflNegation = new InflectedWordElement( negation ); //$NON-NLS-1$
			vgComponents.push(inflNegation);
		}
	}

	/**
	 * Adds <em>te</em> to the stack if the phrase is a te-infinitive verb phrase
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this verb phrase.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 */
	protected void createTe(PhraseElement phrase, Stack<NLGElement> vgComponents) {

		if (phrase.getFeatureAsBoolean(DutchFeature.TE_INFINITIVE)) {
			InflectedWordElement te = new InflectedWordElement( (WordElement)
				phrase.getFactory().createWord("te", LexicalCategory.ADVERB) ); //$NON-NLS-1$
	
			 vgComponents.push(te);
		}
	}
	
	/**
	 * Determines the number agreement for the phrase.
	 * 
	 * @param parent
	 *            the parent element of the phrase.
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @return the <code>NumberAgreement</code> to be used for the phrase.
	 */
	@Override
	protected NumberAgreement determineNumber(NLGElement parent,
			PhraseElement phrase) {
		Object numberValue = phrase.getFeature(Feature.NUMBER);
		NumberAgreement number = null;
		
		if (numberValue instanceof NumberAgreement) {
			number = (NumberAgreement) numberValue;
		} else {
			number = NumberAgreement.SINGULAR;
		}
		
		return number;
	}

	/**
	 * Pushes the front verb onto the stack of verb components.
	 * Sets the front verb features.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 * @param frontVG
	 *            the first verb in the verb group.
	 * @param formValue
	 *            the <code>Form</code> of the phrase.
	 * @param interrogative
	 *            <code>true</code> if the phrase is interrogative.
	 */
	@Override
	protected void pushFrontVerb(PhraseElement phrase,
			Stack<NLGElement> vgComponents, NLGElement frontVG,
			Object formValue, boolean interrogative) {
		
		if (Form.GERUND.equals(formValue)) {
			frontVG.setFeature(Feature.FORM, Form.PRESENT_PARTICIPLE);
			vgComponents.push(frontVG);
		
		} else if (Form.PAST_PARTICIPLE.equals(formValue)) {
			frontVG.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
			vgComponents.push(frontVG);
		
		} else if (Form.PRESENT_PARTICIPLE.equals(formValue)) {
			frontVG.setFeature(Feature.FORM, Form.PRESENT_PARTICIPLE);
			vgComponents.push(frontVG);
		
		} else if (!(formValue == null || Form.NORMAL.equals(formValue)
						|| formValue == Form.SUBJUNCTIVE
						|| formValue == Form.IMPERATIVE )
				&& !isCopular(phrase.getHead()) && vgComponents.isEmpty()) {

			vgComponents.push(frontVG);
		
		} else if (phrase.getParent() != null
						&& ClauseStatus.SUBORDINATE.equals(
								phrase.getParent().getFeature(InternalFeature.CLAUSE_STATUS))
						&& phrase.getParent().getFeatureAsString(InternalFeature.SUBJECTS) == null) {
			PhraseElement parent = (PhraseElement) phrase.getParent();
			NumberAgreement numToUse = determineNumber(phrase.getParent().getParent(),
					(PhraseElement) parent.getParent());
			frontVG.setFeature(Feature.PERSON, parent.getParent()
					.getFeature(Feature.PERSON));
			frontVG.setFeature(Feature.NUMBER, numToUse);
			vgComponents.push(frontVG);

		} else {
			NumberAgreement numToUse = determineNumber(phrase.getParent(),
					phrase);
			frontVG.setFeature(Feature.PERSON, phrase
					.getFeature(Feature.PERSON));
			frontVG.setFeature(Feature.NUMBER, numToUse);
			vgComponents.push(frontVG);
		}
	}

	/**
	 * Add a modifier to a verb phrase. Use heuristics to decide where it goes.
	 * Based on method of the same name in English verb phrase helper.
	 * Reference : section 935 of Grevisse (1993)
	 * 
	 * @param verbPhrase
	 * @param modifier
	 * 
	 * @author vaudrypl, rfdj
	 */
	@Override
	public void addModifier(VPPhraseSpec verbPhrase, Object modifier) {

		if (modifier != null) {
		
			// get modifier as NLGElement if possible
			NLGElement modifierElement = null;
			if (modifier instanceof NLGElement)
				modifierElement = (NLGElement) modifier;
			else if (modifier instanceof String) {
				String modifierString = (String) modifier;

				if (modifierString.length() > 0 && !modifierString.contains(" ")
						&& verbPhrase.getLexicon().hasWord(modifierString, LexicalCategory.ADJECTIVE))
					modifierElement = verbPhrase.getLexicon().lookupWord(
							modifierString, LexicalCategory.ADJECTIVE);


				if (modifierElement == null
						&& modifierString.length() > 0
						&& !modifierString.contains(" "))
					modifierElement = verbPhrase.getFactory().createWord(modifier,
							LexicalCategory.ADVERB);
			}
		
			// if no modifier element, must be a complex string
			if (modifierElement == null) {
				verbPhrase.addPostModifier((String) modifier);
			} else if (modifierElement.isA(LexicalCategory.ADJECTIVE)
					|| modifierElement.getFeatureAsBoolean(DutchLexicalFeature.PREPOSED)) {
				verbPhrase.addPreModifier(modifierElement);
			} else {
					// default case
					verbPhrase.addPostModifier(modifierElement);
			}
		}
	}

    /**
     * Realises the reflexive pronouns in complements of this phrase and removes them from the complements list.
     *
     * @param phrase
     *            the <code>PhraseElement</code> representing this noun phrase.
     * @param realisedElement
     *            the current realisation of the noun phrase.
     */
    protected void realiseReflexivePronouns(PhraseElement phrase,
                                      ListElement realisedElement) {
        NLGElement currentElement = null;
        Object discourseValue = null;
        List<NLGElement> complements = phrase.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
        List<NLGElement> toRemove = new ArrayList<>();

        for (NLGElement complement : complements) {
            currentElement = complement.realiseSyntax();
            discourseValue = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);

            List<NLGElement> children = complement.getChildren();

            if (DiscourseFunction.OBJECT.equals(discourseValue)
                    && children.get(0).isA(LexicalCategory.PRONOUN)) {
                realisedElement.addComponent(currentElement);
                toRemove.add(complement);
            }
        }
        // remove used complements from the internal feature
        complements.removeAll(toRemove);
        phrase.setFeature(InternalFeature.COMPLEMENTS, complements);

    }

	/**
	 * Realises the complements of this phrase.
	 * Based on English method of the same name.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @param realisedElement
	 *            the current realisation of the noun phrase.
	 */
	@Override
	protected void realiseComplements(PhraseElement phrase,
									  ListElement realisedElement) {
		realiseComplements(phrase, realisedElement, true);
	}
	protected void realiseComplements(PhraseElement phrase,
			ListElement realisedElement, Boolean phraseHasMainVerb) {

		ListElement indirects = new ListElement();
		ListElement directs = new ListElement();
		ListElement unknowns = new ListElement();
		Object discourseValue = null;
		NLGElement currentElement = null;

		for (NLGElement complement : phrase
				.getFeatureAsElementList(InternalFeature.COMPLEMENTS)) {
			if (!complement.getFeatureAsBoolean(FrenchInternalFeature.CLITIC)) {
				
				discourseValue = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);

				if (!(discourseValue instanceof DiscourseFunction)) {
					discourseValue = DiscourseFunction.COMPLEMENT;
				}

				// Realise complement only if it is not the relative phrase of
				// the parent clause and not a phrase with the same function in case
				// of a direct or indirect object.
				NLGElement parent = phrase.getParent();
				if ( parent == null ||
						(!complement.getFeatureAsBoolean(DutchInternalFeature.RELATIVISED) &&
							complement != parent.getFeatureAsElement(DutchFeature.RELATIVE_PHRASE) &&
							(discourseValue == DiscourseFunction.COMPLEMENT ||
								!parent.hasRelativePhrase((DiscourseFunction) discourseValue)))) {
					
					if (DiscourseFunction.INDIRECT_OBJECT.equals(discourseValue)) {
						complement = checkIndirectObject(complement);
					}
					
					currentElement = complement.realiseSyntax();
	
					if (currentElement != null) {
						currentElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
								discourseValue);
	
						if (DiscourseFunction.INDIRECT_OBJECT.equals(discourseValue)) {
							indirects.addComponent(currentElement);
						} else if (DiscourseFunction.OBJECT.equals(discourseValue)) {
							if (!Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))
									&& !Tense.CONDITIONAL.equals(phrase.getFeature(Feature.TENSE))
									&& !phrase.getFeatureAsBoolean(Feature.PERFECT))
								directs.addComponent(currentElement);
						} else {
							unknowns.addComponent(currentElement);
						}
					}
				} else {
				// Reset relativised feature if the complement was a relative phrase.
					complement.removeFeature(DutchInternalFeature.RELATIVISED);
				}
			}
			// Reset the clitic selection feature after use.
			complement.removeFeature(DutchInternalFeature.CLITIC);
		}

		// Reference : section 657 of Grevisse (1993)
		// normal order, when complements are all of the same length :
		// direct objects + indirect objects + other complements
		// when objects are longer than others, they are placed after them
		int numberOfWordDirects = NLGElement.countWords(directs.getChildren());
		int numberOfWordIndirects = NLGElement.countWords(indirects.getChildren());
		int numberOfWordUnknowns = NLGElement.countWords(unknowns.getChildren());
		// there are 3*2*1 = 6 orders possible
		if (numberOfWordDirects <= numberOfWordIndirects) {
			if (numberOfWordIndirects <= numberOfWordUnknowns) {
				// normal order
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
				addIndirectObjects(indirects, phrase, realisedElement);
				addUnknownComplements(unknowns, phrase, realisedElement);
			} else if (numberOfWordDirects <= numberOfWordUnknowns) {
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
				addUnknownComplements(unknowns, phrase, realisedElement);
				addIndirectObjects(indirects, phrase, realisedElement);
			} else {
				addUnknownComplements(unknowns, phrase, realisedElement);
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
				addIndirectObjects(indirects, phrase, realisedElement);
			}
		} else {
			if (numberOfWordDirects <= numberOfWordUnknowns) {
				addIndirectObjects(indirects, phrase, realisedElement);
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
				addUnknownComplements(unknowns, phrase, realisedElement);
			} else if (numberOfWordIndirects <= numberOfWordUnknowns) { //here
				addIndirectObjects(indirects, phrase, realisedElement);
				addUnknownComplements(unknowns, phrase, realisedElement);
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
			} else {
				addUnknownComplements(unknowns, phrase, realisedElement);
				addIndirectObjects(indirects, phrase, realisedElement);
				addDirectObjects(directs, phrase, realisedElement);
				realisePreverb(phrase, realisedElement);
			}
		}
	}

	/**
	 * Realise the direct and indirect objects of any NLGElement and removes them from the complements list.
	 *
	 * @param element the element which has objects
	 */
	protected void realiseObjectsOfElement(ListElement realisedElement, NLGElement element) {
		List<NLGElement> complements = element.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
		List<NLGElement> complementsToRemove = new ArrayList<>();

		for (NLGElement complement : complements) {

			Object discourseFunction = complement.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			if (discourseFunction == DiscourseFunction.OBJECT
					|| discourseFunction == DiscourseFunction.INDIRECT_OBJECT) {
				realisedElement.addComponent(complement.realiseSyntax());
				complementsToRemove.add(complement);
			}
		}

		complements.removeAll(complementsToRemove);
		element.setFeature(InternalFeature.COMPLEMENTS, complements);
	}

	/**
	 * Realises the preverb of this phrase.
	 *
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @param realisedElement
	 *            the current realisation of the noun phrase.
	 */
	protected void realisePreverb(PhraseElement phrase,
									  ListElement realisedElement) {
		String preverb = phrase.getFeatureAsString(DutchFeature.PREVERB);
		NLGElement parent = phrase.getParent();

		if (parent != null
				&& !parent.getFeatureAsBoolean(Feature.PERFECT)
				&& !phrase.getFeatureAsBoolean(Feature.PERFECT)
				&& !parent.getFeatureAsBoolean(Feature.PASSIVE)
				&& Form.PAST_PARTICIPLE != phrase.getFeature(Feature.FORM)
				&& Tense.FUTURE != phrase.getFeature(Feature.TENSE)
				&& Tense.CONDITIONAL != phrase.getFeature(Feature.TENSE)
				) {
			if (preverb == null) {
				NLGElement head = phrase.getHead();

				preverb = head.getFeatureAsString(DutchFeature.PREVERB);

				if (preverb == null
						&& !(head instanceof VPPhraseSpec)
						&& !head.hasFeature(LexicalFeature.PAST_PARTICIPLE)
						){
					preverb = getSeparableCompoundVerb((WordElement) phrase.getHead()).preVerb;
				}

			}

			if (preverb != null) {
				realisedElement.addComponent(new WordElement(preverb, phrase.getLexicon()));
			}
		}
	}

	/**
	 * Determine if the verb is a separable compound verb
	 *
	 * Reference: http://taalportaal.org/taalportaal/topic/pid/topic-13998813296768009
	 *
	 * @param baseForm
	 * @return true if element is a separable compound verb
	 */
	public static GetSeparableCompoundVerbReturn getSeparableCompoundVerb(String baseForm) {
		return getSeparableCompoundVerb(null, baseForm);
	}

	/**
	 * Determine if the verb is a separable compound verb
	 *
	 * Reference: http://taalportaal.org/taalportaal/topic/pid/topic-13998813296768009
	 *
	 * @param element WordElement
	 * @return true if element is a separable compound verb
	 */
	public static GetSeparableCompoundVerbReturn getSeparableCompoundVerb(WordElement element) {
		return getSeparableCompoundVerb(new  InflectedWordElement(element), null);
	}

	/**
	 * Determine if the verb is a separable compound verb
	 *
	 * Reference: http://taalportaal.org/taalportaal/topic/pid/topic-13998813296768009
	 *
	 * @param element InflectedWordElement
	 * @return true if element is a separable compound verb
	 */
	public static GetSeparableCompoundVerbReturn getSeparableCompoundVerb(InflectedWordElement element) {
		return getSeparableCompoundVerb(element, null);
	}

	/**
	 * Determine if the verb is a separable compound verb
	 *
	 * Reference: http://taalportaal.org/taalportaal/topic/pid/topic-13998813296768009
	 *
	 * @param element
	 * @param baseForm
	 * @return true if element is a separable compound verb
	 */
	public static GetSeparableCompoundVerbReturn getSeparableCompoundVerb(InflectedWordElement element, String baseForm) {

		String mainVerb = null;
		String preVerb = null;
		Boolean isSCV = false;

		if (baseForm == null || "".equals(baseForm))
			baseForm = element.getBaseForm();

		if (element != null) {

			// We have multiple options to attempt to detect SCVs.
			// Option 1: Try to get preverb from feature
			if (element.getFeatureAsString(DutchFeature.PREVERB) != null) {
				preVerb = element.getFeatureAsString(DutchFeature.PREVERB);
			}

			// Option 2: Try to get preverb from lexical feature
			if (preVerb == null && element.getFeatureAsString(DutchLexicalFeature.PREVERB) != null) {
				preVerb = element.getFeatureAsString(DutchLexicalFeature.PREVERB);
			}

			// Option 3: Try to get preverb from parent's feature
			NLGElement parent = element.getParent();
			if (preVerb == null && parent != null) {
				if (parent.getFeatureAsString(DutchFeature.PREVERB) != null) {
					preVerb = parent.getFeatureAsString(DutchFeature.PREVERB);
				}
			}

			// Option 3: If the lexicon entry has a <past> child, we can check for white spaces in the child's value.
			/*if (preVerb == null) {
				String pastRadical = element.getFeatureAsString(LexicalFeature.PAST);

				if (pastRadical != null && pastRadical.contains(" ")) {
					String[] splitVerb = pastRadical.split("\\s+", 2);

					if (splitVerb.length > 1) {
						preVerb = splitVerb[1]; // the remainder will stay in the same order (e.g. 'hield <i>in stand</i>')
					}
				}
			}*/
		}

		// Option 4: If the baseForm contains a pipe from the user input
		if (baseForm.contains("|") && (preVerb == null || "".equals(preVerb))) {
			String[] splitVerb = baseForm.split("\\|", 2);

			if (splitVerb.length > 1) {
				preVerb = splitVerb[0];
			}
		}

		// Option 5: If all else fails, try splitting off common prefixes
		if (preVerb == null || "".equals(preVerb)) {
			// Detect common prefixes as preverb
			// There is no way to getWords by category alone, so here is a hardcoded list of prepositions that make
			// the verbs separable. Problems: some other prepositions can split the verb, but not always. Also, this
			// method does not check for the ends of syllables, just beginnings of words.
			// Reference: https://www.taal-oefenen.nl/instruction/taal/werkwoorden/werkwoorden-algemeen/samengestelde-werkwoorden-scheidbaar-en-onscheidbaar
			// Added "toe" because no exceptions could be found.

			String[] prefixesOfSeparableVerbs = {"bij", "in", "na", "uit",
					"op", "af", "mee", "tegen", "tussen", "terug", "toe"};

			for (String prefix : prefixesOfSeparableVerbs) {
				if (baseForm.startsWith(prefix) && baseForm.length() - prefix.length() > 3) {
					preVerb = prefix;
				}
			}
		}

		if (preVerb != null) {
			mainVerb = baseForm.substring(preVerb.length(), baseForm.length());
			mainVerb = mainVerb.replaceAll("\\|", "");
			if (element != null) element.setFeature(DutchFeature.PREVERB, preVerb);
			isSCV = true;
		}
		return new GetSeparableCompoundVerbReturn(isSCV, mainVerb, preVerb);
	}


	/**
	 * Class used to get two return values from the getSeparableCompoundVerb method
	 * @author rfdj
	 */
	public static class GetSeparableCompoundVerbReturn {
		public final Boolean isSCV;
		public final String mainVerb;
		public final String preVerb;

		public GetSeparableCompoundVerbReturn(Boolean isSCV, String mainVerb, String preVerb) {
			this.isSCV = isSCV;
			this.mainVerb = mainVerb;
			this.preVerb = preVerb;
		}
	}

	/**
	 * Adds realised direct objects to the complements realisation
	 * @param directs			realised direct objects
	 * @param phrase			the verb phrase to wich belongs those complements
	 * @param realisedElement	complements realisation
	 */
	protected void addDirectObjects(ListElement directs, PhraseElement phrase,
			ListElement realisedElement) {
		boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);
		if (!passive && !InterrogativeType.isObject(phrase
					.getFeature(Feature.INTERROGATIVE_TYPE))) {
			realisedElement.addComponents(directs.getChildren());
		}
	}

	/**
	 * Adds realised indirect objects to the complements realisation
	 * @param indirects			realised indirect objects
	 * @param phrase			the verb phrase to wich belongs those complements
	 * @param realisedElement	complements realisation
	 */
	protected void addIndirectObjects(ListElement indirects, PhraseElement phrase,
			ListElement realisedElement) {
		if (!InterrogativeType.isIndirectObject(phrase
				.getFeature(Feature.INTERROGATIVE_TYPE))) {
			realisedElement.addComponents(indirects.getChildren());
		}
	}

	/**
	 * Adds unknown complements to the complements realisation
	 * @param unknowns			unknown complements
	 * @param phrase			the verb phrase to wich belongs those complements
	 * @param realisedElement	complements realisation
	 */
	protected void addUnknownComplements(ListElement unknowns, PhraseElement phrase,
			ListElement realisedElement) {
		if (!phrase.getFeatureAsBoolean(Feature.PASSIVE)) {
			realisedElement.addComponents(unknowns.getChildren());
		}
	}

	/**
	 * Adds a default preposition to all indirect object noun phrases.
	 * Checks also inside coordinated phrases.
	 * 
	 * @param element the noun phrase
	 * @return the new complement
	 * 
	 * @vaudrypl
	 */
	@SuppressWarnings("unchecked")
	protected NLGElement checkIndirectObject(NLGElement element) {
		if (element instanceof NPPhraseSpec) {
			NLGFactory factory = element.getFactory();
			NPPhraseSpec elementCopy = new NPPhraseSpec((NPPhraseSpec) element);
			PPPhraseSpec newElement = factory.createPrepositionPhrase("van", elementCopy);
			element.setFeature(InternalFeature.DISCOURSE_FUNCTION, DiscourseFunction.INDIRECT_OBJECT);
			element = newElement;
		} else if (element instanceof CoordinatedPhraseElement) {
			element = new CoordinatedPhraseElement( (CoordinatedPhraseElement) element );
			Object coordinates = element.getFeature(InternalFeature.COORDINATES);
			if (coordinates instanceof List) {
				List<NLGElement> list = (List<NLGElement>) coordinates;
				for (int index = 0; index < list.size(); ++index) {
					list.set(index, checkIndirectObject(list.get(index)));
				}
			}
		}
		
		return element;
	}

	/**
	 * Pushes the modal onto the stack of verb components.
	 * Sets the modal features.
	 * Based on English VerbPhraseHelper
	 * 
	 * @param modalWord
	 *            the modal to be used.
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this noun phrase.
	 * @param vgComponents
	 *            the stack of verb components in the verb group.
	 */
	protected void pushModal(WordElement modalWord, PhraseElement phrase,
			Stack<NLGElement> vgComponents) {
		if (modalWord != null
				&& !phrase.getFeatureAsBoolean(InternalFeature.IGNORE_MODAL)) {
			InflectedWordElement inflectedModal = new InflectedWordElement(modalWord);
			
			Object form = phrase.getFeature(Feature.FORM);
			inflectedModal.setFeature(Feature.FORM, form);
			
			Object tense = phrase.getFeature(Feature.TENSE);
			tense = (tense != Tense.PAST) ? tense : Tense.PRESENT;
			inflectedModal.setFeature(Feature.TENSE, tense);
			
			inflectedModal.setFeature(Feature.PERSON, phrase.getFeature(Feature.PERSON));
			
			NumberAgreement numToUse = determineNumber(phrase.getParent(), phrase);
			inflectedModal.setFeature(Feature.NUMBER, numToUse);
			
			vgComponents.push(inflectedModal);
		}
	}

	/**
	 * Realises the auxiliary verbs in the verb group.
	 * 
	 * @param realisedElement
	 *            the current realisation of the noun phrase.
	 * @param auxiliaryRealisation
	 *            the stack of auxiliary verbs.
	 */
	@Override
	protected void realiseAuxiliaries(ListElement realisedElement,
			Stack<NLGElement> auxiliaryRealisation) {

		NLGElement aux = null;
		NLGElement currentElement = null;
		while (!auxiliaryRealisation.isEmpty()) {
			aux = auxiliaryRealisation.pop();
			currentElement = aux.realiseSyntax();
			
			if (currentElement != null) {
				realisedElement.addComponent(currentElement);
				
				if (currentElement.isA(LexicalCategory.VERB)
					|| currentElement.isA(LexicalCategory.MODAL)
					|| currentElement.isA(PhraseCategory.VERB_PHRASE)) {
				currentElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
						DiscourseFunction.AUXILIARY);
				}
			}
		}
	}

}
