package org.monarchinitiative.hpo2gforms;

import org.monarchinitiative.hpo2gforms.cmd.DownloadCommand;
import org.monarchinitiative.hpo2gforms.cmd.GoogleFormsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "hpo2gforms", mixinStandardHelpOptions = true, version = "0.0.1",
        description = "HPO to Google Forms")
public class Main implements Callable<Integer> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

        public static void main(String[] args) {
            if (args.length == 0) {
                // if the user doesn't pass any command or option, add -h to show help
                args = new String[]{"-h"};
            }
            LOGGER.trace("Starting HPO tools");
            CommandLine cline = new CommandLine(new Main())
                    .addSubcommand("download", new DownloadCommand())
                    .addSubcommand("gforms", new GoogleFormsCommand())
                   ;
            cline.setToggleBooleanFlags(false);
            int exitCode = cline.execute(args);
            System.exit(exitCode);
        }


        public static String getVersion() {
            String version = "0.0.0";// default, should be overwritten by the following.
            try {
                Package p = Main.class.getPackage();
                version = p.getImplementationVersion();
            } catch (Exception e) {
                // do nothing
            }
            return version;
        }


        @Override
        public Integer call() {
            // work done in subcommands
            return 0;
        }

    }
