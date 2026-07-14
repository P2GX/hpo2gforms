package org.monarchinitiative.hpo2gforms.gform;



import java.util.List;
import java.util.stream.Collectors;

public class TsvGoogleForm {
    private final int questionnairePart;
    private final List<TsvFormItem> formItemList;

    public TsvGoogleForm(List<TsvFormItem> itemList, int part) {
        this.questionnairePart = part;
        this.formItemList = itemList;
    }

    /**
     * @return the complete generated Google Apps Script source, ready to paste
     * into the Apps Script editor.
     */
    public String getFunction() {
        String termsArray = formItemList.stream()
                .map(TsvFormItem::toJsonObject)
                .collect(Collectors.joining(",\n"));

        return TEMPLATE
                .replace("__FORM_TITLE__", jsEscape(getQuestionnaireTitle()))
                .replace("__TERMS_ARRAY__", termsArray);
    }

    private String getQuestionnaireTitle() {
        return String.format("HPO Questionnaire part %d", questionnairePart);
    }

    /**
     * Escapes a string for safe inclusion inside a single-quoted JS string literal
     * (the form title is embedded that way in the template below).
     */
    private static String jsEscape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * Fixed Apps Script template. Only the form title and the TERMS array content
     * are generated per invocation; the form-building logic itself (section
     * headers, rating grid, comment box, page breaks) stays constant, so it only
     * needs to be reviewed and tested once rather than regenerated per term.
     */
    private static final String TEMPLATE = """
            var TERMS = [
            __TERMS_ARRAY__
            ];

            function hpo_questionnaire() {
              var form = FormApp.create('__FORM_TITLE__');
              form.setDescription(
                'Use this form to enter your opinion about HPO terms, definitions, comments, and synonyms.\\n\\n' +
                'For each term: review the details, then use the grid below it to Accept, Reject, Revise, or mark N/A ' +
                'for each component. If you select "Revise" for any row, please describe your suggested change in the ' +
                'text box that follows the grid.'
              );
              form.setProgressBar(true);
              moveToFolder(form, 'HPO Questionnaires');
              form.setShowLinkToRespondAgain(false);

              form.addTextItem()
                  .setTitle('Name')
                  .setRequired(true);

              var emailValidation = FormApp.createTextValidation()
                  .requireTextMatchesPattern('[^@\\\\s]+@[^@\\\\s]+\\\\.[^@\\\\s]+')
                  .setHelpText('Please enter a valid email address')
                  .build();
              form.addTextItem()
                  .setTitle('Email address')
                  .setRequired(true)
                  .setValidation(emailValidation);

              form.addPageBreakItem().setTitle(TERMS.length > 0 ? (TERMS[0].label + '  ·  ' + TERMS[0].id) : 'Term Review');

              TERMS.forEach(function (term, idx) {
                addTermSection(form, term);

                var isLast = (idx === TERMS.length - 1);
                var pageBreak = form.addPageBreakItem();
                pageBreak.setTitle(isLast ? 'Review complete' : TERMS[idx + 1].label + '  ·  ' + TERMS[idx + 1].id);
              });

              Logger.log('Form created: ' + form.getEditUrl());
              return form;
            }

            function addTermSection(form, term) {
              var definition = term.comment
                  ? term.definition + '  (Comment: ' + term.comment + ')'
                  : term.definition;

              var section = form.addSectionHeaderItem();
              section.setTitle(term.label + '  ·  ' + term.id);
              section.setHelpText(
                'Synonyms: ' + (term.synonyms || 'None found') + '\\n' +
                'Parent(s): ' + term.parents + '\\n' +
                'Definition: ' + definition
              );

              form.addGridItem()
                  .setTitle('Your assessment — ' + term.id)
                  .setRows(['Term label', 'Synonyms', 'Parent(s)', 'Definition'])
                  .setColumns(['Accept', 'Reject', 'Revise', 'N/A'])
                  .setRequired(true);

              form.addParagraphTextItem()
                  .setTitle('Comments / suggested revision — ' + term.id)
                  .setHelpText('Leave blank unless you selected "Revise" for one or more rows above.');
            }

            /**
             * Moves the given form into a Drive folder named folderName, creating the
             * folder first if it doesn't already exist. Requires Drive scope, so the
             * first run after adding this will prompt for an extra permission.
             */
            function moveToFolder(form, folderName) {
              var folders = DriveApp.getFoldersByName(folderName);
              var folder = folders.hasNext() ? folders.next() : DriveApp.createFolder(folderName);
              var file = DriveApp.getFileById(form.getId());
              folder.addFile(file);
              DriveApp.getRootFolder().removeFile(file); // remove from top-level My Drive
            }
            """;
}