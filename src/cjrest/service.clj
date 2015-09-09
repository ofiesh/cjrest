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
        (println (:params endpoint-meta))
        (println (map #(% params) (:params endpoint-meta)))
        (apply func (map #(% params) (:params endpoint-meta)))))))



(def json "http://jsonplaceholder.typicode.com/")

(c/service #'json
           (c/resource :post
                    (c/GET get-post [id name])
                    (c/POST save-post [])))

(def v (consume-endpoint get-post #(println %1 %2)))


(defmacro ho [] `(println ~&env))
