import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

public class BasicTest {

    final private static Lexicon lexicon = new simplenlg.lexicon.english.XMLLexicon();
    final private static NLGFactory factory = new NLGFactory(lexicon);
    final private static simplenlg.realiser.Realiser realiser = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicVerbInflection() {
        SPhraseSpec clause = factory.createClause();

        clause.setSubject("Julia");
        clause.setVerb("want");
        clause.addComplement("to dance");

        collector.checkThat(realiser.realiseSentence(clause), equalTo("Julia wants to dance."));
    }
}
