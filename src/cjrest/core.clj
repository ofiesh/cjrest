(ns cjrest.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.string :refer [replace-first lower-case]]))

(def food-web "http://localhost:4000")

(defn replace-map-entry
  [haystack map-entry]
  (let [key (re-pattern (str ":" (name (key map-entry))))]
    (replace-first haystack key (val map-entry))))

(defn build-url
  [url params]
  (reduce replace-map-entry url params))

(defn build-path-param
 [param]
 (let [param-str (str param)]
   (str "/" (.substring param-str 1 (count param-str))
        "/" param-str)))

(defn build-path
 [resource & params]
 (str "/" resource (reduce str (map build-path-param params))))

(defn GET
  [service path params]
  (json/read-str
   (:body (client/get (str service (build-url path params))))
                 :key-fn keyword))

(defmacro rest-method-wrapper
  [fun & params]
  `(fn bar [~@(map #(symbol (name %)) params)]
     (~fun
      ~(into {}  ( map (fn [param] {param (symbol (name param))}) params)))))

(defmacro rest-method
  [v service resource type & params]
  `(let [path# (build-path ~(str resource) ~@params)
         method# ~(-> type keyword lower-case)]
     (def ~v
       (with-meta
         (rest-method-wrapper
          (fn foo [p#] (~type ~service path# p#))
          ~@params)
         {:path path#
          :params ~(vec params)
          :method method#}))))

(defmacro defservice
  [service & resources]
  `(do
     ~@(for [resource resources
                 method (rest resource)]
         `(rest-method
           ~(second method)
           ~service
           ~(first resource)
           ~(first method)
           ~@(drop 2 method)))))

(macroexpand '(defservice food-web
               (food
                (GET get-food :id :status) 
                (GET get-food-name :name-like))))
