(ns find-comments.core-test
  (:use midje.sweet
          find-comments.core))

(facts "canary tests"
  (fact "truthiness"
    true => true)
  
  (fact "falsiness"
  false => false))

(facts "about finding comments"
  (fact "multiple comments in a file"
    (find-comments-in-file "./dev-resources/sample_code/file1.php") => '(
      "comment (single line)" 
      " Помогите!! Я в ловушке на этом языке. Можете ли вы перевести это?"
      " another comment")))

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
    (find-php-files "./dev-resources/folder_matching_pattern/") => '()))

