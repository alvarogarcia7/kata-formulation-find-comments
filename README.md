# Find the comments kata

This a formulation for your practice

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