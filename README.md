# POS-Neural-Net

This goal of this assignment was to build a personal speech assistant named "Sudi."
While doing this whole thing wouldn't have been possible in the span of 2 weeks,
we did address the speech understanding part of the issue that any pattern-recognizing
algorithm would need to tackle.

This was mainly achieved by building a Hidden Markov Model, that then calculated the
logarithmic probabilisitic values of what part of speech a specific word would have been
(noun, pronoun, preposition, article, adjective, verb, etc.) given a sequence of input training sets.
I first performed Viterbi decoding to find the best sequence of tags for a given line, then trained
the model on corresponding lines. After building both a console and file-based test method for the
accuracy of the tagger, I then tested on several different files outlined in the .gitignore file.
