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
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

public class DutchDeclarativeTest {

    final private static Lexicon lexicon_nl = new simplenlg.lexicon.dutch.XMLLexicon();
    final private static NLGFactory factory_nl = new NLGFactory(lexicon_nl);
    final private static Realiser realiser_nl = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicDeclarativeWhat(){
        SPhraseSpec clause = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("motiveren");
        clause.setObject("Jan");
        clause.setFeature(Feature.TENSE, Tense.FUTURE);
//        clause.setFeature(Feature.PERFECT,true);
        String output = realiser_nl.realiseSentence(clause);
        collector.checkThat(output, equalTo("Jij zult Jan motiveren."));
    }
}
