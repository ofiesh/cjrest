(ns cjrest.service
  (:require [cjrest.func :refer :all]
            [cjrest.url :refer :all]
            [clj-http.client :as client]
            [clojure.string :refer [replace-first
                                    lower-case
                                    upper-case]]
            [clojure.data.json :as json]))

(def json "http://jsonplaceholder.typicode.com")

(defn request
  [url & {:keys [method body]
      :or {method :get}}]
  {:url url
   :method method
   :body body})

(def r (request "http://jsonplaceholder.typicode.com/posts/1" :method :delete))

(defn symbol-to-keyword
  "takes GET -> :get"
  [sym]
  (-> sym name lower-case keyword))

(defn do-request
  "executes the result from (request)"
  [request]
  (let [body (:body request)]
    (json/read-str
     (:body
      ((resolve
        (symbol (str "client/" (-> request :method name))))
       (:url request)
       {:body (if (not (nil? body))
                (json/write-str body))
        :content-type "application/json"})))))

(defmacro defservice
  [service & resources]
  `(do
     ~@(for [resource resources
             endpoint (rest resource)]
         (let [resource-name (first resource)
               params (drop 2 endpoint)
               url (apply build-url
                          (flatten [(str @(resolve service) "/" resource-name)
                                    params]))
               method (symbol-to-keyword (first endpoint))
               symbol-params (map #(-> % name symbol) params)
               param-map (into {} (map (fn [param] {(keyword param)
                                               param})
                                  symbol-params))]
           `(def ~(second endpoint)
              (with-meta
                (fn
                  ([~@symbol-params]
                   (do-request
                    (request
                     (replace-path-params
                      ~url
                      ~param-map)
                     :method ~method)))
                  ([body# ~@symbol-params]
                   (do-request
                    (request
                     (replace-path-params
                      ~url
                      ~param-map)
                     :method ~method
                     :body body#))))
                {:url ~url :params [~@params] :method ~method}))))))

(defservice json
  (posts
   (GET get-posts :id :name)
   (POST save-food))
  (nutrient
   (DELETE delete-nutrient :id)
   (POST save-nutrient :id)))
