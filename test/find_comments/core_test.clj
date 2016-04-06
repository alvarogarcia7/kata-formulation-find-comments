(ns find-comments.core-test
  (:use midje.sweet
          find-comments.core))

(facts "canary tests"
  (fact "truthiness"
    true => true)
  
  (fact "falsiness"
  false => false))

(facts "about finding comments using regex"
  (fact "without quotes or without trailing comment symbol returns nil"
    (find-comment-using-regex " # test") => nil
    (find-comment-using-regex "") => nil
    (find-comment-using-regex "\"asd#\"") => nil)
  (fact "with quotes return comment"
    (find-comment-using-regex "\"a#\" #comment") => "comment"
    (find-comment-using-regex "\"#\" #comment") => "comment"
    (find-comment-using-regex "\"#\" # first # second") => " first # second"
    (find-comment-using-regex "\"a#a\" \"b#b\" #comment") => "comment"
    ;; also need to check single quotes
    (find-comment-using-regex "'a#' #comment") => "comment"
    (find-comment-using-regex "'#' #comment") => "comment"
    (find-comment-using-regex "'#' # first # second") => " first # second"
    (find-comment-using-regex "'a#a' 'b#b' #comment") => "comment"
    (find-comment-using-regex "'a#a' 'b#b' #comment # and inner") => "comment # and inner"
    ))

(facts "about finding comments"
  #_(fact "multiple comments in a file"
    (find-comments-in-file "./dev-resources/sample_code/file1.php") => '(
      "comment (single line)" 
      " lagun !! naiz hizkuntza bat ez dut ulertzen in Iruzkin bat harrapatuta !! lagundu nazakezu?"
      " another comment"))

  #_(fact "comments can contain comment tokens"
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

