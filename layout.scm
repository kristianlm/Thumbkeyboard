(
 ((help) "..... ..xx.")

 ;; tap 1 finger
 (E      "...x. .....") ;; e comes after t, before r
 (A      "x.... .....") ;; a comes before n
 ((repeat) "..... ....x")
 (SPACE  "..... ...x.")
 (T      "..... x....") ;; t comes after a i n s, before h

 (O "..x.. .....") ;; after t
 (I ".x... .....")
 (N "..... ..x..") ;; after e a o i
 (S "..... .x...") ;; s after i
 (H "....x .....") ;; h after t, e

 ;; tap 2 fingers
 (R "...x. .x...") ;; r after e a o
 (D "....x .x...") ;; d after e n
 (L "...x. x....")
 (C "....x x....")

 (U "..x.. x....") ;; u after o q
 (M ".x... .x...")
 (K "....x ..x..")

 (W ".x... x....")
 (F "..x.. .x...")
 (J "..x.. ..x..")

 (G "...x. ..x..") ;; g after n
 (Y ".x... ..x..")
 (P "...x. ...x.")
 (B ".x... ...x.")
 (V "....x ...x.")
 (Q "..x.. ...x.")

 ;; alternative swipes for X and Z
 (X "..... .x..."
    "..... x....")
 (Z "..... x...."
    "..... .x...")


 (PERIOD
  ".x... ....."
  ".x... .x..."
  ".x... .....")
 ((shift SEMICOLON) ;; ":"
  ".x... ....."
  ".x... .x..."
  ".x... ....."
  ".x... .x..."
  ".x... .....")
 (COMMA ;; ","
  ".x... ....."
  ".x... x...."
  ".x... .....")
 (SEMICOLON ;; ";"
  ".x... ....."
  ".x... x...."
  ".x... ....."
  ".x... x...."
  ".x... .....")
 ((shift 1) ;; "!"
  ".x... ....."
  ".x... ...x."
  ".x... .....")
 ((shift SLASH) ;; "?"
  ".x... ....."
  ".x... ...x."
  ".x... ....."
  ".x... ...x."
  ".x... .....")

 ("æ"
  "...x. ....."
  "...x. ..x.."
  "...x. .....")
 ("æ"
  "...x. ....."
  "...x. ..x..")

 ("ø"
  "...x. ....."
  "...x. ...x."
  "...x. .....")
 ("ø"
  "...x. ....."
  "...x. ...x.")

 ("å"
  "...x. ....."
  "...x. ....x"
  "...x. .....")
 ("å"
  "...x. ....."
  "...x. ....x")


 ((shift) "x.... ....x")
 ((shift)
  "..... ....x"
  "x.... ....x"
  "..... ....x")
 ((shift)
  "..... ....x"
  "x.... ....x")
 ((shift)
  "x.... ....x"
  "..... ....x")

 ((ctrl)
  (? "..... ....x")
  ".x... ....x"
  (? "..... ....x")
  "..... .....")

 ((alt)
  (? "..... ....x")
  "..x.. ....x"
  (? "..... ....x")
  "..... .....")

 ((meta)
  (? "..... ....x")
  "....x ....x"
  (? "..... ....x")
  "..... .....")

 ((shift 9) ;; "("
  ".x... ....."
  ".x... x...."
  ".x... ..x.."
  ".x... .....")
 ((shift 0) ;; ")"
  ".x... ....."
  ".x... ..x.."
  ".x... x...."
  ".x... .....")

 (LEFT_BRACKET ;; "["
  "..x.. ....."
  "..x.. x...."
  "..x.. ..x.."
  "..x.. .....")
 (RIGHT_BRACKET ;; "]"
  "..x.. ....."
  "..x.. ..x.."
  "..x.. x...."
  "..x.. .....")

 ((shift LEFT_BRACKET) ;; "{"
  "....x ....."
  "....x x...."
  "....x ..x.."
  "....x .....")
 ((shift RIGHT_BRACKET) ;; "}"
  "....x ....."
  "....x ..x.."
  "....x x...."
  "....x .....")

 ((shift COMMA) ;; "<"
  "...x. ....."
  "...x. x...."
  "...x. ..x.."
  "...x. .....")
 ((shift PERIOD) ;; ">"
  "...x. ....."
  "...x. ..x.."
  "...x. x...."
  "...x. .....")


 (DEL            "..... ...x." "..... .x...") ;; swipe left
 (FORWARD_DEL    "..... ...x." "..... ....x") ;; swipe up

 ;; this is like pressing the enter key, usually also means "go!"
 (ENTER "..... ....x"
        "..... ...x."
        "..... ..x.."
        "..... x....")
 ;; this one won't press "go!" and just insert a newline
 ("\n"
  "x.... ....."
  ".x... ....."
  "..x.. ....."
  "....x .....")

 (TAB "..... .x..."
      "..... ...x.") ;; right hand, swipe right

 (DPAD_UP        (: (* any) "x.... ....x" "x.... ....."))
 (DPAD_DOWN      (: (* any) "x.... ...x." "x.... ....."))
 (DPAD_LEFT      (: (* any) "x.... x...." "x.... ....."))
 (DPAD_RIGHT     (: (* any) "x.... ..x.." "x.... ....."))


 
 ((ctrl DPAD_RIGHT)
  "..... x...."
  "..... ..x..")
 ((ctrl DPAD_LEFT)
  "..... ..x.."
  "..... x....")

 (|0|
  "....x ....."
  "....x ....x"
  "....x .....")
 (|1|
  "....x ....."
  "....x ....x"
  "....x ....."
  "....x ....x"
  "....x .....")
 (|2|
  "....x ....."
  "....x .x..."
  "....x .....")
 (|3|
  "....x ....."
  "....x .x..."
  "....x ....."
  "....x .x..."
  "....x .....")
 (|4|
  "....x ....."
  "....x ...x."
  "....x .....")
 (|5|
  "....x ....."
  "....x ...x."
  "....x ....."
  "....x ...x."
  "....x .....")
 (|6|
  "....x ....."
  "....x x...."
  "....x .....")
 (|7|
  "....x ....."
  "....x x...."
  "....x ....."
  "....x x...."
  "....x .....")
 (|8|
  "....x ....."
  "....x ..x.."
  "....x .....")
 (|9|
  "....x ....."
  "....x ..x.."
  "....x ....."
  "....x ..x.."
  "....x .....")

 (PLUS  ".x... ....."
        "x.... .....")
 (MINUS ".x... ....."
        "...x. .....")

 ((shift BACKSLASH) ;; "|"
  "..... .x..."
  "x.... .x..."
  ".x... .x..."
  "..x.. .x..."
  "..... .x...")

 ((shift 8) ;; "*"
  "x.... ....."
  ".x... ....."
  "...x. ....."
  ".x... ....."
  "..x.. .....")

 ((shift 7) ;; "&"
  "..... .x..."
  "....x .x..."
  "..x.. .x..."
  ".x... .x..."
  "x.... .x..."
  "..... .x...")

 ((shift 6) ;; "^"
  "..x.. ....."
  ".x... ....."
  "...x. ....."
  "....x .....")


 ((shift 3) ;; "#"
  "..... .x..."
  "...x. .x..."
  ".x... .x..."
  "..... .x...")

 ((shift 5) ;; "%"
  "..... .x..."
  "....x .x..."
  "..x.. .x..."
  "..... .x...")
 
 ((shift 2) ;; "@"
  "..... x...."
  "...x. x...."
  ".x... x...."
  "..... x....")

 ((shift APOSTROPHE) ;; "\""
  "x.... ....."
  ".x... ....."
  "...x. ....."
  "....x .....")

 (APOSTROPHE ;; "'"
  "x.... ....."
  ".x... .....")

 (GRAVE ;; "`"
  "x.... ....."
  ".x... ....."
  "...x. .....")
 (GRAVE ;; "`"
  "x.... ....."
  "...x. .....")


 ((shift MINUS)
  "..... .x..."
  ".x... .x..."
  "...x. .x..."
  "..... .x...")
 (EQUALS
  "..... .x..."
  ".x... .x..."
  "x.... .x..."
  "..... .x...")

 (SLASH
  "..... x...."
  "..... ..x.."
  "..... ...x.")
 (BACKSLASH
  "..... ..x.."
  "..... x...."
  "..... .x...")

 (MOVE_END  "..x.. ....."
            "....x .....")
 (MOVE_HOME "....x ....."
            "..x.. .....")

 )

