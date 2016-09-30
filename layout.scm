(
 ("TOGGLE HELP" "... xx.")

 ;; tap 1 finger
 (0      "x.. ...") ;; RESERVED for modifier keys?
 (E      ".x. ...") ;; e comes after t, before r
 (A      "..x ...") ;; a comes before n
 (REPEAT "... ..x")
 (SPACE  "... .x.")
 (T      "... x..") ;; t comes after a i n s, before h

 ;; tap 2 fingers
 (O "..x x..") ;; after t
 (I ".x. .x.")
 (N ".x. ..x") ;; after e a o i
 (S "..x .x.") ;; s after i
 (H ".x. x..") ;; h after t, e
 (R "x.. ..x") ;; r after e a o
 (D "..x ..x") ;; d after e n
 (L "x.. x..")
 (C "x.. .x.")

 ;; delay right middle
 (U "..x ..."
    "..x .x.") ;; u after o q
 (M ".x. ..."
    ".x. .x.")
 (K "x.. ..."
    "x.. .x.")

 ;; delay left middle
 (W "... ..x"
    ".x. ..x")
 (F "... x.."
    ".x. x..")
 (J "... .x."
    ".x. .x.")

 ;; delay left (hold right middle)
 (P "... .x."
    "x.. .x.")
 (G "... .x."
    "..x .x.") ;; g after n

 ;; delay right (hold left middle)
 (Y ".x. ..."
    ".x. x..")
 (V ".x. ..."
    ".x. ..x")

 (B "x.. ..."
    "x.. ..x")
 (Q "... ..x"
    "x.. ..x")
 (X "... x.."
    "..x x..")
 (Z "..x ..."
    "..x x..")

 ;; multitap
 (DEL   (: "... .x." (* any) ".x. .x." "... .x."))
 (ENTER (: "... .x." (* any) "..x .x." "... .x."))
 (TAB   (: "... .x." (* any) "x.. .x." "... .x."))

 (DPAD_UP    (: "x.. ..." (* any) "x.. ..x" "x.. ..."))
 (DPAD_DOWN  (: "x.. ..." (* any) "x.. .x." "x.. ..."))
 (DPAD_LEFT  (: ".x. ..." (* any) ".x. .x." ".x. ..."))
 (DPAD_RIGHT (: ".x. ..." (* any) ".x. ..x" ".x. ...")))
