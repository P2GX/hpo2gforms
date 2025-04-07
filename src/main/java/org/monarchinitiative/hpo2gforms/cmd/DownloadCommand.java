package org.monarchinitiative.hpo2gforms.cmd;


import org.monarchinitiative.biodownload.BioDownloader;
import org.monarchinitiative.biodownload.FileDownloadException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;


/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 10, 2017)
 */

@CommandLine.Command(name = "download",
        mixinStandardHelpOptions = true,
        description = "Download HPO JSON file.")
public final class DownloadCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class.getName());

    public String getName() { return "download"; }
    /**

     */
    public DownloadCommand()  {
    }

    /**
     * Perform the downloading.
     */
    @Override
    public Integer call()  {
        try {
            BioDownloader downloader = BioDownloader.builder(Path.of(downloadDirectory))
                    .overwrite(true)
                    .hpoJson()
                    .build();
            downloader.download();
            LOGGER.info("Done!");
            // Display version of HPO that was downloaded
            File hpJsonFile = new File(String.format("%s/hp.json", downloadDirectory));
            Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
            String version = hpo.version().orElse("n/a");
            System.out.println("Downloaded HPO version " + version + ".");
            return 0;
        } catch (FileDownloadException e) {
            LOGGER.error("Error: {}", e.getMessage(), e);
            return 1;
        }
    }

}