(ns aem-sass-compiler.core-test
  (:require [clojure.test :refer :all]
            [aem-sass-compiler.core :refer :all]
            [aem-sass-compiler.sass :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest test-compiler
  (is (= ".selector {\n  margin: 10px; }\n  .selector .nested {\n    margin: 5px; }\n"
         (compile-file "test/test.sass"))))

