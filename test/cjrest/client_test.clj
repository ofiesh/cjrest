(ns cjrest.client-test
  (:require [clojure.test :refer :all]
            [cjrest.client :refer [GET
                                   build-endpoint
                                   build-resource]]))

(def service "http://jsonplaceholder.typicode.com")

(deftest test-build-endpoint
  (testing "Testing building endpoint"
    (is (= {:service #'service
            :path "/foo/bar/:bar"
            :method :get
            :params [:bar]}
           (build-endpoint 
            #'service :foo :get [:bar])))))

