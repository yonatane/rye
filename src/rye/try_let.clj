(ns rye.try-let)

; parse-body was adapted from https://github.com/scgilardi/slingshot

(defn parse-body
  [body]
  (letfn [(item-type [item]
            ({'catch :catch-clause 'finally :finally-clause}
             (and (seq? item) (first item))
             :expression))
          (match-or-defer [s type]
            (if (-> s ffirst item-type (= type)) s (cons nil s)))]
    (let [groups (partition-by item-type body)
          [e & groups] (match-or-defer groups :expression)
          [c & groups] (match-or-defer groups :catch-clause)
          [f & groups] (match-or-defer groups :finally-clause)]
      (if (every? nil? [groups (next f)])
        [e c (first f)]
        (throw (Exception. "Malformed let-try"))))))

(defmacro try-let
  [bindings* & body]
  (let [bindings (partition 2 bindings*)
        binding-forms (map first bindings)
        nil-bindings (interleave binding-forms (repeat nil))
        [expressions catches finally] (parse-body body)
        try-exprs (fn [exprs]
                    `(try
                       ~@exprs
                       (catch Exception e#
                         (throw (ex-info "wrapper" {:binding-vals ~(vec binding-forms)} e#)))))
        try-inits (map (comp try-exprs list second) bindings)
        try-bindings (interleave binding-forms try-inits)
        try-expressions (try-exprs expressions)]
    `(try
       (let [~@nil-bindings
             ~@try-bindings
             body-ret# ~(if (seq expressions) try-expressions nil)]
         (throw (ex-info "success" {:body-ret body-ret# :binding-vals ~(vec binding-forms)})))
       (catch clojure.lang.ExceptionInfo e#
         (let [e-data# (ex-data e#)
               [~@binding-forms] (:binding-vals e-data#)]
           (try
             (some-> (ex-cause e#) (throw))
             (:body-ret e-data#)
             ~@catches
             ~@(if finally (list finally) nil)))))))
