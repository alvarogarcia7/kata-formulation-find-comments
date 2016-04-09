(ns find-comments.core-test
  (:use midje.sweet
          find-comments.core))

(facts "canary tests"
  (fact "truthiness"
    true => true)
  
  (fact "falsiness"
  false => false))

 
(facts "about ensuring line contains CS"
  (let [ensure (partial ensure-at-least-one-cs-in-line ["#" "//"])]
    (fact "there is no CS in line"
      (ensure "") => nil
      (ensure "test") => nil
      (ensure "more than one word") => nil
      (ensure "only one slash /") => nil)
    (fact "there is a CS in line"
      (ensure "#") => true
      (ensure "test #comment") => true
      (ensure "// all is comment") => true
      (ensure "many # CS // in one line") => true)))

(facts "about finding comment in line"
  (let [fc (partial find-comment-in-line ["#" "//"])]
    (fact "there is no comment in line"
      (fc "") => nil
      (fc "test") => nil
      (fc "some $$ strange && symbols") => nil
      (fc "split('//');") => nil
      (fc "/1/2/3/4") => nil)
    (fact "there is some comment in line"
      (fc "#all string is comment!") => "all string is comment!"
      ;; empty comment is still a comment ;)
      (fc "#") => ""
      (fc "'a#' # test") => " test"
      (fc "'a'# # hooray") => " # hooray"
      (fc "'a\"//' # test # some") => " test # some"
      (fc "//  ") => "  "
      (fc "// first # second") => " first # second"
      (fc "# first // second") => " first // second"
      )))

(facts "about finding comments"
  (fact "multiple comments in a file"
    (find-comments-in-file "./dev-resources/sample_code/file1.php") => '(
      "comment (single line)" 
      " lagun !! naiz hizkuntza bat ez dut ulertzen in Iruzkin bat harrapatuta !! lagundu nazakezu?"
      " another comment"))

  (fact "comments can contain comment tokens"
    (find-comments-in-file "./dev-resources/inception/movie.php") => '(
      "a comment"
      "another comment"
      "a // within a #"
      "we need to go deeper"
      "i heard // you like #s so i put a // in your # so you can // while you #")))

(facts "about finding php files"

  (fact "a single level"
    (find-php-files "./dev-resources/sample_code/") => '("./dev-resources/sample_code/file1.php"))

  (fact "no need to specify the slash at the end of the path"
    (find-php-files "./dev-resources/sample_code") => '("./dev-resources/sample_code/file1.php"))

  (fact "multiple levels"
    (find-php-files "./dev-resources/matching_at_multiple_levels/") => '(
      "./dev-resources/matching_at_multiple_levels/file1.php"
      "./dev-resources/matching_at_multiple_levels/folder/file2.php"))

  (fact "only matches php files, not folders"
    (find-php-files "./dev-resources/folder_matching_pattern/") => '())

  (fact "only matches files with php extension"
    (find-php-files "./dev-resources/bad_extension/") => '()))

