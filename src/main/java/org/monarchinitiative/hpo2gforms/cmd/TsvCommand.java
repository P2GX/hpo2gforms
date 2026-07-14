package org.monarchinitiative.hpo2gforms.cmd;


import org.monarchinitiative.hpo2gforms.gform.GoogleForm;
import org.monarchinitiative.hpo2gforms.gform.TsvFormItem;
import org.monarchinitiative.hpo2gforms.gform.TsvGoogleForm;
import org.monarchinitiative.hpo2gforms.gform.TsvTermReader;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;


/**
 * The logic of this class is that we want to create Java-script like Google Apps code
 * to create a questionnaire for workshop participants to vote on existing HPO term names
 * and definitions. We start with a given HPO id and the code will create a questionnaire
 * for that term and all of its descendants.
 */
@CommandLine.Command(name = "tsv",
        mixinStandardHelpOptions = true,
        description = "Create Google Forms Code from TSV")
public class TsvCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleFormsCommand.class);


    @CommandLine.Option(names={"-o","--outfile"}, description = "outfile name (default: script.txt)")
    private String outFileName = "script.txt";

    @CommandLine.Option(names={"-x","--max"}, description = "maximum items per questionnaire (default: ${DEFAULT-VALUE} )")
    private Integer maxItemsPerQuestionnaire = 25;

    @CommandLine.Option(names={"-l", "--limit"}, description = "limit output to 3 items (for testing")
    private boolean limit = false;

     @CommandLine.Option(names={"-t","--tsv"}, description = "Infile name", required=true)
    private Path infile;


    @Override
    public Integer call() throws IOException {
        // check that infile exists
        if (! Files.exists(infile)) {
            System.err.printf("Could not find input file at %s", infile);
            return 1;
        }
    List<TsvFormItem> items = TsvTermReader.readTerms(infile);
    int part = 1; // TODO create different forms
    TsvGoogleForm gform = new TsvGoogleForm(items, part);

    
    String fxn = gform.getFunction();
    System.out.println(fxn);
    System.out.println("We output the function to the file " + outFileName);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFileName))) {
        bw.write(fxn);
    } catch (IOException e) {
        throw new PhenolRuntimeException(e);
    }


        return 0;
    }


    public static List<String> getOutputFileNames(TermId targetId, int n_groups) {
        List<String> fnames = new ArrayList<>();
        for (int i = 0; i < n_groups; i++) {
            fnames.add(String.format("%s_%d.txt", targetId.getValue().replace(":", "_"), (1+i)));
        }
        return fnames;
    }


    

}
