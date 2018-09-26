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

package simplenlg.features.dutch;

/**
 * Extension of the lexical features constants for Dutch.
 * 
 * @author vaudrypl, rfdj
 *
 */
public abstract class DutchLexicalFeature {

	/**
	 * <p>
	 * This feature gives the noun of the opposite gender corresponding to a noun.
	 * For example, the feminine of <em>chien</em> is <em>chienne</em>.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>opposite_gender</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly form the
	 * noun of the opposite gender corresponding to a noun. This feature
	 * will be looked at first before any reference to lexicons or morphology
	 * rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Nouns.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String OPPOSITE_GENDER = "opposite_gender";

	/**
	 * <p>
	 * This feature gives the feminine singular form of a determiner or adjective.
	 * For example, the feminine singular of
	 * <em>le</em> is <em>la</em> and the feminin of <em>beau</em> is
	 * <em>belle</em>.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>feminine_singular</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * determiners and adjectives. This feature will be looked at first before
	 * any reference to lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Determiners and adjectives.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String FEMININE_SINGULAR = "feminine_singular";

	/**
	 * <p>
	 * This feature gives the feminin plural form of an adjective.
	 * For example, the feminine plural of
	 *  <em>beau</em> is <em>belles</em>.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>feminine_plural</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * determiners and adjectives. This feature will be looked at first before
	 * any reference to lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Adjectives.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String FEMININE_PLURAL = "feminine_plural";

	/**
	 * <p>
	 * This feature gives the form a masculine singular adjective takes
	 * when placed in front of a word beginning with a vowel or a so-called mute 'h'
	 * (not a so-called aspired 'h') For example the form of <em>beau</em> in front
	 * of a vowel is <em>bel</em>.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>liaison</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * adjectives. This feature will be looked at first before
	 * any reference to lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Adjectives only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String LIAISON = "liaison";

	/**
	 * <p>
	 * This flag determines if a word begins with a so-called aspired 'h'.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>aspired_h</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from Lexicons that support this feature.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphophonology methods.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Many categories.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String ASPIRED_H = "aspired_h";
	
	/**
	 * <p>
	 * This flag determines if the comma must be ommited before a coordination conjunction
	 * or after a front modifier.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>no_comma</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from Lexicons that support this feature
	 * and can be set by the user.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The orthography methods.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Conjunctions and word that are or can be front modifiers.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String NO_COMMA = "no_comma";
	
	/**
	 * <p>
	 * This flag determines if the coordination conjunction must be repeated before each
	 * coordinate.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>repeated_conjunction</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from Lexicons that support this feature
	 * and can be set by the user.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The orthography methods.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Conjunctions.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String REPEATED_CONJUNCTION = "repeated_conjunction";
	
	/**
	 * <p>
	 * This flag determines if an adjective is placed before the noun by default,
	 * when added to a noun phrase with addModifier(...). ("antéposé")
	 * Example : "un beau chien" (preposed) vs "un chien élancé" (postposed)
	 * Most adjectives in French are postposed, but preposed adjectives are used frequently.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>preposed</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from the lexicon and can be changed by the user.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>addModifier(...)</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Adjectives.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String PREPOSED = "preposed";
	
	/**
	 * <p>
	 * This flag determines if a verb takes "zijn" as auxiliary instead of "hebben".
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>auxiliary_zijn</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from Lexicons that support this feature.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The syntax processing methods.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String AUXILIARY_ZIJN = "auxiliary_zijn";
	
	/**
	 * <p>
	 * This flag determines if a verb can be used as a copula.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>copular</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>Boolean</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The information is read from Lexicons that support this feature.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The syntax processing methods.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>Boolean.FALSE</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String COPULAR = "copular";
	
	/**
	 * <p>
	 * These features give the indicative present form of a verb.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>present (person) (number)</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String PRESENT1S = "present1s";
	public static final String PRESENT2S = "present2s";
	public static final String PRESENT3S = "present3s";
	public static final String PRESENT1P = "present1p";
	public static final String PRESENT2P = "present2p";
	public static final String PRESENT3P = "present3p";

	/**
	 * <p>
	 * These features give the indicative past form of a verb.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>past (person) (number)</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String PAST1S = "past1s";
	public static final String PAST2S = "past2s";
	public static final String PAST3S = "past3s";
	public static final String PAST1P = "past1p";
	public static final String PAST2P = "past2p";
	public static final String PAST3P = "past3p";

	/**
	 * <p>
	 * These features give the imperative present form of a verb.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>imperative (person) (number)</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String IMPERATIVE2S = "imperative2s";
	public static final String IMPERATIVE1P = "imperative1p";
	public static final String IMPERATIVE2P = "imperative2p";


	/**
	 * <p>
	 * This feature gives the feminine past participle tense form of a verb.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>femininePastParticiple</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String FEMININE_PAST_PARTICIPLE = "feminine_past_participle";
	
	/**
	 * <p>
	 * This feature determines of what type is a pronoun.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>pronoun_type</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>PronounType</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>The lexicon.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processing methods uses pronoun type to determine the appropriate
	 * form for pronouns.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Pronouns.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code></td>
	 * </tr>
	 * </table>
	 */
	public static final String PRONOUN_TYPE = "pronoun_type";

	/**
	 * <p>
	 * These features give the subjunctive present form of a verb.
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>subjunctive (person) (number)</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons but can be set by the user for irregular
	 * cases.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String SUBJUNCTIVE1S = "subjunctive1s";
	public static final String SUBJUNCTIVE2S = "subjunctive2s";
	public static final String SUBJUNCTIVE3S = "subjunctive3s";
	public static final String SUBJUNCTIVE1P = "subjunctive1p";
	public static final String SUBJUNCTIVE2P = "subjunctive2p";
	public static final String SUBJUNCTIVE3P = "subjunctive3p";

	/**
	 * <p>
	 * This feature can be used to set the preverb of a separable complex verb
	 * </p>
	 * <table border="1">
	 * <tr>
	 * <td><b>Feature name</b></td>
	 * <td><em>preverb</em></td>
	 * </tr>
	 * <tr>
	 * <td><b>Expected type</b></td>
	 * <td><code>String</code></td>
	 * </tr>
	 * <tr>
	 * <td><b>Created by</b></td>
	 * <td>All supporting lexicons.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Used by</b></td>
	 * <td>The morphology processor uses this feature to correctly inflect
	 * verbs. This feature will be looked at first before any reference to
	 * lexicons or morphology rules.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Applies to</b></td>
	 * <td>Verbs only.</td>
	 * </tr>
	 * <tr>
	 * <td><b>Default</b></td>
	 * <td><code>null</code>.</td>
	 * </tr>
	 * </table>
	 */
	public static final String PREVERB = "preverb";
}
