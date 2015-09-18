(ns cjrest.route
  (:require [compojure.core :as compojure])
  (:refer-clojure :exclude [defmethod]))

(defmacro defrequest
  [method]
  `(defmacro ~method
     [path params]
     (list ~name path params)))

(def
    ^{:arglists '([a])
      :doc "hooo"
      :static true}
 foo

 (fn [x] (println x)))
