  [KeyEvent]: https://developer.android.com/reference/android/view/KeyEvent.html
  
# Adellica's Chorded Keyboard

This is a 12-button chorded keyboard for Android. The objective is to
replace the standard QUERTY Touch-screen keyboards. It is heavily
inspired
by [Chorded keyboards](https://en.wikipedia.org/wiki/Chorded_keyboard)
and Alexander
Burger's [PentiKeyboard](https://software-lab.de/penti.html) in
particular. The keyboard is fully usable but not very
user-friendly. The transparency feature may misfunction in certain
apps.

- With only 12 buttons, they can be large and you rarely miss
- With only 12 buttons, you'll need to press multiple buttons
  simultaniously to access all the 26 letters of the alphabet
- With fewer key misses, you (hopefully) don't need a dictionary
- There are swipe gestures for pretty much all keyboard symbols
- You can define your own layouts
- It is probably very difficult to learn to use proficiently but
  possibly worth the effort!

![screenshot](screenshot.gif)

## Design goals

### Universal

I want my keyboard to be useful everywhere:

- When I'm writing on my phone SMS / emails
- When I'm entering URLs into my browser
- When I'm entering `bash` commands into my Termux
- Anything `Hacker's Keyboard` can do, I want to be able to do!
- Open up for "touch typing" on a touchscreen (tested and possible,
  but could be improved with tactical feedback)

### Language support

I want something that'll work reasonable well for everyday use in
(hopefully) any language. At least, any phonetic or latin-based
language. Currently, the bundled layout has only been tested with:

- English
- Norwegian
- bash / C / Java / JavaScript / Scheme

### Little screen real-estate

I don't want to cover the screen with my keyboard. I don't want to
cover my screen with my input data - I want to cover it with output
data!

The idea is that since we're only dealing with 12 large buttons, they
don't need to be decorated in detail and thus can be transparent and
leave the enrire screen for your apps. We could make a non-transparent
version too.

### Dictionaryfree

This project focuses on letter-by-letter input, and aims to should be
fast enough on its own. There is therefore currently no dictionary
support.

The reason these are needed on soft QWERTY, I suppose, isn't
because letter-by-letter typing on touchscreens is slow - but because
it's error prone.

### Full Input Spectrum

Since I'll hopefully be able to use with this `emacs` on `Termux`,
I'll need lots of key combinations, like `M-f` and `C-M->`.

I don't know how non-Latin input would work with this, however. I'm
currently focusing on Latin and programming input.

# Interpreter 

There's a very simple stack-based interpreter meant to allow flexible
configuration of your keyboard and layout. It's called Thumb Stack
Machine (TSM).

> Note: Clojure's syntax-highlighting works quite well for this language.

You can run the TSM interpreter without Android like this (after
`gradle` or Android Studio build):

```clojure
➤ rlwrap java -cp app/build/intermediates/classes/debug/ com.adellica.thumbkeyboard.tsm.Reader
Thumb StackMachine (REPL on port 2345)
[ ] 2 3 +
[ 5 ]
```

This syntax is used for layout configuration. See
[main.thumb](app/src/main/assets/main.thumb) and
[default.layout.thumb](app/src/main/assets/default.layout.thumb).

## Keypresses

TSM has special support for `keypresses`. These borrow from the Emacs
syntax, and have some operators:

```clojure
➤ rlwrap java -cp app/build/intermediates/classes/debug/ com.adellica.thumbkeyboard.tsm.Reader
Thumb StackMachine (REPL on port 2345)
[ ] :C-a ;; this means the keypress "holding control while pressing a"
[ :C-a ] drop
[ ] :x ;; a single-letter means a keypress of that key (here, "pressing x")
[ :x ] shift? ;; is keypress on top of stack holding shift modifier?
[ false ] drop
[ ] :X ;; single-letters are case-sensitive!
[ :X ] shift?
[ true ] drop
[ ] :X false shift! ;; removing shift modifier makes it lowercase (convenience)
[ :x ] drop
[ ] :1
[ :1 ] dup
[ :1 :1 ] shift?
[ :1 false ] not ;; negate!
[ :1 true ] shift! ;; set shift modifier to true on keypress in stack position 2
[ :! ] drop ;; makes sence? holding shift and pressing 1 yields ! (US layout only)
[ ] :@ ;; let's try the other way
[ :@ ] false shift! ;; how does this keypress look like without holding shift?
[ :2 ] drop ;; it looks like :2 (again, it's always US layout)
[ ] :C-M-x ;; would be the keypress for control-alt-x
[ :C-M-x ] alt?
[ true ] drop ;; as expected, we're holding alt (meta) when pressing C-M-x.
[ ] :enter ;; non-single-letter inputs are supported (named keys)
[ :enter ] true shift! ;; they are not case-sensitive
[ :S-enter ] ;; and are always printed in lower-case (explicit shift (S) modifier)
```

Many of Android's [KeyEvent]s are supported (remove the `KEYCODE_`
prefix), but some have been renamed (`FORWARD_DEL` => `:delete`).

# TODO

- delete whitespace first before "delete word"
- add \t and \n boundaries for "delete word"
- emoji: find out how they work and support them
- real borders: give each button/box a deadzone/margin so that enter
  doesn't need two swipes down to not conflict with swipe left.
- a quick and easy way to update/share layouts/strokes
- add a way to switch to other keyboard apps
- add a non-seethrough mode
