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

import gov.nih.nlm.nls.lvg.Util.Str;
import simplenlg.features.*;
import simplenlg.features.dutch.DutchFeature;
import simplenlg.features.french.FrenchFeature;
import simplenlg.features.french.FrenchInternalFeature;
import simplenlg.features.french.FrenchLexicalFeature;
import simplenlg.framework.*;
import simplenlg.phrasespec.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class containing the main methods for realising the syntax
 * of clauses for French.
 * 
 * Reference :
 * Grevisse, Maurice (1993). Le bon usage, grammaire française,
 * 12e édition refondue par André Goosse, 8e tirage, Éditions Duculot,
 * Louvain-la-Neuve, Belgique.
 * 
 * @author vaudrypl
 */
public class ClauseHelper extends simplenlg.syntax.english.nonstatic.ClauseHelper {
	/**
	 * This method does nothing in the French clause syntax helper.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	@Override
	protected void addEndingTo(PhraseElement phrase,
			ListElement realisedElement, NLGFactory phraseFactory) {}

	/**
	 * Adds the specifier to the beginning of the clause when dealing with WHO_INDIRECT_OBJECT
	 * interrogatives.
	 *
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 */
	protected void addInterrogativeSpecifier(PhraseElement phrase,
												  ListElement realisedElement) {
		if (phrase.getFeature(Feature.INTERROGATIVE_TYPE) == InterrogativeType.WHO_INDIRECT_OBJECT) {

		}
	}

	/**
	 * Checks the subjects of the phrase to determine if there is more than one
	 * subject. This ensures that the verb phrase is correctly set. Also set
	 * person and gender correctly.
	 * Also sets FrenchLexicalFeature.NE_ONLY_NEGATION
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 */
	@Override
	protected void checkSubjectNumberPerson(PhraseElement phrase,
			NLGElement verbElement) {
		boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);
		// If the clause has a relativised subject, make subject agreement
		// with parent noun phrase instead.
		List<NLGElement> subjects =
				phrase.getFeatureAsElementList(InternalFeature.SUBJECTS);
		List<NLGElement> normalSubjects = subjects;
		if ((!passive && phrase.hasRelativePhrase(DiscourseFunction.SUBJECT))
				|| (passive && phrase.hasRelativePhrase(DiscourseFunction.OBJECT))) {
			subjects = new ArrayList<NLGElement>();
			NLGElement parentNP = phrase.getParent();
			if (parentNP instanceof NPPhraseSpec) subjects.add(parentNP);
			phrase.setFeature(InternalFeature.SUBJECTS, subjects);
		}
		
		super.checkSubjectNumberPerson(phrase, verbElement);
		// Put the original subject back if it was changed (to call the superclass
		// version of the function) because the subject or object was relativised.
		if (subjects != normalSubjects) {
			phrase.setFeature(InternalFeature.SUBJECTS, normalSubjects);
		}

		boolean noOnlyNegation = false;
		boolean feminine = false;
		Person person = Person.THIRD;
		
		if (subjects != null && subjects.size() >= 1) {
			feminine = true;
			for (NLGElement currentElement : subjects) {
				Object gender = currentElement.getFeature(LexicalFeature.GENDER);
				if (gender != Gender.FEMININE) {
					feminine = false;
				}
				// If there's at least one first person subject, the subjects as a whole
				// are first person. Otherwise, if there's at least on second person subject,
				// the subjects as a whole are second person. Otherwise they are third person
				// by default.
				Object currentPerson = currentElement.getFeature(Feature.PERSON);
				if (currentPerson == Person.FIRST) {
					person = Person.FIRST;
				} else if (person == Person.THIRD && currentPerson == Person.SECOND) {
					person = Person.SECOND;
				}

				if (!noOnlyNegation) {
					noOnlyNegation = currentElement.checkIfNeOnlyNegation();
				}
			}
		}
		// If there is at least one feminine subject and nothing else, the gender
		// of the subject group is feminine. Otherwise, it is masculine.
		if (feminine) {
			verbElement.setFeature(LexicalFeature.GENDER, Gender.FEMININE);
		}
		else {
			verbElement.setFeature(LexicalFeature.GENDER, Gender.MASCULINE);
		}
		
