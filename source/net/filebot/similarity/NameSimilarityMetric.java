
package net.filebot.similarity;

import static net.filebot.similarity.Normalization.*;
import static org.simmetrics.builders.StringMetricBuilder.*;
import static org.simmetrics.tokenizers.Tokenizers.*;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.BlockDistance;

import com.ibm.icu.text.Transliterator;

public class NameSimilarityMetric implements SimilarityMetric {

	private final StringMetric metric = with(new BlockDistance<String>()).tokenize(qGramWithPadding(3)).build();

	private final Transliterator transliterator = Transliterator.getInstance("Any-Latin;Latin-ASCII;[:Diacritic:]remove");

	@Override
	public float getSimilarity(Object o1, Object o2) {
		return metric.compare(normalize(o1), normalize(o2));
	}

	protected String normalize(Object object) {
		// use string representation
		String name = object.toString();

		// apply transliterator
		if (transliterator != null) {
			name = transliterator.transform(name);
		}

		// normalize separators
		name = normalizePunctuation(name);

		// normalize case and trim
		return name.toLowerCase();
	}

}
