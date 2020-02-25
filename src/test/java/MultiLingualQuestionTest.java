import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import simplenlg.features.Feature;
import simplenlg.features.InterrogativeType;
import simplenlg.features.Person;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.PPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

public class MultiLingualQuestionTest {

    final private static Lexicon lexicon_fr = new simplenlg.lexicon.french.XMLLexicon();
    final private static NLGFactory factory_fr = new NLGFactory(lexicon_fr);
    final private static Realiser realiser_fr = new Realiser();

    final private static Lexicon lexicon_en = new simplenlg.lexicon.english.XMLLexicon();
    final private static NLGFactory factory_en = new NLGFactory(lexicon_en);
    final private static Realiser realiser_en = new Realiser();

    final private static Lexicon lexicon_nl = new simplenlg.lexicon.dutch.XMLLexicon();
    final private static NLGFactory factory_nl = new NLGFactory(lexicon_nl);
    final private static Realiser realiser_nl = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicWhatInterrogative(){

        SPhraseSpec clause = factory_en.createClause("you", "think");
        PPPhraseSpec aboutJohn = factory_en.createPrepositionPhrase("about", "John");
        clause.addPostModifier(aboutJohn);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        collector.checkThat(realiser_en.realiseSentence(clause), equalTo("What do you think about John?"));

        SPhraseSpec clause2 = factory_fr.createClause("tu", "penser");
        PPPhraseSpec aboutJean = factory_fr.createPrepositionPhrase("sur", "Jean");
        clause2.addPostModifier(aboutJean);
        clause2.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        collector.checkThat(realiser_fr.realiseSentence(clause2), equalTo("Qu'est-ce que tu penses sur Jean?"));

        //SPhraseSpec clause3 = factory_nl.createClause("jij", "vinden");
        SPhraseSpec clause3 = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause3.setSubject(subject);
        clause3.setVerb("doen");
        clause3.setObject("dat");
//        PPPhraseSpec aboutJan = factory_nl.createPrepositionPhrase("over", "Jan");
//        clause3.addPostModifier(aboutJan);
        clause3.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHY);
        //collector.checkThat(realiser_nl.realiseSentence(clause3), equalTo("Wat vind jij?"));
        collector.checkThat(realiser_nl.realiseSentence(clause3), equalTo("Waarom doe jij dat?"));
    }
}
