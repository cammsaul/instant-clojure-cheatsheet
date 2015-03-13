(ns clojureref.sources
  "Static data relating to namespaces and symbols."
  (:require [clojure.string :as str]
            [clojureref.util :as util]))

(def ^:private namespaces (atom []))

(defn set-namespaces! [ns-list]
  (reset! namespaces ns-list))

(defn- -dox-for-symbol
  "Return dictionary of docstr, args, etc. for SYMB."
  [ns-name-str symb-name]
  (let [qualified-symbol (symbol ns-name-str symb-name)]
    (util/doc-for-symbol qualified-symbol)))

(defn- -symbol-info [ns-info ns-name-str symb-name]
  (merge ns-info
         (-dox-for-symbol ns-name-str symb-name)))

(defn symbols-for-ns
  "Return seq of symbols + info like [symb-name {info}] for NS."
  [ns]
  (let [ns-name-str (-> ns ns-name str)
        ns-info {:namespace ns-name-str}]
    (->> ns
         ns-publics                     ; map of symbol -> var
         keys
         (map (fn [symb]
                (let [symb-name (str symb)]
                  [symb-name (-symbol-info ns-info ns-name-str symb-name)]))))))

(def all-symbols
  "Dict of {symb-name -> [{info}+]}"
  (delay (->> @namespaces
              (mapcat symbols-for-ns)
              (group-by first)
              (map (fn [[k valls]]
                     {k (map second valls)}))
              (into {}))))

(def all-symbols-keys
  "Sorted vector of all string names of all symbols."
  (delay (->> @all-symbols
              keys
              sort
              vec)))
