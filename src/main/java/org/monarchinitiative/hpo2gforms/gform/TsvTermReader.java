package org.monarchinitiative.hpo2gforms.gform;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a TSV file of proposed/candidate HPO terms - terms with no HP ID
 * assigned yet - with columns (in this order):
 * {@code Label  Definition  Comment  Synonyms  PMIDs  Parents}
 * <p>
 * Cells may be RFC4180-style quoted (double-quote enclosed, embedded
 * newlines allowed, "" as an escaped quote), since spreadsheet exports
 * commonly wrap long definitions across multiple lines. Trailing empty
 * columns beyond "Parents" are ignored.
 * <p>
 * Produces one {@link FormItem} per data row, ready to hand to
 * {@link GoogleForm#GoogleForm(List, String)}.
 */
public final class TsvTermReader {

    // Matches "PMID: 12345" or "PMID:12345", case-insensitive, anywhere in a cell
    // (cells may list several PMIDs, one per line).
    private static final Pattern PMID_PATTERN = Pattern.compile("PMID:?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    // Splits "Abnormal fetal skeletal morphology - HP:0025662" into label + HP id.
    // Accepts either an en dash (-) or a hyphen (-) as the separator.
    private static final Pattern PARENT_ID_PATTERN = Pattern.compile("^(.*?)\\s*[\u2013-]\\s*(HP:\\d+)\\s*$");

    private TsvTermReader() {
    }

    /**
     * @param tsvFile path to the candidate-term TSV file
     * @return one FormItem per non-blank data row (header row is skipped)
     */
    public static List<TsvFormItem> readTerms(Path tsvFile) throws IOException {
        String content = Files.readString(tsvFile, StandardCharsets.UTF_8);
        List<List<String>> rows = parseDelimited(content, '\t');
        List<TsvFormItem> items = new ArrayList<>();
        int idCounter = 1;
        for (int i = 1; i < rows.size(); i++) { // start at 1: skip header row
            List<String> row = rows.get(i);
            if (row.stream().allMatch(String::isBlank)) {
                continue; // skip blank rows
            }
            String label = field(row, 0);
            if (label.isBlank()) {
                continue; // a row needs at least a label to be worth a form item
            }
            String definition = normalizeWhitespace(field(row, 1));
            String comment = normalizeWhitespace(field(row, 2));
            String synonyms = formatSemicolonList(field(row, 3));
            String pmidSuffix = extractPmidSuffix(field(row, 4));
            String parents = formatParents(field(row, 5));
            items.add(TsvFormItem.fromTsvFields(label, definition + pmidSuffix, comment, synonyms, parents));
        }
        return items;
    }

    private static String field(List<String> row, int index) {
        return index < row.size() ? row.get(index).trim() : "";
    }

    /** Collapses embedded newlines/runs of whitespace (from wrapped spreadsheet cells) into single spaces. */
    private static String normalizeWhitespace(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    /** Splits a ";"-separated cell into trimmed, cleaned, "; "-rejoined parts. */
    private static String formatSemicolonList(String raw) {
        if (raw.isBlank()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (String part : raw.split(";")) {
            String trimmed = normalizeWhitespace(part);
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        return String.join("; ", parts);
    }

    /**
     * Splits a ";"-separated Parents cell; entries with an embedded HP id
     * ("Label - HP:0025662") are reformatted as "Label (HP:0025662)" to match
     * the style used for ontology-backed terms. Entries with no HP id (a
     * parent that is itself just a proposed label, not yet an HP term) are
     * kept as-is.
     */
    private static String formatParents(String raw) {
        if (raw.isBlank()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (String part : raw.split(";")) {
            String trimmed = normalizeWhitespace(part);
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher m = PARENT_ID_PATTERN.matcher(trimmed);
            parts.add(m.matches() ? String.format("%s (%s)", m.group(1).trim(), m.group(2)) : trimmed);
        }
        return String.join("; ", parts);
    }

    /** @return " [PMID:123, PMID:456]" for all PMIDs found anywhere in the cell, or "" if none. */
    private static String extractPmidSuffix(String raw) {
        if (raw.isBlank()) {
            return "";
        }
        Set<String> tags = new LinkedHashSet<>(); // preserves first-seen order, drops duplicates
        Matcher m = PMID_PATTERN.matcher(raw);
        while (m.find()) {
            tags.add("PMID:" + m.group(1));
        }
        return tags.isEmpty() ? "" : " [" + String.join(", ", tags) + "]";
    }

    /**
     * Minimal RFC4180-style delimited-text parser: supports quoted fields
     * (double-quote enclosed), embedded newlines inside quotes, "" as an
     * escaped quote, and a configurable delimiter (tab, here).
     * <p>
     * NOTE: if your project already depends on a CSV library (e.g. Apache
     * Commons CSV), prefer that over this hand-rolled parser instead -
     * quoting edge cases are easy to get subtly wrong by hand, and a
     * maintained library has already dealt with them. This is included so
     * the TSV path works with zero new dependencies.
     */
    private static List<List<String>> parseDelimited(String content, char delimiter) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        int n = content.length();
        while (i < n) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < n && content.charAt(i + 1) == '"') {
                        field.append('"'); // escaped quote
                        i += 2;
                    } else {
                        inQuotes = false; // closing quote
                        i++;
                    }
                } else if (c == '\r') {
                    i++; // normalize CRLF -> LF inside quoted fields
                } else {
                    field.append(c);
                    i++;
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                } else if (c == delimiter) {
                    currentRow.add(field.toString());
                    field.setLength(0);
                    i++;
                } else if (c == '\r') {
                    i++; // ignore; newline is handled on the following \n
                } else if (c == '\n') {
                    currentRow.add(field.toString());
                    field.setLength(0);
                    rows.add(currentRow);
                    currentRow = new ArrayList<>();
                    i++;
                } else {
                    field.append(c);
                    i++;
                }
            }
        }
        if (field.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(field.toString());
            rows.add(currentRow);
        }
        return rows;
    }
}