package english;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import simplenlg.features.*;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.*;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test class for all 16 supported WH-questions and YESNO in English
 * WHO, WHO_DIRECT_OBJECT, WHO_INDIRECT_OBJECT, WHAT, WHAT_DIRECT_OBJECT, HOW, HOW_PREDICATE, HOW_COME, HOW_MANY, HOW_ADJECTIVE, WHY, WHERE, WHEN, WHICH, WHOSE, YESNO
 */
public class EnglishInterrogativeTest {

    final private static Lexicon lexicon = new simplenlg.lexicon.english.XMLLexicon();
    final private static NLGFactory factory = new NLGFactory(lexicon);
    final private static Realiser realiser = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicInterrogativeDirectObjectWhat(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");

        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("What do you think about John?"));
    }

    @Test
    public void basicInterrogativeIndirectObjectWho(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        VPPhraseSpec verb = factory.createVerbPhrase();
        verb.setVerb("present");
        clause.setVerb(verb);
        clause.setObject("John");
        clause.setIndirectObject("Mary");
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_INDIRECT_OBJECT);
        clause.setFeature(Feature.TENSE, Tense.FUTURE);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Whom will you present John to?"));
    }

    @Test
    public void basicInterrogativeWhy(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHY);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Why do you think about John?"));
    }

    @Test
    public void basicInterrogativeWhere(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHERE);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Where do you think about John?"));
    }

    @Test
    public void basicInterrogativeWhen(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHEN);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("When do you think about John?"));
    }

    @Test
    public void basicInterrogativeYesNo(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Do you think about John?"));
    }

    @Test
    public void basicInterrogativeHowMany(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        NPPhraseSpec object = factory.createNounPhrase("DEVICE");
        object.setFeature(LexicalFeature.PLURAL,true);
        object.setNoun("computer");
        clause.setObject(object);
        clause.setVerb("give");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("to");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_MANY);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("How many computers do you give to John?"));
    }

    @Test
    public void basicInterrogativeDirectObjectWho(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("present");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("to");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_OBJECT);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Whom do you present to John?"));
    }

    @Test
    public void basicInterrogativeWho(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Who thinks about John?"));
    }

    @Test
    public void basicInterrogativeHow(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("How do you think about John?"));
    }

    @Test
    public void basicInterrogativeWhich(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setObject("thoughts");
        clause.setVerb("have");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHICH);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Which thoughts do you have about John?"));
    }

    @Test
    public void basicInterrogativeHowPredicate(){
        SPhraseSpec clause = factory.createClause();
        PPPhraseSpec preposition = factory.createPrepositionPhrase();
        preposition.setPreposition("in");
        preposition.setObject("Paris");
        NPPhraseSpec subject = factory.createNounPhrase();
        subject.setNoun("holiday");
        subject.setSpecifier("your");
        subject.addComplement(preposition);
        clause.setObject(subject);

        VPPhraseSpec verb = factory.createVerbPhrase();
        verb.setVerb("be");
        clause.setVerb(verb);
        clause.setFeature(Feature.TENSE, Tense.PAST);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_PREDICATE);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("How was your holiday in Paris?"));
    }

    @Test
    public void basicInterrogativeHowAdjective(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("John");
        clause.setSubject(subject);

        VPPhraseSpec verb = factory.createVerbPhrase();
        verb.setVerb("are");

        AdjPhraseSpec adjective = factory.createAdjectivePhrase();
        adjective.setAdjective("smart");
        verb.setComplement(adjective);

        clause.setVerb(verb);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_ADJECTIVE);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("How smart is John?"));
    }

    @Test
    public void basicInterrogativeHowCome(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("think");
        PPPhraseSpec pp = factory.createPrepositionPhrase();
        pp.setObject("John");
        pp.setPreposition("about");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_COME);
        clause.setFeature(Feature.PERFECT,true);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("How come you have thought about John?"));
    }

    @Test
    public void basicInterrogativeWhat(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("motivate");
        clause.setObject("John");
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_SUBJECT);
        clause.setFeature(Feature.TENSE, Tense.FUTURE);
        clause.setFeature(Feature.PERFECT,true);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("What will have motivated John?"));
    }

    @Test
    public void basicInterrogativeWhose(){
        SPhraseSpec clause = factory.createClause();
        NPPhraseSpec subject = factory.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("get");
        clause.setObject("keys");
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHOSE);
        clause.setFeature(Feature.PERFECT,true);
        String output = realiser.realiseSentence(clause);
        collector.checkThat(output, equalTo("Whose keys have you gotten?"));
    }

}
