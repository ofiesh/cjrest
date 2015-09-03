(ns cjrest.core-test
  (:require [clojure.test :refer :all]
            [cjrest.core :as c]))

(def foo "http://google.com")



(macroexpand '(c/defservice foo
               (foo
                (GET get-food :id))))

(macroexpand '(c/rest-method get-food foo foo GET :id))
