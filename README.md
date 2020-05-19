# rye

Clojure error handling

Currently only implements `try-let` which keeps the bindings even in the `catch` and `finally` clauses.<sup>&ast;</sup><br>
<sub>&ast; Not necessarily a good idea</sub>

## Usage

```clojure
(try-let [a (create-a)
          b (create-b a)]
  (create-c b)
  (catch Exception e
    (when b (close b))
    (when a (close a))
    (throw e))
  (finally
    (println "a:" a "b:" b)))
```

See tests for more examples.

Internally implements clojure.lang.IExceptionInfo that doesn't write the stacktrace,
which is a part from [fmnoise/flow](https://github.com/fmnoise)

## License

Copyright © 2020 Yonatan Elhanan

Distributed under the MIT License
