(ns cjrest.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.string :refer [replace-first
                                    lower-case
                                    upper-case]]
            [compojure.core :as compojure]))

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

(defn bodyless-request
  [service path params fn]
  (json/read-str
   (:body (fn (str service (build-url path params))
            :key-fn)) keyword))

(defn GET
  [service path params]
  (bodyless-request service path params client/get))

(defn DELETE
  [service path params]
  (bodyless-request service path params client/delete))

(defn POST
  [service path body]
  (json/read-str
   (:body
    (client/post
     (str service (build-url path nil))
     {:body (json/write-str body)
      :content-type "application/json"}))
   :key-fn keyword))

(defmacro rest-method-wrapper
  [fun & params]
  `(fn bar [~@(map #(symbol (name %)) params)]
     (~fun
      ~(into {}  ( map (fn [param] {param (symbol (name param))}) params)))))

(defmacro rest-method
  [v service resource type & params]
  `(let [path# (build-path ~(str resource) ~@params)
         method# ~(-> type lower-case keyword)]
     (def ~v
       (with-meta
         (rest-method-wrapper
          (fn foo [p#] (~type ~service path# p#))
          ~@params)
         {:path path#
          :params ~(vec params)
          :method method#}))))

(defmacro post-rest-method
  [v service resource]
  (let [path (build-path (str resource))]
    `(def ~v
       (with-meta
         #(POST ~service %)
         {:path ~path
          :params nil
          :method :post}))))

(defmacro defservice
  [service & resources]
  `(do
     ~@(for [resource resources
             method (rest resource)]
         (case (first method)
           (DELETE GET)
           `(rest-method
             ~(second method)
             ~service
             ~(first resource)
             ~(first method)
             ~@(drop 2 method))
           POST
           `(post-rest-method
             ~(second method) ~service ~(first resource))
))))

(defservice food-web
  (food
   (DELETE delete-food :id)
   (POST save-food)
   (GET get-food :id :status) 
   (GET get-food-name :name-like)))

(defmacro consume-meta
  [meta fn]
  (println meta fn))

(defmacro consume-endpoint
  [endpoint fn]
  (let [meta (meta @(resolve endpoint))]
    (:method meta)))

(defmacro consume-meta [method fn]
  (println method)
)

(macroexpand '(consume-endpoint
               get-food #(%)))
