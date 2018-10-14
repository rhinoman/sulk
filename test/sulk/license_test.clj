(ns sulk.license-test
  (:require [clojure.test :refer :all]
            [sulk.license :as slk]))

(def test-license-1 {:customer-name "Acme Corp."
                     :software "fizzbuzzer"
                     :version 1.0
                     :max-users 30
                     :issue-date "2018-01-01"
                     :expires "2032-01-01"})

(defn generate-bad-license []
  (str slk/license-header "\n" (slk/encode-license test-license-1)
       "\n" (slk/sign-license (slk/encode-license {:wtf "bbq"})) "\n"
       slk/license-footer))

(deftest test-license-handling
  (testing "Test key loading"
    (slk/read-private-key "resources/TEST_private_key.der")
    (slk/read-public-key "resources/TEST_public_key.der"))
  (testing "Test generate license signature"
    (let [lic-enc (slk/encode-license test-license-1)
          lic-dec (slk/decode-license lic-enc)
          sig (slk/sign-license lic-enc)]
      (is (not (empty? lic-enc)))
      (is (= lic-dec test-license-1))
      (println lic-enc)
      (println sig)
      (testing "Test verify license"
        (is (true? (slk/verify-license lic-enc sig))))
      (testing "Test verify bad license"
        (is (false? (slk/verify-license (str "foobar" lic-enc) sig))))))
  (testing "Test generate license text"
    (let [lic-text (slk/generate-license-text test-license-1)
          decoded-lic-text (slk/read-license-text lic-text)
          bad-lic-text (generate-bad-license)
          decoded-bad-lic (slk/read-license-text bad-lic-text)]
      (is (not (empty? lic-text)))
      (is (= decoded-lic-text test-license-1))
      (print bad-lic-text)
      (is (= decoded-bad-lic slk/bad-license-resp)))))

