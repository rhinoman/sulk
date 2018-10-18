(ns sulk.license
  (:require [cheshire.core :as cheshire]
            [clojure.string :refer [blank?]]
            [taoensso.timbre :as timbre]
            [clojure.string :as str]
            [clojure.walk :as cw]
            [clojure.java.io :as io])
  (:import (sulk KeyManager)
           (java.util Base64)
           (clojure.lang IPersistentMap)
           (java.io IOException)))

(set! *warn-on-reflection* true)

(def ^KeyManager km (KeyManager/getInstance))

(def license-header "----BEGIN LICENSE----")
(def license-footer "-----END LICENSE-----")

(def bad-license-resp {:error "Invalid license"})
(def file-error-resp {:error "Could not read license file"})
(def license-parse-error {:error "Could not read license"})
;; Normally, you will either read the private key or the public key
;; But seldom would you need to do both outside of unit tests


(defn read-private-key [filename]
  "reads the private key into the key manager"
  (try
    (.readPrivateKey km filename)
    (catch Exception e
      (timbre/error e)
      (System/exit -1))))

(defn read-public-key [filename]
  "reads the public key into the key manager"
  (try
    (.readPublicKey km filename)
    (catch Exception e
      (timbre/error e)
      (System/exit -1))))

(defn sign-license [^String license-text]
  "Signs the provided license - returns a byte array"
  (.sign km license-text))

(defn verify-license [^String license ^String signature]
  "Given a license and signature, verifies"
  (.verify km license signature))

(defn encode-license [^IPersistentMap license-data]
  "Encodes the license Map to Base64-encoded json"
  (let [ld-json (cheshire/encode license-data)]
    (.encodeToString (Base64/getEncoder) (.getBytes ld-json))))

(defn decode-license [^String license-string]
  "Decodes the license b64 string back to a clojure Map"
  (try
    (let [ba (.decode (Base64/getDecoder) license-string)
          js (apply str (map char ba))
          lm (cheshire/decode js)]
      ;; make the string keys back into clj keywords again
      (cw/keywordize-keys lm))
    (catch Exception e
      (timbre/warn "Could not decode license text" e)
      license-parse-error)))

(defn generate-license-text [^IPersistentMap license-data]
  "Generates the license file text"
  (let [ld-b64 (encode-license license-data)
        sig (sign-license ld-b64)]
    (str license-header "\n" ld-b64 "\n" sig "\n" license-footer)))

(defn extract-license-text [^String license-text]
  (let [lines (str/split-lines license-text)]
    {:license (get lines 1)
     :signature (get lines 2)}))

(defn read-license-text [^String license-text]
  "verify license signature and returns license data"
  (try
    (let [lines (str/split-lines license-text)
          ^String header (get lines 0)
          ^String license (get lines 1)
          ^String signature (get lines 2)
          ^String footer (get lines 3)]
      (assert (= header license-header))
      (assert (= footer license-footer))
      (assert (verify-license license signature))
      ;;Convert the license string back to a clojure Map
      (decode-license license))
    (catch AssertionError ae
      (timbre/warn "Invalid License!" ae)
      bad-license-resp)
    (catch Exception e
      (timbre/warn "Couldn't parse license!" e)
      bad-license-resp)))

;; Read license file
(defn read-license-file [^String filename]
  "Reads the license file"
  (try
    (read-license-text (slurp filename))
     (catch IOException ioe
       (timbre/error "Could not read license file!")
       file-error-resp)))

;; Write out license file
(defmulti write-license-file! (fn [x y] [(class x)]))

(defmethod write-license-file! [String]
  [license-text ^String filename]
  "Writes encoded license text to file"
  (try
    (spit filename license-text)
    (catch Exception e
      (timbre/error "Error writing license file" e))))

(defmethod write-license-file! [IPersistentMap]
  [license ^String filename]
  "Encodes license to text and then writes to file"
   (write-license-file! (generate-license-text license) filename))



