(ns sulk.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [sulk.license :as slk]
            [cheshire.core :as cheshire]
            [clojure.walk :as walk])
  (:gen-class))

(def cli-options
  [["-k" "--key-file FILENAME" "Key file in DER format; private key or public key depending on operation"
    :validate [#(not (empty? %)) "Private key file is required"]]
   ["-i" "--input-file FILENAME" "optional; input license file in json format"]
   ["-o" "--output-file FILENAME" "optional; license file name"
    :default "license"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Sulk software license key generator"
        ""
        "Usage: sulk [options]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  generate  Generate a new license key"
        "  verify    Verify an existing license key"
        ""
        "Please refer to the documentation for more information."]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments and options."
  [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"generate" "verify"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))


(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn gen-license-interactive [])

(defn load-license-file [filename]
  "Loads a json file into a clojure Map"
  (walk/keywordize-keys (cheshire/decode (slurp filename))))

(defn generate-license [options]
  (slk/read-private-key (:key-file options))
  (let [license-data (if (:input-file options)
                       (load-license-file (:input-file options))
                       (gen-license-interactive))
        lic-text (slk/generate-license-text license-data)]
    (slk/write-license-file! lic-text (:output-file options))))

(defn verify-license [options]
  (slk/read-public-key (:key-file options))
  (let [verification (slk/read-license-file (:input-file options))]
    (if (:error verification)
        (exit -2 (str "License could not be verified: " (:error verification)))
        (do
          (println verification)
          (exit 1 "License is valid")))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "generate" (generate-license options)
        "verify"   (verify-license options)))))
