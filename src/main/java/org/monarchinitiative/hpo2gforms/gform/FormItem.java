package org.monarchinitiative.hpo2gforms.gform;

import org.monarchinitiative.phenol.ontology.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds the display data for one HPO term, and renders itself as a JSON
 * object literal for the {@code TERMS} array consumed by the generated
 * Google Apps Script (see {@link GoogleForm}). PMID cross-references, if
 * present, are folded into the end of {@code definition} as "[PMID:...]".
 */
public record FormItem(
        Term term,
        String label,
        String definition,
        String comment,
        String synonyms,
        List<Term> parents
) {

    /**
     * @param term an HPO term
     * @return a "; "-separated String listing all synonyms of a term, for display on the Google form.
     */
    private static String getSynonymString(Term term) {
        List<String> parts = new ArrayList<>();
        for (TermSynonym tsyn : term.getSynonyms()) {
            String label = tsyn.getValue();
            String scope = tsyn.getScope().toString();
            String stype = tsyn.getSynonymTypeName();
            parts.add(String.format("%s [%s;%s]", label, scope, stype));
        }
        return String.join("; ", parts);
    }

    /**
     * @param term an HPO term
     * @return " [PMID:123, PMID:456]" for the term's PMID cross-references, or ""
     * if none are found (so appending it to a definition is always safe).
     */
    private static String getPmidSuffix(Term term) {
        List<String> pmidList = term.getPmidXrefs().stream()
                .filter(SimpleXref::isPmid)
                .map(SimpleXref::getId)
                .toList();
        if (pmidList.isEmpty()) {
            return "";
        }
        String tags = pmidList.stream()
                .map(FormItem::formatPmidTag)
                .collect(java.util.stream.Collectors.joining(", "));
        return " [" + tags + "]";
    }

    /**
     * Normalizes a raw PMID xref id to a "PMID:123" tag, without double-prefixing
     * if the id already carries a "PMID" prefix from phenol.
     */
    private static String formatPmidTag(String id) {
        return id.toUpperCase().startsWith("PMID") ? id : "PMID:" + id;
    }

    /**
     * @return a compact "; "-separated String of this term's parent(s), e.g. "Abnormal communication (HP:0034434)".
     */
    private String getParentsString() {
        if (parents.isEmpty()) {
            return "please report error"; // we should never actually be processing the root, so this should never happen
        }
        List<String> parentStrings = new ArrayList<>();
        for (Term parent : parents) {
            parentStrings.add(String.format("%s (%s)", parent.getName(), parent.id().getValue()));
        }
        return String.join("; ", parentStrings);
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
                    "id": "%s",
                    "label": "%s",
                    "synonyms": "%s",
                    "parents": "%s",
                    "definition": "%s",
                    "comment": "%s"
                  }""",
                jsonEscape(term.id().getValue()),
                jsonEscape(label),
                jsonEscape(synonymValue),
                jsonEscape(getParentsString()),
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

    /**
     * @param term        an HPO term
     * @param hpoOntology reference to HPO ontology
     * @return the parent terms of {@code term} in {@code hpoOntology}
     */
    private static List<Term> getParents(Term term, Ontology hpoOntology) {
        List<Term> termList = new ArrayList<>();
        for (TermId tid : hpoOntology.graph().getParents(term.id())) {
            Optional<Term> opt = hpoOntology.termForTermId(tid);
            opt.ifPresent(termList::add);
        }
        return termList;
    }

    /**
     * @param term        an HPO term to be displayed on the Google form
     * @param hpoOntology reference to the HPO ontology
     * @return a {@link FormItem} carrying the display data for {@code term}
     */
    public static FormItem fromTerm(Term term, Ontology hpoOntology) {
        String label = term.getName();
        String definition = (term.getDefinition() == null ? "" : term.getDefinition()) + getPmidSuffix(term);
        String comment = term.getComment() == null ? "" : term.getComment();
        String synonyms = getSynonymString(term);
        List<Term> parents = getParents(term, hpoOntology);
        return new FormItem(term, label, definition, comment, synonyms, parents);
    }

}