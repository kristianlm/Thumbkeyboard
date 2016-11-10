
# Adellica's Chorded Keyboard

This is a 4-button chorded keyboard for Android. Maybe this adventure
will make me replace my Androids built-in QWERTY keyboard.

## Design goals

### Universal

I want my keyboard to be useful everywhere:

- When I'm writing on my phone SMS / emails
- When I'm entering URLs into my browser
- When I'm entering `bash` commands into my Termux
- Anything `Hacker's Keyboard` can do, I can do!

### Language support

I want something that'll work reasonable well for my everyday
languages:

- English
- Norwegian
- bash / C / Java / JavaScript / Scheme

### Little screen real-estate

I don't want to cover the screen with my keyboard, I want to cover my
screen with my input data - I want to cover it with output data!

### Touch-typing

Once proficient, I want to be able to type without looking at the
keyboard. Fewer keys will be the "key" here, I think - few large and
unmissable buttons.

### Robust

I want it to be "hard" to hit the wrong buttons, while acheiving a
reasonable speed. If I have only 4 buttons, for example, pressing the
wrong button won't happen too often. However, the problem is probably
just shifted over to timing issues instead. We'll have to see how well
this works out in practice.

### Dictionaryfree

I don't think I want natural-language dictionaries, letter-by-letter
input should be fast enough on its own. The reason these are needed on
soft QWERTY, I suppose, isn't because typing is slow - but because
it's error prone.

### Full Input Spectrum

Since I'll hopefully be able to use with this `emacs` on `Termux`,
I'll need lots of key combinations, like `M-f` and `C-M->`.

### Configurable

It should be possible to customize the chords so users can add their
own.

### Hardwareizeable?

With 3 hardware buttons, it should be able to reimplement the keyboard
relatively easily. That probably means that swipe-gestures should be
available as chords too. Now you can enter text into your Arduino toy
just the same way you'd enter text into your phone.

## What's missing

I don't know how non-Latin input would work with this. I'm focusing on
Latin and programming input.


# TODO

- fixme: make it so that inserting "Å" and friends releases shift
- arrow keys as single swipe keys. replace -/+ swipes perhaps? arrows
  are probably used more often
- cannot type "C-/" (text insert vs pressing key?)
- & is quite long
- ^ is missing
- swap modifier for () and <>
- make CTRL + friends work with single doble-tap
- separate number-mode (for numeric input, phone numbers, time, date etc)

