(ns com.redorb.aemsasscompiler.core-test
  (:use expectations)
  (:require [com.redorb.aemsasscompiler.core :refer :all]
            [com.redorb.aemsasscompiler.sass :refer :all]))

  (expect 1 1)
  (expect ".selector {\n  margin: 10px; }\n  .selector .nested {\n    margin: 5px; }\n"
         (compile-file "src/test/clojure/com/redorb/testing-resources/test.sass"))
