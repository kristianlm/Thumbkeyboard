package com.adellica.thumbkeyboard.tsm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by klm on 9/28/16.
 */
public class Keycodes {

    private final static Map<String, Integer> mapping = new HashMap<String, Integer>();

    static {
        init();
    }

    private static void put(String name, int keycode) {
        mapping.put(name, keycode);
    }
    public static Integer fromString(final String name) {
        return mapping.get(name);
    }
    public static String toString(int _code) {
        Integer code = _code;
        for(String key : mapping.keySet()) {
            if(code.equals(mapping.get(key))) return key;
        }
        throw new RuntimeException("no key for code " + code);
    }

    public static void init() {
        // taken from KeyCodes.class
        put("UNKNOWN", 0);
        put("SOFT_LEFT", 1);
        put("SOFT_RIGHT", 2);
        put("HOME", 3);
        put("BACK", 4);
        put("CALL", 5);
        put("ENDCALL", 6);
        put("0", 7);
        put("1", 8);
        put("2", 9);
        put("3", 10);
        put("4", 11);
        put("5", 12);
        put("6", 13);
        put("7", 14);
        put("8", 15);
        put("9", 16);
        put("*", 17);
        put("POUND", 18);
        put("DPAD_UP", 19);
        put("DPAD_DOWN", 20);
        put("DPAD_LEFT", 21);
        put("DPAD_RIGHT", 22);
        put("DPAD_CENTER", 23);
        put("VOLUME_UP", 24);
        put("VOLUME_DOWN", 25);
        put("POWER", 26);
        put("CAMERA", 27);
        put("CLEAR", 28);
        put("A", 29);
        put("B", 30);
        put("C", 31);
        put("D", 32);
        put("E", 33);
        put("F", 34);
        put("G", 35);
        put("H", 36);
        put("I", 37);
        put("J", 38);
        put("K", 39);
        put("L", 40);
        put("M", 41);
        put("N", 42);
        put("O", 43);
        put("P", 44);
        put("Q", 45);
        put("R", 46);
        put("S", 47);
        put("T", 48);
        put("U", 49);
        put("V", 50);
        put("W", 51);
        put("X", 52);
        put("Y", 53);
        put("Z", 54);
        put(",", 55);
        put(".", 56);
        put("ALT_LEFT", 57);
        put("ALT_RIGHT", 58);
        put("SHIFT_LEFT", 59);
        put("SHIFT_RIGHT", 60);
        put("TAB", 61);
        put("SPACE", 62);
        put("SYM", 63);
        put("EXPLORER", 64);
        put("ENVELOPE", 65);
        put("ENTER", 66);
        put("BACKSPACE", 67);
        put("`", 68);
        put("-", 69);
        put("=", 70);
        put("[", 71);
        put("]", 72);
        put("\\", 73);
        put(";", 74);
        put("'", 75);
        put("/", 76);
        put("@", 77);
        put("NUM", 78);
        put("HEADSETHOOK", 79);
        put("FOCUS", 80);
        put("+", 81);
        put("MENU", 82);
        put("NOTIFICATION", 83);
        put("SEARCH", 84);
        put("MEDIA_PLAY_PAUSE", 85);
        put("MEDIA_STOP", 86);
        put("MEDIA_NEXT", 87);
        put("MEDIA_PREVIOUS", 88);
        put("MEDIA_REWIND", 89);
        put("MEDIA_FAST_FORWARD", 90);
        put("MUTE", 91);
        put("PAGE_UP", 92);
        put("PAGE_DOWN", 93);
        put("PICTSYMBOLS", 94);
        put("SWITCH_CHARSET", 95);
        put("BUTTON_A", 96);
        put("BUTTON_B", 97);
        put("BUTTON_C", 98);
        put("BUTTON_X", 99);
        put("BUTTON_Y", 100);
        put("BUTTON_Z", 101);
        put("BUTTON_L1", 102);
        put("BUTTON_R1", 103);
        put("BUTTON_L2", 104);
        put("BUTTON_R2", 105);
        put("BUTTON_THUMBL", 106);
        put("BUTTON_THUMBR", 107);
        put("BUTTON_START", 108);
        put("BUTTON_SELECT", 109);
        put("BUTTON_MODE", 110);
        put("ESCAPE", 111);
        put("DELETE", 112);
        put("CTRL_LEFT", 113);
        put("CTRL_RIGHT", 114);
        put("CAPS_LOCK", 115);
        put("SCROLL_LOCK", 116);
        put("META_LEFT", 117);
        put("META_RIGHT", 118);
        put("FUNCTION", 119);
        put("SYSRQ", 120);
        put("BREAK", 121);
        put("MOVE_HOME", 122);
        put("MOVE_END", 123);
        put("INSERT", 124);
        put("FORWARD", 125);
        put("MEDIA_PLAY", 126);
        put("MEDIA_PAUSE", 127);
        put("MEDIA_CLOSE", 128);
        put("MEDIA_EJECT", 129);
        put("MEDIA_RECORD", 130);
        put("F1", 131);
        put("F2", 132);
        put("F3", 133);
        put("F4", 134);
        put("F5", 135);
        put("F6", 136);
        put("F7", 137);
        put("F8", 138);
        put("F9", 139);
        put("F10", 140);
        put("F11", 141);
        put("F12", 142);
        put("NUM_LOCK", 143);
        put("NUMPAD_0", 144);
        put("NUMPAD_1", 145);
        put("NUMPAD_2", 146);
        put("NUMPAD_3", 147);
        put("NUMPAD_4", 148);
        put("NUMPAD_5", 149);
        put("NUMPAD_6", 150);
        put("NUMPAD_7", 151);
        put("NUMPAD_8", 152);
        put("NUMPAD_9", 153);
        put("NUMPAD_DIVIDE", 154);
        put("NUMPAD_MULTIPLY", 155);
        put("NUMPAD_SUBTRACT", 156);
        put("NUMPAD_ADD", 157);
        put("NUMPAD_DOT", 158);
        put("NUMPAD_COMMA", 159);
        put("NUMPAD_ENTER", 160);
        put("NUMPAD_EQUALS", 161);
        put("NUMPAD_LEFT_PAREN", 162);
        put("NUMPAD_RIGHT_PAREN", 163);
        put("VOLUME_MUTE", 164);
        put("INFO", 165);
        put("CHANNEL_UP", 166);
        put("CHANNEL_DOWN", 167);
        put("ZOOM_IN", 168);
        put("ZOOM_OUT", 169);
        put("TV", 170);
        put("WINDOW", 171);
        put("GUIDE", 172);
        put("DVR", 173);
        put("BOOKMARK", 174);
        put("CAPTIONS", 175);
        put("SETTINGS", 176);
        put("TV_POWER", 177);
        put("TV_INPUT", 178);
        put("STB_POWER", 179);
        put("STB_INPUT", 180);
        put("AVR_POWER", 181);
        put("AVR_INPUT", 182);
        put("PROG_RED", 183);
        put("PROG_GREEN", 184);
        put("PROG_YELLOW", 185);
        put("PROG_BLUE", 186);
        put("APP_SWITCH", 187);
        put("BUTTON_1", 188);
        put("BUTTON_2", 189);
        put("BUTTON_3", 190);
        put("BUTTON_4", 191);
        put("BUTTON_5", 192);
        put("BUTTON_6", 193);
        put("BUTTON_7", 194);
        put("BUTTON_8", 195);
        put("BUTTON_9", 196);
        put("BUTTON_10", 197);
        put("BUTTON_11", 198);
        put("BUTTON_12", 199);
        put("BUTTON_13", 200);
        put("BUTTON_14", 201);
        put("BUTTON_15", 202);
        put("BUTTON_16", 203);
        put("LANGUAGE_SWITCH", 204);
        put("MANNER_MODE", 205);
        put("3D_MODE", 206);
        put("CONTACTS", 207);
        put("CALENDAR", 208);
        put("MUSIC", 209);
        put("CALCULATOR", 210);
        put("ZENKAKU_HANKAKU", 211);
        put("EISU", 212);
        put("MUHENKAN", 213);
        put("HENKAN", 214);
        put("KATAKANA_HIRAGANA", 215);
        put("YEN", 216);
        put("RO", 217);
        put("KANA", 218);
        put("ASSIST", 219);

    }

}
