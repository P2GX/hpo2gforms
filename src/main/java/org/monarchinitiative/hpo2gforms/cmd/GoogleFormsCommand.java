package org.monarchinitiative.hpo2gforms.cmd;

import org.monarchinitiative.hpo2gforms.gform.GoogleForm;
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
import java.util.*;
import java.util.concurrent.Callable;


/**
 * The logic of this class is that we want to create Java-script like Google Apps code
 * to create a questionnaire for workshop participants to vote on existing HPO term names
 * and definitions. We start with a given HPO id and the code will create a questionnaire
 * for that term and all of its descendants.
 */
@CommandLine.Command(name = "forms",
        mixinStandardHelpOptions = true,
        description = "Create Google Forms Code")
public class GoogleFormsCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleFormsCommand.class);

    @CommandLine.Option(names={"-t","--target"}, required = true, description = "HPO term id (number only)")
    private String targetHpoId;

    @CommandLine.Option(names={"-o","--outfile"}, description = "outfile name (default: script.txt)")
    private String outFileName = "script.txt";

    @CommandLine.Option(names={"-x","--max"}, description = "maximum items per questionnaire (default: ${DEFAULT-VALUE} )")
    private Integer maxItemsPerQuestionnaire = 25;

    @CommandLine.Option(names={"-l", "--limit"}, description = "limit output to 3 items (for testing")
    private boolean limit = false;


    @Override
    public Integer call() {
        // check that target id is valid
        Ontology hpoOntology = OntologyLoader.loadOntology(new File(hpopath));
        Optional<TermId> opt = getTargetId(hpoOntology);
        if (opt.isEmpty()) {
            return 1;
        }
        TermId targetId = opt.get();
        List<TermId> targetList = getTargetIdAndDescendants(hpoOntology, targetId);
        List<List<Term>> termIdPartition = partitionTermIds(targetList, maxItemsPerQuestionnaire, hpoOntology);
        List<String> fnames = getOutputFileNames(targetId, termIdPartition.size());
        for (int i = 0; i < termIdPartition.size(); i++) {
            int part = i+1;
            String fname = fnames.get(i);
            List<Term> terms = termIdPartition.get(i);
            if (limit) {
                terms = terms.stream().limit(3).toList();
            }
            GoogleForm gform = new GoogleForm(terms, hpoOntology, targetId, part);
            String fxn = gform.getFunction();
            System.out.println(fxn);
            System.out.println("We output the function to the file " + fname);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
                bw.write(fxn);
            } catch (IOException e) {
                throw new PhenolRuntimeException(e);
            }
            if (limit) break;
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


    public static List<List<Term>> getBatches(List<TermId> collection, int batchSize, Ontology hpoOntology){
        int i = 0;
        List<List<TermId>> batches = new ArrayList<>();
        while(i<collection.size()){
            int nextInc = Math.min(collection.size()-i,batchSize);
            List<TermId> batch = collection.subList(i,i+nextInc);
            batches.add(batch);
            i = i + nextInc;
        }
        List<List<Term>> termBatches = new ArrayList<>();
        for (List<TermId> batch : batches) {
            List<Term> terms = new ArrayList<>();
            for (TermId termId : batch) {
                Optional<Term> opt = hpoOntology.termForTermId(termId);
                if (opt.isPresent()) {
                    terms.add(opt.get());
                } else {
                    // should never happen
                    throw new PhenolRuntimeException("Could not find term " + termId);
                }
            }
            termBatches.add(terms);
        }

        return termBatches;
    }


    private List<List<Term>> partitionTermIds(List<TermId> targetSet, Integer maxSize, Ontology hpoOntology) {
        if (maxSize == null) {
            // if no maximum size is provided, return one group
            return getBatches(targetSet, Integer.MAX_VALUE, hpoOntology);
        } else {
            int n_groups = (int) Math.ceil((double)targetSet.size() / maxSize);
            int group_size = (int) Math.ceil((double)targetSet.size() / n_groups);
            return   getBatches(targetSet, group_size, hpoOntology);
        }
    }


    private List<TermId> getTargetIdAndDescendants(Ontology hpoOntology, TermId targetId) {
        List<TermId> targetList = new ArrayList<>();
        targetList.add(targetId);
        for (TermId tid: hpoOntology.graph().getDescendants(targetId)) {
            targetList.add(tid);
        }
        return targetList;
    }









    private Optional<TermId> getTargetId(Ontology hpoOntology) {
        if ( targetHpoId.length() != 7) {
            System.err.println("[ERROR] target ID must be a seven digit number that corresponds to an HPO id");
            System.err.println("[ERROR] for insance, 0002021, which corresponds to Pyloric stenosis HP:0002021");
            return Optional.empty();
        }

        for (char c : targetHpoId.toCharArray()) {
            if (!Character.isDigit(c)) {
                System.err.println("[ERROR] target ID must be a seven digit number that corresponds to an HPO id.");
                System.err.println("[ERROR] for instance, 0002021, which corresponds to Pyloric stenosis HP:0002021.");
                return  Optional.empty();
            }
        }
        TermId hpoId = TermId.of("HP", targetHpoId);
        return Optional.of(hpoId);
    }


}
