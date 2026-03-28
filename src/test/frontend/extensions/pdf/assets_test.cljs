(ns frontend.extensions.pdf.assets-test
  (:require [clojure.test :as test :refer [are deftest is testing]]
            [frontend.extensions.pdf.utils :as pdf-utils]))

(deftest fix-local-asset-pagename
  (testing "matched filenames"
    (are [x y] (= y (pdf-utils/fix-local-asset-pagename x))
      "2015_Book_Intertwingled_1659920114630_0" "2015 Book Intertwingled"
      "hls__2015_Book_Intertwingled_1659920114630_0" "2015 Book Intertwingled"
      "hls/2015_Book_Intertwingled_1659920114630_0" "hls/2015 Book Intertwingled"
      "hls__sicp__-1234567" "sicp"))
  (testing "non matched filenames"
    (are [x y] (= y (pdf-utils/fix-local-asset-pagename x))
      "foo" "foo"
      "foo_bar" "foo_bar"
      "foo__bar" "foo__bar"
      "foo_bar.pdf" "foo_bar.pdf")))

(deftest hls-file?-test
  (testing "returns true for hls__ prefixed filenames"
    (are [x] (true? (pdf-utils/hls-file? x))
      "hls__document.pdf"
      "hls__2015_Book_Title_123456_0"))
  (testing "returns false for non-hls filenames"
    (are [x] (not (pdf-utils/hls-file? x))
      "document.pdf"
      "HLS__uppercase"
      nil
      ""
      42)))

(deftest fix-selection-text-breakline-test
  (testing "joins broken lines with space for latin text"
    (are [input expected] (= expected (pdf-utils/fix-selection-text-breakline input))
      "this is a\ntest paragraph"    "this is a test paragraph"
      "he is 1\n8 years old"         "he is 18 years old"))
  (testing "joins hyphenated line breaks"
    (is (= "this is a test paragraph"
           (pdf-utils/fix-selection-text-breakline "this is a te-\nst paragraph"))))
  (testing "joins non-latin text without space"
    (are [input expected] (= expected (pdf-utils/fix-selection-text-breakline input))
      "这是一个\n\n段落"    "这是一个段落"
      "これ\n\nは、段落"   "これは、段落"))
  (testing "returns nil for blank input"
    (are [x] (nil? (pdf-utils/fix-selection-text-breakline x))
      nil
      ""
      "   ")))
