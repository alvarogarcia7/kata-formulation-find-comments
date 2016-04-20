# Find the comments kata

This a formulation for your practice

## Formulation

Little Johnny is inspecting a PHP source code that contains comments. These comments contains words that he doesn't understand, as they are written in another language.

### Rules and constraints

The source code in the PHP files does not need to be correct.

A comment (a subset of PHP comments) is defined as:

  * A line containing C-style comment delimiter, except when it is within a string
    * ``// hello C-style`` is a valid comment
    * ``echo "//";`` is not a comment
    * ``echo '//';`` is not a comment
  * A line containing Perl-style comment delimiter, except when it is within a string
    * ``# hello Perl-style`` is a valid comment
    * ``echo "#";`` is not a comment
    * ``echo '#';`` is not a comment
  * There are no multi-line comments
    * ``/* ... */`` is not a comment

You can do this kata in any language you want. In any case it is not allowed to use a PHP parser. This task must be done manually.

### How to start

  * Clone/fork (any stars are welcome) [this repo][formulation]
  * Run the tests. [See this][running-tests] for help
  * They should be red.
  * Go to production code and fix it
  * Have fun (happy kata and happy koding!)

### Requirements

Please do not read ahead, just read the current assignment, do it, then read the next one:

  1. Can you help Little Johnny fetch all those comments?
  1. These messages have not been audited yet and we want to publish the code, so it is better to remove the comments. Can you do it?

### Optional requirement

As an optional task, when the problem is finished (you can attack in your preferred order), there must be an executable jar with some parameters to process the current folder with the 'working modes' specified above.


[formulation]: https://github.com/alvarogarcia7/kata-formulation-find-comments
[running-tests]: https://github.com/alvarogarcia7/cli-app-base-clojure/blob/master/README.md#tests



This section is a copy [from here](http://alvarogarcia7.github.io/blog/2016/03/01/kata-formulation-find-comments/). [Raw source](https://github.com/alvarogarcia7/blog_source/blob/source/source/_posts/2016-03-01-kata-formulation-find-comments.markdown)

## Tests

This section is a copy [from here](https://github.com/alvarogarcia7/cli-app-base-clojure/blob/master/README.md#tests)

### Testing from the CLI

``lein midje :autotest``

This has the advantage that loads everything, each time.

### Testing inside the REPL

```bash
lein repl
```

```clojure
(use 'midje.repl)
(autotest)
```

This has the advantage that is faster.
