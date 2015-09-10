(ns cjrest.service
  (:require [compojure.core :refer [GET POST DELETE PUT]]
            [clj-http.client :as client]
            [cjrest.client :as c]))

(def request-methods
  {:get (fn [url func] (GET url [] func))
   :post (fn [url func] (POST url [] func))
   :delete (fn [url func] (DELETE url [] func))
   :put (fn [url func] (PUT url [] func))})

(defn consume-endpoint
  [endpoint func]
  (let [endpoint-meta (meta endpoint)]
    (fn [req]
      (let [params (:params req)]
        (apply func (map #(% params) (:params endpoint-meta)))))))



(def json "http://jsonplaceholder.typicode.com")

(c/defresource post #'json)

(post
 (c/GET v [id bar]))

(def v2 (consume-endpoint v #(println %1 %2)))



