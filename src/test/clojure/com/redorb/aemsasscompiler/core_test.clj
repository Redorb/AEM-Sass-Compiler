(ns com.redorb.aemsasscompiler.core-test
  (:require [clojure.test :refer :all]
            [com.redorb.aemsasscompiler.core :refer :all]
            [com.redorb.aemsasscompiler.sass :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest test-compiler
  (is (= ".selector {\n  margin: 10px; }\n  .selector .nested {\n    margin: 5px; }\n"
         (compile-file "src/test/clojure/com/redorb/testing-resources/test.sass"))))

