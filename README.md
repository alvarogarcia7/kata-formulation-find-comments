# Find the comments kata

This a formulation for your practice

## Tests

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