package org.p2gx.hpo2gforms.gform;

import java.util.HashMap;
import java.util.Map;

public class TextBolder {

    private static final Map<Character, String> boldMap = new HashMap<>();

        static {
            // Bold uppercase letters
            boldMap.put('A', "\uD835\uDC00");
            boldMap.put('B', "\uD835\uDC01");
            boldMap.put('C', "\uD835\uDC02");
            boldMap.put('D', "\uD835\uDC03");
            boldMap.put('E', "\uD835\uDC04");
            boldMap.put('F', "\uD835\uDC05");
            boldMap.put('G', "\uD835\uDC06");
            boldMap.put('H', "\uD835\uDC07");
            boldMap.put('I', "\uD835\uDC08");
            boldMap.put('J', "\uD835\uDC09");
            boldMap.put('K', "\uD835\uDC0A");
            boldMap.put('L', "\uD835\uDC0B");
            boldMap.put('M', "\uD835\uDC0C");
            boldMap.put('N', "\uD835\uDC0D");
            boldMap.put('O', "\uD835\uDC0E");
            boldMap.put('P', "\uD835\uDC0F");
            boldMap.put('Q', "\uD835\uDC10");
            boldMap.put('R', "\uD835\uDC11");
            boldMap.put('S', "\uD835\uDC12");
            boldMap.put('T', "\uD835\uDC13");
            boldMap.put('U', "\uD835\uDC14");
            boldMap.put('V', "\uD835\uDC15");
            boldMap.put('W', "\uD835\uDC16");
            boldMap.put('X', "\uD835\uDC17");
            boldMap.put('Y', "\uD835\uDC18");
            boldMap.put('Z', "\uD835\uDC19");

            // Bold lowercase letters
            boldMap.put('a', "\uD835\uDC1A");
            boldMap.put('b', "\uD835\uDC1B");
            boldMap.put('c', "\uD835\uDC1C");
            boldMap.put('d', "\uD835\uDC1D");
            boldMap.put('e', "\uD835\uDC1E");
            boldMap.put('f', "\uD835\uDC1F");
            boldMap.put('g', "\uD835\uDC20");
            boldMap.put('h', "\uD835\uDC21");
            boldMap.put('i', "\uD835\uDC22");
            boldMap.put('j', "\uD835\uDC23");
            boldMap.put('k', "\uD835\uDC24");
            boldMap.put('l', "\uD835\uDC25");
            boldMap.put('m', "\uD835\uDC26");
            boldMap.put('n', "\uD835\uDC27");
            boldMap.put('o', "\uD835\uDC28");
            boldMap.put('p', "\uD835\uDC29");
            boldMap.put('q', "\uD835\uDC2A");
            boldMap.put('r', "\uD835\uDC2B");
            boldMap.put('s', "\uD835\uDC2C");
            boldMap.put('t', "\uD835\uDC2D");
            boldMap.put('u', "\uD835\uDC2E");
            boldMap.put('v', "\uD835\uDC2F");
            boldMap.put('w', "\uD835\uDC30");
            boldMap.put('x', "\uD835\uDC31");
            boldMap.put('y', "\uD835\uDC32");
            boldMap.put('z', "\uD835\uDC33");
        }

        public static String encodeBold(String input) {
            StringBuilder encodedText = new StringBuilder();

            for (char c : input.toCharArray()) {
                String boldChar = boldMap.getOrDefault(c, String.valueOf(c));
                encodedText.append(boldChar);
            }

            return encodedText.toString();
        }

}
