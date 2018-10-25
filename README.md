# SimpleNLG-NL

SimpleNLG-NL is a Dutch surface realiser used for Natural Language Generation in Dutch. It is based on version 1.1 of the bilingual [SimpelNLG-EnFr](https://github.com/rali-udem/SimpleNLG-EnFr). With that basis, it can be used for all three languages: English, French and Dutch.

The original [SimpleNLG](https://github.com/simplenlg/simplenlg) is a Java library originally developed by Ehud Reiter, Albert Gatt and Dave Westwater, of the University of Aberdeen.

The Dutch version contains multiple lexicons based on [Wiktionary data](https://dumps.wikimedia.org/nlwiktionary/20180901/). The largest lexicon has 79.438 entries. The default lexicon is reduced to 8601 words matched with the top 10.000 most common words from a [word frequency list](https://github.com/hermitdave/FrequencyWords/blob/master/content/2016/nl/nl_full.txt). An even smaller lexicon of 3387 entries is also provided.

SimpleNLG-NL was developed as part of the master's thesis of Ruud de Jong. The thesis describing the process can be found at the [theses repository of Twente University](https://essay.utwente.nl/76411/).

## Usage
The API is intentionally kept close to that of SimpleNLG-EnFr, which in turn is based on SimpleNLG. 

A basic tutorial can be found in the [wiki for SimpleNLG-NL](https://github.com/rfdj/SimpleNLG-NL/wiki) (based on the [SimpleNLG wiki](https://github.com/simplenlg/simplenlg/wiki)).

One noteworthy addition is the ```DutchFeature.PREVERB``` feature. [Separable Complex Verbs](http://www.taalportaal.org/taalportaal/topic/pid/topic-13998813296768009) (SCVs) can be split into a preverb and a main verb (e.g. _vrijkomen_ is split into _vrij_ and _komen_). SimpleNLG-NL tries to detect SCVs, but in case it is unsuccessful, the user can set the feature on the verb or add a pipe in the verb input string, e.g. ```factory.createVerbPhrase("vrij|komen")```.

## License
SimpleNLG-NL is licensed under the [MPL](https://www.mozilla.org/en-US/MPL/). The Dutch lexicons are based on data from [Wiktionary.org](https://wiktionary.org), which is licensed under the [GNU Free Documentation License](https://www.wikipedia.org/wiki/Wikipedia:Copyrights) and the [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/).