		verbElement.setFeature(Feature.PERSON, person);
		setNeOnlyNegation(verbElement, noOnlyNegation);
	}
	
	/**
	 * Check complements and sets FrenchLexicalFeature.NE_ONLY_NEGATION
	 * accordingly for the verb phrase.
	 * 
	 * @param verbElement	the verb phrase
	 */
	protected void setNeOnlyNegation(NLGElement verbElement, boolean noOnlyNegation) {
		// check complements if subject doesn't already have the feature
		if (!noOnlyNegation) {
			
			List<NLGElement> complements =
				verbElement.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
			
			for (NLGElement current : complements) {
				if ( current.checkIfNeOnlyNegation() ) {
					noOnlyNegation = true;
					break;
				}
			}
		}
		
		verbElement.setFeature(FrenchLexicalFeature.NE_ONLY_NEGATION, noOnlyNegation);
	}

	/**
	 * Add a modifier to a clause. Use heuristics to decide where it goes.
	 * Based on method of the same name in English clause helper
	 * Reference : section 935 of Grevisse (1993)
	 * 
	 * @param clause
	 * @param modifier
	 * 
	 * @author vaudrypl, rfdj
	 */
	@Override
	public void addModifier(SPhraseSpec clause, Object modifier) {

		if (modifier != null) {
		
			// get modifier as NLGElement if possible
			NLGElement modifierElement = null;
			if (modifier instanceof NLGElement)
				modifierElement = (NLGElement) modifier;
			else if (modifier instanceof String) {
				String modifierString = (String) modifier;
				if (modifierString.length() > 0 && !modifierString.contains(" "))
					modifierElement = clause.getFactory().createWord(modifier,
							LexicalCategory.ADVERB);
			}
		
			// if no modifier element, must be a complex string
			if (modifierElement == null) {
				clause.addPostModifier((String) modifier);
			} else if (clause.hasFeature(DutchFeature.RELATIVE_PHRASE)) {
				clause.addPreModifier(modifierElement);
			} else {
				// default case
				clause.addPostModifier(modifierElement);
			}
		}
	}
		
	/**
	 * Checks the discourse function of the clause and alters the form of the
	 * clause as necessary.
	 * 
	 * Based on method of the same name in English syntax processor
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 */
	@Override
	protected void checkDiscourseFunction(PhraseElement phrase) {
		Object clauseForm = phrase.getFeature(Feature.FORM);
		Object discourseValue = phrase
				.getFeature(InternalFeature.DISCOURSE_FUNCTION);

		if (DiscourseFunction.OBJECT.equals(discourseValue)
				|| DiscourseFunction.INDIRECT_OBJECT.equals(discourseValue)) {

			if (Form.IMPERATIVE.equals(clauseForm)) {
				phrase.setFeature(Feature.FORM, Form.INFINITIVE);
			}
		}
	}

	/**
	 * Checks if there are any clausal subjects and if so, put each of them
	 * in a "le fait" + (conjunction) construction.
	 * 
	 * @param phrase
	 * 
	 * @author vaudrypl
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void checkClausalSubjects(PhraseElement phrase) {
		Object subjects = phrase.getFeature(InternalFeature.SUBJECTS);
		List<NLGElement> subjectList = null;
		if (subjects instanceof CoordinatedPhraseElement) {
			subjects = ((CoordinatedPhraseElement)subjects).getFeature(InternalFeature.COORDINATES);
		}
		if (subjects instanceof List) subjectList = (List<NLGElement>) subjects;
		
		if (subjectList != null) {
			for (int index = 0; index < subjectList.size(); ++index) {
				NLGElement currentSubject = subjectList.get(index);
				
				if (currentSubject instanceof SPhraseSpec) {
					Object form = currentSubject.getFeature(Feature.FORM);
					NLGElement verbPhrase = ((SPhraseSpec)currentSubject).getVerbPhrase();
					if (form == null && verbPhrase != null) form = verbPhrase.getFeature(Feature.FORM);
					if (form == Form.NORMAL || form == null) {
						NLGFactory factory = phrase.getFactory();
						NPPhraseSpec newSubject = factory.createNounPhrase("le", "fait");
						newSubject.addPostModifier(currentSubject);
						
						currentSubject.setFeature(InternalFeature.CLAUSE_STATUS, ClauseStatus.SUBORDINATE);
						currentSubject.setFeature(Feature.SUPRESSED_COMPLEMENTISER, false);
						
						currentSubject = newSubject;
					}
				}
				
				subjectList.set(index, currentSubject);
			}
		}
	}

	/**
	 * Copies the front modifiers of the clause to the list of post-modifiers of
	 * the verb only if the phrase has infinitive form.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 */
	@Override
	protected void copyFrontModifiers(PhraseElement phrase,
			NLGElement verbElement) {
		super.copyFrontModifiers(phrase, verbElement);

		// If the complementiser of an infinitive clause is "que", it is suppressed,
		// otherwise it is not suppressed.
		Object clauseForm = phrase.getFeature(Feature.FORM);
		Object clauseStatus = phrase.getFeature(InternalFeature.CLAUSE_STATUS);
		Object complementiser = phrase.getFeature(Feature.COMPLEMENTISER);
		WordElement que = phrase.getLexicon().lookupWord("que", LexicalCategory.COMPLEMENTISER);
		if (clauseForm == Form.INFINITIVE && clauseStatus == ClauseStatus.SUBORDINATE) {
			if (que.equals(complementiser)) phrase.setFeature(Feature.SUPRESSED_COMPLEMENTISER, true);
			else phrase.setFeature(Feature.SUPRESSED_COMPLEMENTISER, false);
		}
	}

	/**
	 * Realises the cue phrase for the clause if it exists. In French,
	 * checks if the phrase is infinitive and doesn't realise the cue phrase
	 * if so.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 */
	@Override
	protected void addCuePhrase(PhraseElement phrase,
			ListElement realisedElement) {
		Object form = phrase.getFeature(Feature.FORM);
		if (form != Form.INFINITIVE) super.addCuePhrase(phrase, realisedElement); 
	}

	/**
	 * Checks to see if this clause is a subordinate clause or is in the
	 * subjunctive mood. If it is then the complementiser is added
	 * as a component to the realised element <b>unless</b> the complementiser
	 * has been suppressed.
	 * 
	 * If this is a relative clause, the correct relative pronoun is added instead.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 */
	@Override
	protected void addComplementiser(PhraseElement phrase,
			ListElement realisedElement) {

		// Relative clause.
		NLGElement relativePhrase = phrase.getFeatureAsElement(DutchFeature.RELATIVE_PHRASE);
		if ( relativePhrase != null) {
			
			// Get discourse function.
			Object functionObject = relativePhrase.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			DiscourseFunction function;
			List<NLGElement> subjects = phrase.getFeatureAsElementList(InternalFeature.SUBJECTS);
			if (functionObject instanceof DiscourseFunction) {
				function = (DiscourseFunction) functionObject;
			} else if (subjects != null && subjects.contains(relativePhrase)) {
				function = DiscourseFunction.SUBJECT;
			} else {
				function = DiscourseFunction.COMPLEMENT;
			}
			// Decide which relative pronoun to use.
			NLGFactory factory = phrase.getFactory();
			NLGElement relativePronoun = null;
			NLGElement preposition = null;
			boolean passive = phrase.getFeatureAsBoolean(Feature.PASSIVE);

			Object numberValue = relativePhrase.getFeature(Feature.NUMBER);
			// default number is SINGULAR
			NumberAgreement number = NumberAgreement.SINGULAR;
			if (numberValue instanceof NumberAgreement) {
				number = (NumberAgreement) numberValue;
			}

			Object genderValue = relativePhrase.getFeature(LexicalFeature.GENDER);
			// default gender is COMMON
			Gender gender = Gender.COMMON;
			if (genderValue instanceof Gender) {
				gender = (Gender) genderValue;
			}
			String specifier = relativePhrase.getFeatureAsString(InternalFeature.SPECIFIER);
			if ("het".equals(specifier)){
				gender = Gender.NEUTER;
			}

			if (gender == Gender.NEUTER && number == NumberAgreement.SINGULAR) {
				relativePronoun = factory.createNounPhrase(
						factory.createWord("dat", LexicalCategory.PRONOUN) );
			} else {
				relativePronoun = factory.createNounPhrase(
						factory.createWord("die", LexicalCategory.PRONOUN) );
			}

			phrase.setFeature(Feature.NUMBER, number);


			// Add relative pronoun.
			if (relativePronoun != null) {
				relativePronoun.setFeature(InternalFeature.DISCOURSE_FUNCTION, function);
				relativePronoun.setParent(phrase);
				relativePronoun = relativePronoun.realiseSyntax();
				if (relativePronoun != null) {
					realisedElement.addComponent(relativePronoun);
				}
			}
			
		// Realise complementiser if appropriate.
		} else if ((phrase.getFeature(InternalFeature.CLAUSE_STATUS) == ClauseStatus.SUBORDINATE
					|| phrase.getFeature(Feature.FORM) == Form.SUBJUNCTIVE)
				&& !phrase.getFeatureAsBoolean(Feature.SUPRESSED_COMPLEMENTISER)) {
	
			Object complementiser = phrase.getFeature(Feature.COMPLEMENTISER);
			NLGFactory factory = phrase.getFactory();
			NLGElement currentElement = factory.createNLGElement(complementiser, LexicalCategory.COMPLEMENTISER);
			if (currentElement != null) {
				currentElement = currentElement.realiseSyntax();
				if (currentElement != null) {
					realisedElement.addComponent(currentElement);
				}
			}
		}
	}

	/**
	 * Adds the subjects to the beginning of the clause unless the clause is
	 * infinitive, imperative or passive, the subjects split the verb or,
	 * in French, the relative phrase discourse function is subject.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param splitVerb
	 *            an <code>NLGElement</code> representing the subjects that
	 *            should split the verb
	 */
	@Override
	protected void addSubjectsToFront(PhraseElement phrase,
			ListElement realisedElement,
			NLGElement splitVerb) {
		
		if (!phrase.hasRelativePhrase(DiscourseFunction.SUBJECT)) {
			if(phrase.hasFeature(Feature.INTERROGATIVE_TYPE)){
				ListElement realisedSubject = new ListElement();
				//Realising subject to add later
				super.addSubjectsToFront(phrase, realisedSubject,splitVerb);
				if(this.addSubjectAfterVerb(realisedElement,realisedSubject)){
					return;
				}
			}
			super.addSubjectsToFront(phrase, realisedElement, splitVerb);
		}
	}

	/**
	 * Method for adding the subject after the verb. Mainly used for Dutch interrogatives
	 * @param realisedElement, the current list of realised elements
	 * @param realisedSubject, the subject that has to go behind the verb
	 */
	private boolean addSubjectAfterVerb(ListElement realisedElement, ListElement realisedSubject){
		List<NLGElement> alreadyRealisedElements = realisedElement.getChildren();
		for(int vpIndex = 0; vpIndex < alreadyRealisedElements.size(); vpIndex++){
			NLGElement alreadyRealisedElement = alreadyRealisedElements.get(vpIndex);
			if(alreadyRealisedElement.getCategory().equalTo(PhraseCategory.VERB_PHRASE)){
				List<NLGElement> vpComponents = alreadyRealisedElement.getChildren();
				for(int vIndex = 0; vIndex < vpComponents.size(); vIndex++){
					NLGElement vpComponent = vpComponents.get(vIndex);
					if(vpComponent.getCategory().equalTo(LexicalCategory.VERB)){
						//Add the realisedSubject directly after the first verb in the first verb phrase
						vpComponents.add(vIndex+1,realisedSubject.getFirst());
						//alreadyRealisedElement.
						((ListElement) alreadyRealisedElement).setComponents(vpComponents);
						alreadyRealisedElements.set(vpIndex,alreadyRealisedElement);
						realisedElement.setComponents(alreadyRealisedElements);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Realises the subjects of a passive clause unless, in French,
	 * the relative phrase discourse function is subject.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	@Override
	protected void addPassiveSubjects(PhraseElement phrase,
			ListElement realisedElement,
			NLGFactory phraseFactory) {
		
		if (!phrase.hasRelativePhrase(DiscourseFunction.SUBJECT)) {
			super.addPassiveSubjects(phrase, realisedElement, phraseFactory);
		}
	}
	
	/**
	 * Realises the complements of passive clauses; also sets number, person for
	 * passive.
	 * In French, checks before that the clause doesn't have a relativised object.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 */
	@Override
	protected NLGElement addPassiveComplementsNumberPerson(
			PhraseElement phrase,
			ListElement realisedElement, NLGElement verbElement) {
		NLGElement splitVerb = null;
		if (!phrase.hasRelativePhrase(DiscourseFunction.OBJECT)) {
			splitVerb = super.addPassiveComplementsNumberPerson(
					phrase, realisedElement, verbElement);
		}
		return splitVerb;
	}

	/**
	 * Realises the preposition of the interrogative.
	 *
	 * @param preposition
	 *            the key word NLGElement of the interrogative.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	protected void realiseInterrogativePreposition(NLGElement preposition,
											   ListElement realisedElement, NLGFactory phraseFactory) {

		if (preposition != null) {
			NLGElement currentElement = preposition.realiseSyntax();
			if (currentElement != null) {
				realisedElement.addComponent(currentElement);
			}
		}
	}

	/**
	 * This is the main controlling method for handling interrogative clauses.
	 * The actual steps taken are dependent on the type of question being asked.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 * @return an <code>NLGElement</code> representing a subject that should
	 *         split the verb
	 */
	@Override
	protected NLGElement realiseInterrogative(
			PhraseElement phrase, ListElement realisedElement,
			NLGFactory phraseFactory, NLGElement verbElement) {
		NLGElement splitVerb = null;

		if (phrase.getParent() != null) {
			phrase.getParent().setFeature(InternalFeature.INTERROGATIVE, true);
		}
		Object type = phrase.getFeature(Feature.INTERROGATIVE_TYPE);

		if (type instanceof InterrogativeType) {
			switch ((InterrogativeType) type) {
				case YES_NO:
					break;

				case HOW:
					realiseInterrogativeKeyWord("hoe", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHY:
					realiseInterrogativeKeyWord("waarom", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHERE:
					realiseInterrogativeKeyWord("waar", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case HOW_MANY:
					realiseInterrogativeKeyWord("hoeveel", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHO_SUBJECT:
					realiseInterrogativeKeyWord("wie", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHO_OBJECT:
					realiseInterrogativeKeyWord("wie", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHO_INDIRECT_OBJECT:
					SPhraseSpec s = ((SPhraseSpec) phrase);
					Object indirectObject = s.getIndirectObject();
					NLGElement preposition = null;

					if (indirectObject instanceof PPPhraseSpec) {
						preposition = ((PPPhraseSpec) indirectObject).getPreposition();
					}
					else{
						preposition = phraseFactory.createPrepositionPhrase("aan");
					}
					realiseInterrogativePreposition(preposition, realisedElement, //$NON-NLS-1$
							phraseFactory);
					realiseInterrogativeKeyWord("wie", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;

				case WHAT_OBJECT:
					realiseInterrogativeKeyWord("wat", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
//				Complex case where the preposition has to go at the back of the sentence, use WHY or HOW_COME
//				case WHAT_FOR:
//					realiseInterrogativeKeyWord("waar", realisedElement, //$NON-NLS-1$
//							phraseFactory);
//					PPPhraseSpec voor = phraseFactory.createPrepositionPhrase("voor");
//					realiseInterrogativePreposition(voor,realisedElement,phraseFactory);
//					break;
				case WHEN:
					realiseInterrogativeKeyWord("wanneer", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				case WHICH:
					realiseInterrogativeKeyWord("welke", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				case WHOSE:
					realiseInterrogativeKeyWord("van", realisedElement, //$NON-NLS-1$
							phraseFactory);
					realiseInterrogativeKeyWord("wie", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				case HOW_CONDITION_QUALITY:
					realiseInterrogativeKeyWord("hoe", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				case HOW_ADJECTIVE:
					realiseInterrogativeKeyWord("hoe", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				case HOW_COME:
					realiseInterrogativeKeyWord("hoezo", realisedElement, //$NON-NLS-1$
							phraseFactory);
					break;
				default:
					break;
			}
		}
		return splitVerb;
	}

	/**
	 * The method for adding an adjective to a HOW_ADJECTIVE question
	 * @param adjective, the adjective to add
	 * @param realisedElement, an element to add to the sentence
	 * @param phraseFactory, the NLG factory
	 */
	public void realiseInterrogativeAdjective(NLGElement adjective, ListElement realisedElement, NLGFactory phraseFactory){
		if (adjective != null) {
			NLGElement currentElement = adjective.realiseSyntax();
			if (currentElement != null) {
				realisedElement.addComponent(currentElement);
			}
		}
	}

	/**
	 * The method for adding a noun to a WHICH question
	 * @param noun, the noun to add
	 * @param realisedElement, an element to add to the sentence
	 * @param phraseFactory, the NLG factory
	 */
	public void realiseInterrogativeNoun(NLGElement noun, ListElement realisedElement, NLGFactory phraseFactory){
		if (noun != null) {
			NLGElement currentElement = noun.realiseSyntax();
			if (currentElement != null) {
				realisedElement.addComponent(currentElement);
			}
		}
	}

	/**
	 * The main method for controlling the syntax realisation of clauses.
	 * The French version takes care of interrogative clauses, using the result
	 * of its superclass as a base.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representation of the clause.
	 * @return the <code>NLGElement</code> representing the realised clause.
	 */
	@Override
	public NLGElement realise(PhraseElement phrase) {
		ListElement realisedElement = null;
		NLGFactory phraseFactory = phrase.getFactory();
		NLGElement splitVerb = null;

		if (phrase != null) {
			// vaudrypl added phrase argument to ListElement constructor
			// to copy all features from the PhraseElement
			realisedElement = new ListElement(phrase);
			Object interrogativeType = phrase.getFeature(Feature.INTERROGATIVE_TYPE);

			NLGElement verbElement = phrase
					.getFeatureAsElement(InternalFeature.VERB_PHRASE);

			if (verbElement == null) {
				verbElement = phrase.getHead();
			}

			checkClausalSubjects(phrase);
			checkSubjectNumberPerson(phrase, verbElement);
			checkDiscourseFunction(phrase);
			copyFrontModifiers(phrase, verbElement);
			addComplementiser(phrase, realisedElement);
			addCuePhrase(phrase, realisedElement);
			if (phrase.hasFeature(Feature.INTERROGATIVE_TYPE)) {
				addInterrogativeSpecifier(phrase, realisedElement);

				if(interrogativeType.equals(InterrogativeType.WHO_SUBJECT)){
					verbElement.setFeature(Feature.PERSON,Person.THIRD);
				}
				splitVerb = realiseInterrogative(phrase,
						realisedElement, phraseFactory, verbElement);
			} else {
				phrase.getPhraseHelper()
						.realiseList(
								realisedElement,
								phrase.getFeatureAsElementList(InternalFeature.FRONT_MODIFIERS),
								DiscourseFunction.FRONT_MODIFIER);
			}
			if (interrogativeType == null)
				addSubjectsToFront(phrase, realisedElement, splitVerb);

			NLGElement passiveSplitVerb = addPassiveComplementsNumberPerson(
					phrase, realisedElement, verbElement);

			if (passiveSplitVerb != null)
				splitVerb = passiveSplitVerb;

			if (((SPhraseSpec) phrase).getVerb() != null){
				realiseVerb(phrase, realisedElement, splitVerb, verbElement);
				//For these types of interrogatives, search for the object, remove it and put it in second position.
				if(interrogativeType instanceof InterrogativeType
					&& (interrogativeType.equals(InterrogativeType.HOW_ADJECTIVE)
					|| interrogativeType.equals(InterrogativeType.WHICH)
					|| interrogativeType.equals(InterrogativeType.HOW_MANY))){
					addObjectBeforeVerb(realisedElement);
				}

			}

			//In these types of interrogatives, the subject is after the verb
			if (interrogativeType instanceof InterrogativeType
					&& interrogativeType != InterrogativeType.WHO_INDIRECT_OBJECT
					&& interrogativeType != InterrogativeType.WHY
					&& interrogativeType != InterrogativeType.WHERE
					&& interrogativeType != InterrogativeType.WHO_SUBJECT
			)
				addSubjectsToFront(phrase, realisedElement, splitVerb);

			addPassiveSubjects(phrase, realisedElement, phraseFactory);
		}
		return realisedElement;
	}

	/**
	 * This method moves adjectives in object position before the verb, which is useful
	 * in interrogative HOW_ADJECTIVE
	 * @param realisedElement
	 */
	private void addObjectBeforeVerb(ListElement realisedElement) {
		List<NLGElement> alreadyRealisedElements = realisedElement.getChildren();
		for(int vpIndex = 0; vpIndex < alreadyRealisedElements.size(); vpIndex++){
			NLGElement alreadyRealisedElement = alreadyRealisedElements.get(vpIndex);
			if(alreadyRealisedElement.getCategory().equalTo(PhraseCategory.VERB_PHRASE)){
				List<NLGElement> vpComponents = alreadyRealisedElement.getChildren();
				for(int vIndex = 0; vIndex < vpComponents.size(); vIndex++){
					NLGElement vpComponent = vpComponents.get(vIndex);
					if(vpComponent.hasFeature(InternalFeature.DISCOURSE_FUNCTION) && vpComponent.getFeature(InternalFeature.DISCOURSE_FUNCTION).equals(DiscourseFunction.OBJECT)){
						//We add the adjective phrase to the front of the VP components, because in the
						// HOW_ADJECTIVE, this comes always before the verb in interrogatives.
						vpComponents.add(0,vpComponents.remove(vIndex));

						((ListElement) alreadyRealisedElement).setComponents(vpComponents);
						alreadyRealisedElements.set(vpIndex,alreadyRealisedElement);
						realisedElement.setComponents(alreadyRealisedElements);
						return;
					}
				}
			}
		}
	}


	/**
	 * Create a copy of a noun phrase or coordinated noun phrases
	 * and changes the specifier on the copy.
	 * 
	 * @param nounPhrase
	 * @param specifier
	 * @return
	 */
	protected NLGElement changeSpecifier(NLGElement nounPhrase, NLGElement specifier) {
		NLGElement modifiedElement = nounPhrase;
		
		if (nounPhrase instanceof NPPhraseSpec) {
			modifiedElement = new NPPhraseSpec((NPPhraseSpec) nounPhrase);
			((NPPhraseSpec) modifiedElement).setSpecifier(specifier);
			
		} else if (nounPhrase instanceof CoordinatedPhraseElement) {
			modifiedElement = new CoordinatedPhraseElement((CoordinatedPhraseElement) nounPhrase);
			
			List<NLGElement> coordinates = modifiedElement.getFeatureAsElementList(InternalFeature.COORDINATES);
			List<NLGElement> modifiedCoordinates = new ArrayList<NLGElement>();
			for (NLGElement element : coordinates) {
				modifiedCoordinates.add( changeSpecifier(element, specifier) );
			}
			modifiedElement.setFeature(InternalFeature.COORDINATES, modifiedCoordinates);
		}
		
		return modifiedElement;
	}
}

