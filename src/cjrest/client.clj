 (ns cjrest.client
     (:require [cjrest.url :refer :all]
               [clj-http.client :as client]
               [clojure.string :refer [replace-first
                                       lower-case]]
               [clojure.data.json :as json]))

(defn request
  [url & {:keys [method body]
      :or {method :get}}]
  {:url url
   :method method
   :body body})

(def request-methods
  {:get client/get
   :post client/post
   :delete client/delete
   :update client/update})

(defn dispatch-request
  "executes the result from (request)"
  [request] 
  (let [body (:body request)]
    (json/read-str
     (:body
      (((:method request) request-methods)
       (:url request)
       {:body (if (not (nil? body))
                (json/write-str body))
        :content-type "application/json"})))))

(defn build-endpoint
  [service-url resource method params]
  {:service service-url
   :path (apply build-url `(~(str "/" (name resource)) ~@params))
   :method method
   :params params})

(defn make-request
  [endpoint body param-map]
  (request (replace-path-params
               (str (var-get (:service endpoint))
                    (:path endpoint))
               param-map)
              :method (:method endpoint)
              :body body))

(defn do-request
  [endpoint body param-map]
  (dispatch-request (make-request endpoint body param-map)))

(defmacro def-method
  [method]
  `(defmacro ~method
     [name# params#]
     (let [param-map#
           (into {} (map (fn [param#]
                           {(keyword param#) param#})
                         params#))
           meta# {:params
                 (into [] (map  #(keyword %) params#))
                 :method (-> '~method lower-case keyword)}]
       `(fn request-anon
          [~'service-var# ~'resource#]
          (let [~'endpoint#
                (build-endpoint
                 ~'service-var#
                 ~'resource#
                 ~(:method meta#)
                 ~(:params meta#))]
             (def ~name#
               (with-meta
                 (fn method-anon
                   ~@(remove
                      nil?
                      (list
                       `([~@params#]
                         (do-request ~'endpoint#  nil ~param-map#))
                       (if (= :post (:method meta#))
                         `([~'body# ~@params#]
                           (do-request ~'endpoint# ~'body# ~param-map#))))))
                 ~meta#))
            )))))

(defn resource
  [res & methods]
  {:resource res
   :methods methods})

(defn service
  [service-url & resources]
  (for [resource resources
        method (:methods resource)]
    (let [method-meta (meta method)]
      (method
       (build-endpoint service-url
                       (:resource resource)
                       (:method method-meta)
                       (:params method-meta))))))

(def-method GET)
(def-method POST)
(def-method DELETE)
(def-method PUT)

(def json "http://jsonplaceholder.typicode.com/")

(service #'json
         (resource :post
                   (GET get-post [id])))

(defmacro defresource
  [resource service-var]
  (let [fubar (keyword resource)]
    `(defmacro ~resource
       [& endpoints#]
       `(do
          ~(for [endpoint# endpoints#]
             `(~endpoint# ~~(keyword resource) ~~service-var))
          ))))

(defresource food #'json)

(clojure.pprint/pprint (macroexpand-1 '(food (GET v [id]))))
