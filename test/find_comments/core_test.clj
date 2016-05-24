(ns find-comments.core-test
  (:require [expectations :refer [expect from-each]])
  (:use find-comments.core))

;; helpers

(defmacro expect-cases
  [cases f]
  (cons 'do
        (for [[data expected] (eval cases)]
          `(expect ~expected (~f ~data)))))

;; canary tests

(expect true true)

;; about finding comment in line

;; - there is no comment in line:

(def nil-cases ["" 
                "test" 
                "some $$ strange && symbols"
                "split('//');"
                "/1/2/3/4"])

(expect nil (from-each [l nil-cases] (find-comment-in-line l)))

;; - there is some comment in line:

(def comment-cases
  {"#all string" "all string" 
   "#" ""
   "'a#' # test" " test"
   "'a'# # hooray" " # hooray"
   "'a\"//' # test # some" " test # some"
   "//  " "  "
   "// first # second" " first # second"
   "# first // second" " first // second"})

(expect-cases comment-cases find-comment-in-line)

;; about finding comments

(def file-cases
  {;; - multiple comments in a file
   "./dev-resources/sample_code/file1.php" 
   ["comment (single line)" 
    " lagun !! naiz hizkuntza bat ez dut ulertzen in Iruzkin bat harrapatuta !! lagundu nazakezu?"
    " another comment"]
   ;; - comments can contain comment tokens
   "./dev-resources/inception/movie.php"
   ["a comment" 
    "another comment" 
    "a // within a #" 
    "we need to go deeper" 
    "i heard // you like #s so i put a // in your # so you can // while you #"]})

(expect-cases file-cases find-comments-in-file)

;; about finding php files

;; - a single level
(expect '("./dev-resources/sample_code/file1.php") (find-php-files "./dev-resources/sample_code/"))

;; - no need to specify the slash at the end of the path
(expect '("./dev-resources/sample_code/file1.php") (find-php-files "./dev-resources/sample_code"))

;; - multiple levels
(expect 
 ["./dev-resources/matching_at_multiple_levels/file1.php"
  "./dev-resources/matching_at_multiple_levels/folder/file2.php"]
  (find-php-files "./dev-resources/matching_at_multiple_levels/"))

;; - only matches php files, not folders
(expect [] (find-php-files "./dev-resources/folder_matching_pattern/"))

;; - only matches files with php extension
(expect [] (find-php-files "./dev-resources/bad_extension/"))

