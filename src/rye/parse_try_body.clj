(ns rye.parse-try-body)

; parse-try-body was adapted from https://github.com/scgilardi/slingshot

(defn parse-try-body
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
