package org.p2gx.hpo2gforms.gform;



public record TsvFormItem(
        String label,
        String definition,
        String comment,
        String synonyms,
        String parents) {


     /**
     * @param term        an HPO term to be displayed on the Google form
     * @param hpoOntology reference to the HPO ontology
     * @return a {@link FormItem} carrying the display data for {@code term}
     */
    public static TsvFormItem fromTsvFields(String label,
        String definition,
        String comment,
        String synonyms,
        String parents) {

        return new TsvFormItem(label, definition, comment, synonyms, parents);
    }

     /**
     * Renders this term as a JSON object literal matching the shape expected by
     * the {@code TERMS} array in the generated Apps Script: id, label, synonyms,
     * parents, definition, comment.
     */
    public String toJsonObject() {
        String synonymValue = synonyms.isBlank() ? "None found" : synonyms;
        return String.format("""
                  {
                    "label": "%s",
                    "synonyms": "%s",
                    "parents": "%s",
                    "definition": "%s",
                    "comment": "%s"
                  }""",
                jsonEscape(label),
                jsonEscape(synonymValue),
                jsonEscape(parents),
                jsonEscape(definition),
                jsonEscape(comment)
        );
    }

    /**
     * Escapes a string for safe inclusion inside a double-quoted JSON string
     * (which is also a valid JS string literal). Unlike the old {@code escape()}
     * helper, this preserves apostrophes and quotes instead of deleting them.
     */
    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
    
}
