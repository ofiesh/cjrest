(ns cjrest.url
  (:require [clojure.string :refer [replace-first]]))

(defn build-url
  "adds params to a base-url by adding the param as a path and keyword
e.g. /foobar & :id :name -> /foobar/id/:id/name/:name"
  [base-url & params]
  (str
   base-url
   (reduce
    str
    (map
     #(str "/" (name %) "/" (str %))
     params))))

(defn replace-path-params
  "for {:id 1} /foobar/id/:id -> /foobar/id/1"
  [url params]
  (reduce
   #(replace-first
     %1
     (-> %2 key str re-pattern)
     (str (val %2)))
   url
   params))
