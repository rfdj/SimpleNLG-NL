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
        realiser_nl.setDebugMode(true);

        SPhraseSpec clause = factory_en.createClause("you", "think");
        PPPhraseSpec aboutJohn = factory_en.createPrepositionPhrase("about", "John");
        clause.addComplement(aboutJohn);
        clause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        collector.checkThat(realiser_en.realiseSentence(clause), equalTo("What do you think about John?"));

        SPhraseSpec clause2 = factory_fr.createClause("tu", "penser");
        PPPhraseSpec aboutJean = factory_fr.createPrepositionPhrase("en", "Jean");
        clause2.addPostModifier(aboutJean);
        clause2.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        collector.checkThat(realiser_fr.realiseSentence(clause2), equalTo("Qu'est-ce que tu penses en Jean?"));

        SPhraseSpec clause3 = factory_nl.createClause();
        NPPhraseSpec subject = factory_nl.createNounPhrase("JIJ");
        PPPhraseSpec aboutJan = factory_nl.createPrepositionPhrase();
        aboutJan.setObject("Jan");
        aboutJan.setPreposition("over");

        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause3.setSubject(subject);
        clause3.setVerb("denk");
//        clause3.setObject("iets");
        clause3.addComplement(aboutJan);
        clause3.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
        String output3 = realiser_nl.realiseSentence(clause3);
        System.out.println(output3);
        collector.checkThat(output3, equalTo("Wat denk jij over Jan?"));

        SPhraseSpec clause4 = factory_nl.createClause();
        NPPhraseSpec subject2 = factory_nl.createNounPhrase("JIJ");
//        NPPhraseSpec object = factory_nl.createNounPhrase("bal");

        subject2.setFeature(Feature.PRONOMINAL, true);
        subject2.setFeature(Feature.PERSON, Person.SECOND);
        clause4.setSubject(subject);
        clause4.setVerb("gooien");
        clause4.setObject("de bal");
//        clause3.setIndirectObject(aboutJan);
        clause4.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
        String output4 = realiser_nl.realiseSentence(clause4);
        System.out.println(output4);
        collector.checkThat(output4, equalTo("Gooi jij de bal?"));


    }
}
