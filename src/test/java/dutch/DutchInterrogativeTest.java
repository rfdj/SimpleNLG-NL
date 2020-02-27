package dutch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import simplenlg.features.Feature;
import simplenlg.features.InterrogativeType;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.*;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

public class DutchInterrogativeTest {

    final private static Lexicon lexicon_nl = new simplenlg.lexicon.dutch.XMLLexicon();
    final private static NLGFactory factory_nl = new NLGFactory(lexicon_nl);
    final private static Realiser realiser_nl = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicInterrogativeDirectWhat(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");

        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Wat denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeIndirectWho(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        clause.addComplement("soms");
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_INDIRECT_OBJECT);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Aan wie denk jij soms?"));
    }

    @Test
    public void basicInterrogativeWhy(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHY);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Waarom denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeWhere(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHERE);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Waar denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeWhen(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHEN);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Wanneer denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeYesNo(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeHowMany(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setObject("computers");
        clause.setVerb("geven");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("aan");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_MANY);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Hoeveel computers geef jij aan Jan?"));
    }

    @Test
    public void basicInterrogativeDirectWho(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("presenteren");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("aan");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_OBJECT);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Wie presenteer jij aan Jan?"));
    }

    @Test
    public void basicInterrogativeWho(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("aan");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Wie denkt aan Jan?"));
    }

    @Test
    public void basicInterrogativeHow(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Hoe denk jij over Jan?"));
    }

    @Test
    public void basicInterrogativeWhich(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setObject("gedachten");
        clause.setVerb("hebben");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHICH);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Welke gedachten heb jij over Jan?"));
    }

    @Test
    public void basicInterrogativeHowCondition(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("jouw vakantie in Parijs");
        clause.setSubject(subject);

        VPPhraseSpec verb = factory_nl.createVerbPhrase();
        verb.setVerb("zijn");
        clause.setFeature(Feature.TENSE, Tense.PAST);
        clause.setVerb(verb);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_CONDITION_QUALITY);

        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Hoe was jouw vakantie in Parijs?"));
    }

    @Test
    public void basicInterrogativeHowAdjective(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("Jan");
        clause.setSubject(subject);

        VPPhraseSpec verb = factory_nl.createVerbPhrase();
        verb.setVerb("zijn");
        clause.setVerb(verb);

        AdjPhraseSpec adjective = factory_nl.createAdjectivePhrase();
        adjective.setAdjective("slim");

        clause.setObject(adjective);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_ADJECTIVE);

        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Hoe slim is Jan?"));


    }

    @Test
    public void basicInterrogativeHowCome(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("denk");
        PPPhraseSpec pp = factory_nl.createPrepositionPhrase();
        pp.setObject("Jan");
        pp.setPreposition("over");
        clause.addComplement(pp);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_COME);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Hoezo denk jij over Jan?"));
    }


}
