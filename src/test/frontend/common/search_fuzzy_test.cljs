(ns frontend.common.search-fuzzy-test
  (:require [cljs.test :refer [are deftest is testing]]
            [frontend.common.search-fuzzy :as fuzzy]))

(deftest clean-str-test
  (testing "removes brackets, slashes, underscores, parens and spaces, lowercases"
    (are [input expected] (= expected (fuzzy/clean-str input))
      "Hello World"       "helloworld"
      "[[page ref]]"      "pageref"
      "path/to/file"      "pathtofile"
      "under_score"       "underscore"
      "(some parens)"     "someparens"
      "MiXeD CaSe"        "mixedcase"
      "path\\to\\file"    "pathtofile")))

(deftest str-len-distance-test
  (testing "returns 1.0 for equal-length strings"
    (are [s1 s2] (= 1.0 (fuzzy/str-len-distance s1 s2))
      "abc" "xyz"
      "a"   "b"))
  (testing "returns normalized distance for different lengths"
    (are [s1 s2 expected] (= expected (fuzzy/str-len-distance s1 s2))
      "ab"   "abcd" 0.5
      "a"    "abcd" 0.25))
  (testing "is symmetric"
    (are [s1 s2] (= (fuzzy/str-len-distance s1 s2)
                    (fuzzy/str-len-distance s2 s1))
      "short" "longer string"
      "a"     "abc")))

(deftest score-test
  (testing "exact match scores highest"
    (let [exact (fuzzy/score "test" "test")
          partial (fuzzy/score "test" "testing")]
      (is (pos? exact))
      (is (> exact partial))))
  (testing "prefix match scores higher than mid-string match"
    (let [prefix (fuzzy/score "test" "test page")
          mid (fuzzy/score "test" "my test page")]
      (is (> prefix mid))))
  (testing "no match returns 0"
    (is (zero? (fuzzy/score "xyz" "abc")))))

(deftest fuzzy-search-test
  (testing "returns matching items sorted by score"
    (let [data ["apple" "application" "banana" "appetizer"]
          result (fuzzy/fuzzy-search data "app")]
      (is (every? #(re-find #"(?i)app" %) result))
      (is (not (some #{"banana"} result)))
      (is (= "apple" (first result)) "exact prefix match should rank first")))
  (testing "respects limit"
    (let [data (map #(str "item-" %) (range 50))
          result (fuzzy/fuzzy-search data "item" :limit 5)]
      (is (<= (count result) 5))))
  (testing "supports extract-fn"
    (let [data [{:name "alice"} {:name "bob"} {:name "alison"}]
          result (fuzzy/fuzzy-search data "ali" :extract-fn :name)]
      (is (= 2 (count result)))
      (is (every? #(re-find #"(?i)ali" (:name %)) result))))
  (testing "returns empty for no matches"
    (is (empty? (fuzzy/fuzzy-search ["a" "b" "c"] "zzz")))))
