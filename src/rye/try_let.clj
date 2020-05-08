(ns rye.try-let
  (:require [rye.parse-try-body :refer [parse-try-body]]))

(defmacro try-let
  [bindings* & body]
  (let [bindings (partition 2 bindings*)
        binding-forms (map first bindings)
        nil-bindings (interleave binding-forms (repeat nil))
        [expressions catches finally] (parse-try-body body)
        try-exprs (fn [exprs]
                    `(try
                       ~@exprs
                       (catch Exception e#
                         (throw (ex-info "wrapper" {::binding-vals ~(vec binding-forms)} e#)))))
        try-inits (map (comp try-exprs list second) bindings)
        try-bindings (interleave binding-forms try-inits)]
    `(try
       (let [~@nil-bindings
             ~@try-bindings]
         (try
           ~@expressions
           ~@catches
           ~@(when finally (list finally))))
       (catch clojure.lang.ExceptionInfo e#
         (when-not (::binding-vals (ex-data e#))
           (throw e#))
         (let [e-data# (ex-data e#)
               [~@binding-forms] (::binding-vals e-data#)]
           (try
             (some-> (ex-cause e#) (throw))
             ~@catches
             ~@(when finally (list finally))))))))
