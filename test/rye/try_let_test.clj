(ns rye.try-let-test
  (:require [clojure.test :refer [deftest testing is]]
            [rye.try-let :refer [try-let]])
  (:import (clojure.lang ExceptionInfo)))

(deftest success
  (testing "nothing"
    (is (nil? (try-let []))))
  (testing "no bindings"
    (is (= 3 (try-let []
               (+ 1 2)))))
  (testing "no body"
    (is (nil? (try-let [a 1
                        b 2]))))
  (testing "no catch or finally"
    (is (= 3 (try-let [a 1
                       b 2]
               (+ a b)))))
  (testing "catch"
    (is (= 3 (try-let [a 1
                       b 2]
               (+ a b)
               (catch Exception _
                 (is false "Should not catch anything"))))))
  (testing "catches"
    (is (= 3 (try-let [a 1
                       b 2]
               (+ a b)
               (catch ExceptionInfo _
                 (is false "Should not catch anything"))
               (catch Exception _
                 (is false "Should not catch anything"))))))
  (testing "finally"
    (let [finally-executed (atom false)
          res (try-let [a 1
                        b 2]
                (+ a b)
                (finally
                  (is (= 3 (+ a b)) "Bindings are available in finally")
                  (reset! finally-executed true)))]
      (is (= 3 res))
      (is @finally-executed)))
  (testing "catch and finally"
    (let [finally-executed (atom false)]
      (is (= 3 (try-let [a 1
                         b 2]
                 (+ a b)
                 (catch Exception _
                   (is false "Should not catch anything"))
                 (finally
                   (is (= 3 (+ a b)) "Bindings are available in finally")
                   (reset! finally-executed true)))))
      (is @finally-executed)))
  (testing "catches and finally"
    (let [finally-executed (atom false)]
      (is (= 3 (try-let [a 1
                         b 2]
                 (+ a b)
                 (catch ExceptionInfo _
                   (is false "Should not catch anything"))
                 (catch Exception _
                   (is false "Should not catch anything"))
                 (finally
                   (is (= 3 (+ a b)) "Bindings are available in finally")
                   (reset! finally-executed true)))))
      (is @finally-executed)))
  (testing "catches and finally but empty body"
    (let [finally-executed (atom false)]
      (is (nil? (try-let [a 1
                          b 2]
                  (catch ExceptionInfo _
                    (is false "Should not catch anything"))
                  (catch Exception _
                    (is false "Should not catch anything"))
                  (finally
                    (is (= 3 (+ a b)) "Bindings are available in finally")
                    (reset! finally-executed true)))))
      (is @finally-executed)))
  (testing "throw from catch"
    (let [finally-executed (atom false)]
      (is (thrown-with-msg? Exception #"caught"
                            (try-let [a 1
                                      b 0]
                              (/ a b)
                              (catch ArithmeticException e
                                (throw (Exception. "caught" e)))
                              (finally
                                (is (= 1 (+ a b)) "Bindings are available in finally")
                                (reset! finally-executed true)))))
      (is @finally-executed))))

(deftest failure
  (testing "no bindings"
    (is (thrown? Exception
                 (try-let []
                   (throw (Exception.))))))
  (testing "no catch or finally"
    (is (thrown? Exception
                 (try-let [a 1
                           b 2]
                   (throw (Exception.))))))
  (testing "catch"
    (is (= :caught
           (try-let [a 1
                     b 0]
             (/ a b)
             (catch ArithmeticException _
               :caught)))))
  (testing "catches"
    (is (= :caught
           (try-let [a 1
                     b 0]
             (/ a b)
             (catch ArithmeticException _
               :caught)
             (catch Exception _
               (is false "Should not be here"))))))
  (testing "finally"
    (let [finally-executed (atom false)]
      (is (thrown? ArithmeticException
                   (try-let [a 1
                             b 0]
                     (/ a b)
                     (finally
                       (is (= 1 (+ a b)) "Bindings are available in finally")
                       (reset! finally-executed true)))))))
  (testing "catch and finally"
    (let [finally-executed (atom false)]
      (is (= :caught
             (try-let [a 1
                       b 0]
               (/ a b)
               (catch Exception _
                 :caught)
               (finally
                 (is (and (= 1 a) (= 0 b)) "Bindings are available in finally")
                 (reset! finally-executed true)))))
      (is @finally-executed)))
  (testing "catches and finally"
    (let [finally-executed (atom false)]
      (is (= :caught
             (try-let [a 1
                       b 0]
               (/ a b)
               (catch ExceptionInfo _
                 (is false "Should not catch this"))
               (catch ArithmeticException _
                 :caught)
               (catch Exception _
                 (is false "Should not catch this"))
               (finally
                 (is (and (= 1 a) (= 0 b)) "Bindings are available in finally")
                 (reset! finally-executed true)))))
      (is @finally-executed)))
  (testing "catches and finally but empty body"
    (let [finally-executed (atom false)]
      (is (= :caught
             (try-let [a 1
                       b (throw (Exception.))]
               (catch ExceptionInfo _
                 (is false "Should not catch this"))
               (catch Exception _
                 :caught)
               (finally
                 (is (and (= 1 a) (nil? b)) "Only successful bindings are available in finally")
                 (reset! finally-executed true)))))
      (is @finally-executed)))
  )
