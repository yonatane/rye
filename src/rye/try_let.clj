(ns rye.try-let
  (:require [rye.parse-try-body :refer [parse-try-body]])
  (:import (rye.jump Jump)))

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
                         (throw (Jump. {::binding-vals ~(vec binding-forms)} e#)))))
        try-inits (map (comp try-exprs list second) bindings)
        try-bindings (interleave binding-forms try-inits)]
    `(try
       (let [~@nil-bindings
             ~@try-bindings]
         (try
           ~@expressions
           ~@catches
           ~@(when finally (list finally))))
       (catch Jump jump#
         (let [jump-data# (ex-data jump#)
               [~@binding-forms] (::binding-vals jump-data#)]
           (try
             (throw (ex-cause jump#))
             ~@catches
             ~@(when finally (list finally))))))))
