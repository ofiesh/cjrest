(ns cjrest.service-test
  (:require [clojure.test :refer :all]
            [cjrest.service :as s]))

(def bar "fooba")

(s/foobar s/bar)
